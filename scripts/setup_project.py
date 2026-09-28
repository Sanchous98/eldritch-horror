#!/usr/bin/env python3
"""Create and populate a GitHub Project (v2) board for eldritch-horror.

Requires a token with the `project` scope (obtain via `gh auth refresh -s project`).

Idempotent: reuses an existing project with the same title, adds any missing
single-select options, skips items already on the board, and sets Priority + Area
from each issue's labels/milestone. Safe to re-run as the roadmap grows.

Board shape:
  - Project: "Eldritch Horror — Roadmap"
  - Fields:  Priority (P0..P3), Area (see AREA_OPTIONS)
  - Items:   every issue in the repo.
"""
import json
import re
import subprocess
import sys
import urllib.request

REPO = "Sanchous98/eldritch-horror"
API = "https://api.github.com"
GQL = "https://api.github.com/graphql"
PROJECT_TITLE = "Eldritch Horror — Roadmap"

PRIORITY_OPTIONS = [
    ("P0", "RED", "Blocks everything else"),
    ("P1", "ORANGE", "Current milestone, do next"),
    ("P2", "YELLOW", "Planned"),
    ("P3", "GRAY", "Later / nice-to-have"),
]
AREA_OPTIONS = [
    ("Core", "GRAY", "Framework and platform"),
    ("Progression", "GREEN", "Character growth"),
    ("Sanity", "PURPLE", "Sanity axis"),
    ("Corruption", "PINK", "Corruption axis"),
    ("Cult", "ORANGE", "Factions"),
    ("Ritual", "RED", "Rituals"),
    ("Quest", "BLUE", "Quests and dialogue"),
    ("World", "BLUE", "Worldgen and dimensions"),
    ("Bestiary", "RED", "Mobs and bosses"),
    ("Economy", "YELLOW", "Items and trading"),
    ("UI", "BLUE", "Menus, codex, HUD"),
    ("Art & Audio", "PINK", "Presentation"),
    ("Build", "GRAY", "Build and tooling"),
    ("Compat", "GREEN", "Compatibility and release"),
    ("QA", "YELLOW", "Balance, tests, performance"),
    ("Research", "ORANGE", "Decisions"),
]


def token():
    try:
        t = subprocess.check_output(["gh", "auth", "token"], text=True).strip()
        if t:
            return t
    except Exception:
        pass
    with open("/home/dev/.config/gh/hosts.yml") as f:
        m = re.search(r"oauth_token:\s*(gho_\S+)", f.read())
    if not m:
        sys.exit("no token available")
    return m.group(1)


TOKEN = token()


def gql(query, variables=None):
    req = urllib.request.Request(
        GQL,
        data=json.dumps({"query": query, "variables": variables or {}}).encode(),
        method="POST",
        headers={
            "Authorization": f"token {TOKEN}",
            "Accept": "application/vnd.github+json",
            "Content-Type": "application/json",
        },
    )
    try:
        with urllib.request.urlopen(req) as r:
            out = json.loads(r.read().decode())
    except urllib.error.HTTPError as e:
        sys.exit(f"GraphQL HTTP {e.code}: {e.read().decode()}")
    if "errors" in out:
        sys.exit(f"GraphQL errors: {json.dumps(out['errors'], indent=2)}")
    return out["data"]


def rest(path):
    req = urllib.request.Request(
        API + path,
        headers={"Authorization": f"token {TOKEN}", "Accept": "application/vnd.github+json"},
    )
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read().decode())


def rest_paged(path):
    out, page = [], 1
    sep = "&" if "?" in path else "?"
    while True:
        batch = rest(f"{path}{sep}per_page=100&page={page}")
        out.extend(batch)
        if len(batch) < 100:
            return out
        page += 1


# ------------------------------------------------------------------ project
def find_or_create_project():
    login = rest("/user")["login"]
    data = gql(
        "query($login:String!){ user(login:$login){ id projectsV2(first:50){ nodes{ id number title url } } } }",
        {"login": login},
    )
    for p in data["user"]["projectsV2"]["nodes"]:
        if p["title"] == PROJECT_TITLE:
            print(f"  reuse project #{p['number']}")
            return data["user"]["id"], p
    created = gql(
        """mutation($ownerId:ID!,$title:String!){
             createProjectV2(input:{ownerId:$ownerId,title:$title}){
               projectV2 { id number title url }
             } }""",
        {"ownerId": data["user"]["id"], "title": PROJECT_TITLE},
    )["createProjectV2"]["projectV2"]
    print(f"  created project #{created['number']} {created['url']}")
    return data["user"]["id"], created


def sync_field(project_id, name, options):
    """Return the field with the given single-select options ensured."""
    data = gql(
        """query($projectId:ID!){ node(id:$projectId){ ... on ProjectV2 {
             fields(first:100){ nodes{
               ... on ProjectV2SingleSelectField { id name options{ id name } }
             } } } } }""",
        {"projectId": project_id},
    )
    field = next((f for f in data["node"]["fields"]["nodes"] if f and f.get("name") == name), None)
    wanted = [{"name": n, "color": c, "description": d} for n, c, d in options]
    if field is None:
        field = gql(
            """mutation($projectId:ID!,$name:String!,$options:[ProjectV2SingleSelectFieldOptionInput!]!){
                 createProjectV2Field(input:{projectId:$projectId,dataType:SINGLE_SELECT,
                   name:$name,singleSelectOptions:$options}){
                   projectV2Field { ... on ProjectV2SingleSelectField { id name options{ id name } } }
                 } }""",
            {"projectId": project_id, "name": name, "options": wanted},
        )["createProjectV2Field"]["projectV2Field"]
        print(f"  created field {name}")
        return field
    have = {o["name"] for o in field["options"]}
    if have != {n for n, _, _ in options}:
        field = gql(
            """mutation($fieldId:ID!,$options:[ProjectV2SingleSelectFieldOptionInput!]!){
                 updateProjectV2Field(input:{fieldId:$fieldId,singleSelectOptions:$options}){
                   projectV2Field { ... on ProjectV2SingleSelectField { id name options{ id name } } }
                 } }""",
            {"fieldId": field["id"], "options": wanted},
        )["updateProjectV2Field"]["projectV2Field"]
        print(f"  updated field {name} ({len(options)} options)")
    else:
        print(f"  reuse field {name}")
    return field


