#!/usr/bin/env python3
"""Create and populate a GitHub Project (v2) board for eldritch-horror.

Requires a token with the `project` scope (obtain via `gh auth refresh -s project`).
Idempotent: reuses an existing project with the same title, skips items already on it,
and skips fields that already exist.

Board shape:
  - Project: "Eldritch Horror — Roadmap"
  - Fields:  Priority (P0..P3), Area (Sanity/Corruption/Cult/Ritual/Content/Client/Build/Research)
  - Items:   every issue in the repo, with Priority + Area set from labels/milestone.
  - The repository is linked to the project.
"""
import json
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
    ("Sanity", "PURPLE", ""),
    ("Corruption", "PINK", ""),
    ("Cult", "GREEN", ""),
    ("Ritual", "RED", ""),
    ("Content", "YELLOW", ""),
    ("Client", "BLUE", ""),
    ("Build", "GRAY", ""),
    ("Research", "ORANGE", ""),
]


def token():
    # Prefer gh's token (it can carry the project scope); fall back to hosts.yml.
    try:
        t = subprocess.check_output(["gh", "auth", "token"], text=True).strip()
        if t:
            return t
    except Exception:
        pass
    import re
    with open("/home/dev/.config/gh/hosts.yml") as f:
        m = re.search(r"oauth_token:\s*(gho_\S+)", f.read())
    if not m:
        sys.exit("no token available")
    return m.group(1)


TOKEN = token()


def _post(url, payload):
    req = urllib.request.Request(
        url,
        data=json.dumps(payload).encode(),
        method="POST",
        headers={
            "Authorization": f"token {TOKEN}",
            "Accept": "application/vnd.github+json",
            "Content-Type": "application/json",
        },
    )
    try:
        with urllib.request.urlopen(req) as r:
            return json.loads(r.read().decode())
    except urllib.error.HTTPError as e:
        sys.exit(f"POST {url} -> {e.code}: {e.read().decode()}")


def gql(query, variables=None):
    out = _post(GQL, {"query": query, "variables": variables or {}})
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


# ------------------------------------------------------------------ project
def find_or_create_project(owner_id):
    data = gql(
        """query($login:String!){
             user(login:$login){ projectsV2(first:50){ nodes { id number title } } }
           }""",
        {"login": rest("/user")["login"]},
    )
    for p in data["user"]["projectsV2"]["nodes"]:
        if p["title"] == PROJECT_TITLE:
            print(f"  reuse project #{p['number']} ({p['id']})")
            return p
    created = gql(
        """mutation($ownerId:ID!,$title:String!){
             createProjectV2(input:{ownerId:$ownerId,title:$title}){
               projectV2 { id number title url }
             }
           }""",
        {"ownerId": owner_id, "title": PROJECT_TITLE},
    )["createProjectV2"]["projectV2"]
    print(f"  created project #{created['number']} {created['url']}")
    return created


def ensure_field(project_id, name, options):
    data = gql(
        """query($projectId:ID!){
             node(id:$projectId){ ... on ProjectV2 {
               fields(first:50){ nodes{
                 ... on ProjectV2SingleSelectField { id name options { id name } }
               } }
             } }
           }""",
        {"projectId": project_id},
    )
    for f in data["node"]["fields"]["nodes"]:
        if f and f.get("name") == name:
            print(f"  reuse field {name}")
            return f
    made = gql(
        """mutation($projectId:ID!,$name:String!,$options:[ProjectV2SingleSelectFieldOptionInput!]!){
             createProjectV2Field(input:{projectId:$projectId,dataType:SINGLE_SELECT,
               name:$name,singleSelectOptions:$options}){
               projectV2Field { ... on ProjectV2SingleSelectField { id name options { id name } } }
             }
           }""",
        {
            "projectId": project_id,
            "name": name,
            "options": [{"name": n, "color": c, "description": d} for n, c, d in options],
        },
    )["createProjectV2Field"]["projectV2Field"]
    print(f"  created field {name}")
    return made


def link_repo(project_id, repo_id):
    try:
        gql(
            """mutation($projectId:ID!,$repositoryId:ID!){
                 linkProjectV2ToRepository(input:{projectId:$projectId,repositoryId:$repositoryId}){
                   repository { id }
                 }
               }""",
            {"projectId": project_id, "repositoryId": repo_id},
        )
        print("  linked repository")
    except SystemExit as e:
        print(f"  (repo link skipped: {e})")


# ------------------------------------------------------------------ items
def priority_for(issue):
    title = issue["title"]
    ms = (issue.get("milestone") or {}).get("title", "")
    if title.startswith("Decide"):
        return "P0"
    if ms.startswith("M1") or ms.startswith("M2"):
        return "P1"
    if ms.startswith("M3") or ms.startswith("M4"):
        return "P2"
    return "P3"


def area_for(issue):
    labels = {l["name"] for l in issue["labels"]}
    if issue["title"].startswith("Decide"):
        return "Research"
    for key, area in [
        ("sanity", "Sanity"), ("corruption", "Corruption"), ("cult", "Cult"),
        ("ritual", "Ritual"), ("client", "Client"), ("build", "Build"),
        ("content", "Content"),
    ]:
        if key in labels:
            return area
    return "Build"


def main():
    me = rest("/user")
    owner_id = gql("query{viewer{id}}", {})["viewer"]["id"]
    repo = rest(f"/repos/{REPO}")
    print("project:")
    project = find_or_create_project(owner_id)
    pid = project["id"]
    print("fields:")
    prio = ensure_field(pid, "Priority", PRIORITY_OPTIONS)
    area = ensure_field(pid, "Area", AREA_OPTIONS)
    link_repo(pid, repo["node_id"])

    prio_opts = {o["name"]: o["id"] for o in prio["options"]}
    area_opts = {o["name"]: o["id"] for o in area["options"]}

    issues = rest(f"/repos/{REPO}/issues?state=all&per_page=100")
    print(f"items ({len(issues)} issues):")
    added = 0
    for issue in issues:
        item = gql(
            """mutation($projectId:ID!,$contentId:ID!){
                 addProjectV2ItemById(input:{projectId:$projectId,contentId:$contentId}){
                   item { id }
                 }
               }""",
            {"projectId": pid, "contentId": issue["node_id"]},
        )["addProjectV2ItemById"]["item"]
        iid = item["id"]
        for field, opt in ((prio, priority_for(issue)), (area, area_for(issue))):
            gql(
                """mutation($projectId:ID!,$itemId:ID!,$fieldId:ID!,$optionId:String!){
                     updateProjectV2ItemFieldValue(input:{projectId:$projectId,itemId:$itemId,
                       fieldId:$fieldId,value:{singleSelectOptionId:$optionId}}){
                       projectV2Item { id }
                     }
                   }""",
                {
                    "projectId": pid,
                    "itemId": iid,
                    "fieldId": field["id"],
                    "optionId": (prio_opts if field is prio else area_opts)[opt],
                },
            )
        added += 1
    print(f"  set fields on {added} item(s)")
    print(f"\nboard: {project.get('url', 'https://github.com/users/' + me['login'] + '/projects')}")


if __name__ == "__main__":
    main()