def existing_item_ids(project_id):
    ids = set()
    cursor = None
    while True:
        data = gql(
            """query($projectId:ID!,$cursor:String){ node(id:$projectId){ ... on ProjectV2 {
                 items(first:100, after:$cursor){ pageInfo{ hasNextPage endCursor }
                   nodes{ content{ ... on Issue { id } } } } } } }""",
            {"projectId": project_id, "cursor": cursor},
        )
        items = data["node"]["items"]
        for it in items["nodes"]:
            c = it.get("content") or {}
            if c.get("id"):
                ids.add(c["id"])
        if not items["pageInfo"]["hasNextPage"]:
            return ids
        cursor = items["pageInfo"]["endCursor"]


def link_repo(project_id, repo_id):
    try:
        gql(
            """mutation($projectId:ID!,$repositoryId:ID!){
                 linkProjectV2ToRepository(input:{projectId:$projectId,repositoryId:$repositoryId}){
                   repository { id } } }""",
            {"projectId": project_id, "repositoryId": repo_id},
        )
        print("  linked repository")
    except SystemExit as e:
        print(f"  (repo link: {e})")


# ------------------------------------------------------------------ mapping
AREA_BY_LABEL = [
    ("sanity", "Sanity"), ("corruption", "Corruption"), ("cult", "Cult"),
    ("ritual", "Ritual"), ("dialogue", "Quest"), ("quest", "Quest"),
    ("dimension", "World"), ("worldgen", "World"),
    ("boss", "Bestiary"), ("bestiary", "Bestiary"),
    ("economy", "Economy"), ("accessibility", "UI"),
    ("localization", "Compat"), ("compatibility", "Compat"),
    ("balance", "QA"), ("performance", "QA"), ("testing", "QA"),
    ("audio", "Art & Audio"), ("art", "Art & Audio"),
    ("progression", "Progression"), ("ui", "UI"),
    ("build", "Build"), ("tech-debt", "Core"),
]


def area_for(issue):
    if issue["title"].lower().startswith("decide"):
        return "Research"
    labels = {l["name"] for l in issue["labels"]}
    for key, area in AREA_BY_LABEL:
        if key in labels:
            return area
    return "Core"


def priority_for(issue):
    title = issue["title"].lower()
    if title.startswith("decide"):
        return "P0"
    if title.startswith("epic:"):
        return "P2"
    ms = (issue.get("milestone") or {}).get("title", "")
    for prefix, p in (("M1", "P1"), ("M2", "P1"), ("M3", "P2"), ("M4", "P2"),
                      ("M5", "P2"), ("M6", "P2"), ("M7", "P3"), ("M8", "P3"),
                      ("M9", "P3"), ("M10", "P3"), ("M11", "P3"), ("M12", "P3")):
        if ms.startswith(prefix):
            return p
    return "P3"


def main():
    owner_id, project = find_or_create_project()
    pid = project["id"]
    print("fields:")
    prio = sync_field(pid, "Priority", PRIORITY_OPTIONS)
    area = sync_field(pid, "Area", AREA_OPTIONS)
    repo = rest(f"/repos/{REPO}")
    link_repo(pid, repo["node_id"])

    prio_opts = {o["name"]: o["id"] for o in prio["options"]}
    area_opts = {o["name"]: o["id"] for o in area["options"]}

    on_board = existing_item_ids(pid)
    issues = rest_paged(f"/repos/{REPO}/issues?state=all")
    issues = [i for i in issues if "pull_request" not in i]
    print(f"items: {len(issues)} issues, {len(on_board)} already on board")
    added = 0
    for issue in issues:
        if issue["node_id"] in on_board:
            continue
        item = gql(
            """mutation($projectId:ID!,$contentId:ID!){
                 addProjectV2ItemById(input:{projectId:$projectId,contentId:$contentId}){
                   item { id } } }""",
            {"projectId": pid, "contentId": issue["node_id"]},
        )["addProjectV2ItemById"]["item"]
        iid = item["id"]
        for field, opt, opts in ((prio, priority_for(issue), prio_opts),
                                 (area, area_for(issue), area_opts)):
            gql(
                """mutation($projectId:ID!,$itemId:ID!,$fieldId:ID!,$optionId:String!){
                     updateProjectV2ItemFieldValue(input:{projectId:$projectId,itemId:$itemId,
                       fieldId:$fieldId,value:{singleSelectOptionId:$optionId}}){
                       projectV2Item { id } } }""",
                {"projectId": pid, "itemId": iid, "fieldId": field["id"],
                 "optionId": opts[opt]},
            )
        added += 1
    print(f"  added {added} item(s)")
    print(f"\nboard: {project.get('url')}")


if __name__ == "__main__":
    main()
