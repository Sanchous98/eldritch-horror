#!/usr/bin/env python3
"""Generate the guided-advancement tree for the mod's content (docs/ADVANCEMENTS.md).

The tree is plain data under src/main/resources/data/eldritch_horror/advancement/. Every node
uses the single criterion {"trigger": "minecraft:impossible"}; the systems that own the progress
call advancement/Advancements.award at the moment it happens, so there is no vanilla criteria
guessing to drift from the journal.

Titles and descriptions are translation keys. Quest-backed nodes reuse the quest's existing
quest.eldritch_horror.<id> / .desc keys, so the journal remains the single source of wording.

Usage:
    python3 tools/gen_advancements.py          # write advancement JSON + merge lang keys
    python3 tools/gen_advancements.py --check  # report missing/stale files, write nothing
"""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src/main/resources/data/eldritch_horror/advancement")
LANG = os.path.join(ROOT, "src/main/resources/assets/eldritch_horror/lang")
SETTLEMENTS = os.path.join(ROOT, "src/main/resources/assets/eldritch_horror/map/settlements.json")
NS = "eldritch_horror"


def rid(path):
    return f"{NS}:{path}"


# --- wording ---------------------------------------------------------------------------------

ROOT_TEXT = {
    "title": "Eldritch Horror",
    "description": "A guided path through the prologue, investigations, cults, cities and the "
                   "things that should not be.",
}
ROOT_TEXT_RU = {
    "title": "Древний ужас",
    "description": "Путь-проводник через пролог, расследования, культы, города и то, чему не "
                   "следует быть.",
}

PROLOGUE_CHOOSE = {
    "title": "Who Are You?",
    "description": "Choose an investigator at a prologue pedestal.",
}
PROLOGUE_CHOOSE_RU = {
    "title": "Кто вы?",
    "description": "Выберите исследователя у пьедестала в прологе.",
}

THREAT_TEXTS = {
    "first_ancient_one": {
        "title": "That Which Should Not Die",
        "description": "Land the killing blow on an Ancient One.",
    },
    "fallen_city": {
        "title": "A City Lost",
        "description": "Stand in a curated city as it Falls.",
    },
}
THREAT_TEXTS_RU = {
    "first_ancient_one": {
        "title": "То, что не должно умирать",
        "description": "Нанесите смертельный удар Древнему.",
    },
    "fallen_city": {
        "title": "Потерянный город",
        "description": "Окажитесь в курируемом городе, когда он падёт.",
    },
}

CITY_CONTRACT = {
    "title": "Atlas of the Fallen",
    "description": "Complete all 24 curated city contracts.",
}
CITY_CONTRACT_RU = {
    "title": "Атлас павших",
    "description": "Выполните все 24 контракта в курируемых городах.",
}

CITY_TEXT = {
    "description": "Complete the local contract in %s.",
}
CITY_TEXT_RU = {
    "description": "Выполните местный контракт в городе %s.",
}

CULT_RANK_TEXT = {
    "title": "%s (%s)",
    "description": "Standing with %s reaches %s.",
}
CULT_RANK_TEXT_RU = {
    "title": "%s (%s)",
    "description": "Положение у фракции %s достигает ранга %s.",
}

# --- tree ------------------------------------------------------------------------------------

# The curated cities, read from the same file the game reads.
CITY_NAMES_RU = {
    "tokyo": "Токио", "new_york": "Нью-Йорк", "mexico_city": "Мехико", "mumbai": "Мумбаи",
    "delhi": "Дели", "shanghai": "Шанхай", "buenos_aires": "Буэнос-Айрес",
    "los_angeles": "Лос-Анджелес", "cairo": "Каир", "rio_de_janeiro": "Рио-де-Жанейро",
    "beijing": "Пекин", "moscow": "Москва", "istanbul": "Стамбул", "paris": "Париж",
    "seoul": "Сеул", "lagos": "Лагос", "jakarta": "Джакарта", "london": "Лондон",
    "lima": "Лима", "bangkok": "Бангкок", "sydney": "Сидней", "rome": "Рим",
    "cape_town": "Кейптаун", "nairobi": "Найроби",
}

# Starter investigator chains: (first task, second task, signature item icon, investigator name).
STARTERS = [
    ("starter_agatha_grave", "starter_agatha_vigil", "rosary", "Sister Agatha"),
    ("starter_aldous_notes", "starter_aldous_survey", "antiquarians_compass", "Aldous Pemberton"),
    ("starter_amos_tonics", "starter_amos_cases", "sedative_vial", "Dr. Amos Hartley"),
    ("starter_cormac_rope", "starter_cormac_trap", "smugglers_token", "Cormac Blackwood"),
    ("starter_eleanor_notes", "starter_eleanor_signs", "seers_lens", "Eleanor Vance"),
    ("starter_evelyn_credit", "starter_evelyn_patron", "family_signet", "Evelyn Ashcombe"),
    ("starter_hazel_quill", "starter_hazel_story", "press_pass", "Hazel Quinn"),
    ("starter_jack_trail", "starter_jack_stakeout", "investigators_badge", "Jack Corrigan"),
    ("starter_marion_circle", "starter_marion_commune", "planchette", "Marion Delacroix"),
    ("starter_nikolai_books", "starter_nikolai_insight", "forbidden_grimoire", "Nikolai Volkov"),
    ("starter_tom_kit", "starter_tom_line", "dog_tags", "Tom Mallory"),
    ("starter_vera_chalk", "starter_vera_ward", "blood_chalice", "Vera Nightingale"),
]

CULTS = [
    ("drowned_choir", "pearl_reagent"),
    ("unblinking_eye", "order_sigil"),
    ("hollow_choir", "blood_offering"),
]
CULT_RANKS = ["initiate", "member", "devoted", "inner_circle"]

ENDGAME = [
    ("open_the_way", "cults/sworn_ally", "ritual_knife", "goal"),
    ("awaken_the_horror", "cults/open_the_way", "void_reagent", "challenge"),
    ("the_decision", "cults/awaken_the_horror", "effigy_doll", "challenge"),
    ("the_seal", "cults/awaken_the_horror", "order_sigil", "challenge"),
]


def load_cities():
    with open(SETTLEMENTS, encoding="utf-8") as handle:
        data = json.load(handle)
    cities = []
    for settlement in data["settlements"]:
        if settlement.get("tier") != "curated":
            continue
        key = re.sub(r"_[-\d]+_[-\d]+$", "", settlement["id"])
        cities.append((key, settlement["name"]))
    return cities


def quest(key):
    """A display that reuses the quest's journal wording."""
    return {"translate": f"quest.eldritch_horror.{key}"}, \
           {"translate": f"quest.eldritch_horror.{key}.desc"}


def display(icon, title, description, frame="task", root_background=None):
    out = {
        "icon": {"id": rid(icon)},
        "title": title,
        "description": description,
        "frame": frame,
        "show_toast": True,
        "announce_to_chat": True,
        "hidden": False,
    }
    if root_background is not None:
        out["background"] = root_background
        out["show_toast"] = False
        out["announce_to_chat"] = False
    return out


def node(path, parent, icon, title, description, frame="task", background=None):
    body = {
        "display": display(icon, title, description, frame, background),
        "criteria": {"impossible": {"trigger": "minecraft:impossible"}},
    }
    if parent is not None:
        body["parent"] = rid(parent)
    return path, body


def build_tree(cities):
    nodes = {}
    nodes["root"] = node(
        "root", None, "black_obol", {"translate": "advancement.eldritch_horror.root.title"},
        {"translate": "advancement.eldritch_horror.root.description"},
        background="minecraft:gui/advancements/backgrounds/stone")[1]

    title, desc = quest("first_steps")
    nodes["prologue/choose_investigator"] = node(
        "prologue/choose_investigator", "root", "investigators_badge",
        {"translate": "advancement.eldritch_horror.prologue.choose.title"},
        {"translate": "advancement.eldritch_horror.prologue.choose.description"})[1]
    nodes["prologue/first_steps"] = node(
        "prologue/first_steps", "prologue/choose_investigator", "gate_fragment",
        title, desc)[1]
    title, desc = quest("first_rite")
    nodes["prologue/first_rite"] = node(
        "prologue/first_rite", "prologue/first_steps", "chalk_reagent", title, desc, "goal")[1]

    for first, second, icon, _ in STARTERS:
        title, desc = quest(first)
        nodes[f"investigation/{first}"] = node(
            f"investigation/{first}", "prologue/first_steps", icon, title, desc)[1]
        title, desc = quest(second)
        nodes[f"investigation/{second}"] = node(
            f"investigation/{second}", f"investigation/{first}", icon, title, desc, "goal")[1]

    title, desc = quest("lorekeeper")
    nodes["investigation/lorekeeper"] = node(
        "investigation/lorekeeper", "prologue/first_rite", "tome_of_the_eye", title, desc, "goal")[1]
    title, desc = quest("lorekeeper_ii")
    nodes["investigation/lorekeeper_ii"] = node(
        "investigation/lorekeeper_ii", "investigation/lorekeeper", "star_codex", title, desc,
        "challenge")[1]

    title, desc = quest("sworn_ally")
    nodes["cults/sworn_ally"] = node(
        "cults/sworn_ally", "prologue/first_rite", "cult_token", title, desc, "goal")[1]
    for cult, icon in CULTS:
        parent = "cults/sworn_ally"
        for rank in CULT_RANKS:
            path = f"cults/{cult}/{rank}"
            frame = {"initiate": "task", "member": "goal", "devoted": "goal",
                     "inner_circle": "challenge"}[rank]
            title = {"translate": "advancement.eldritch_horror.cult.rank.title",
                     "with": [{"translate": f"rank.eldritch_horror.{cult}.{CULT_RANKS.index(rank)}"},
                              {"translate": f"cult.eldritch_horror.{cult}"}]}
            desc = {"translate": "advancement.eldritch_horror.cult.rank.description",
                    "with": [{"translate": f"cult.eldritch_horror.{cult}"},
                             {"translate": f"rank.eldritch_horror.{cult}.{CULT_RANKS.index(rank)}"}]}
            nodes[path] = node(path, parent, icon, title, desc, frame)[1]
            parent = path

    for quest_id, parent, icon, frame in ENDGAME:
        title, desc = quest(quest_id)
        nodes[f"cults/{quest_id}"] = node(
            f"cults/{quest_id}", parent, icon, title, desc, frame)[1]

    for key, name in cities:
        path = f"cities/{key}"
        nodes[path] = node(
            path, "prologue/first_steps", "city_map",
            {"translate": f"advancement.eldritch_horror.cities.{key}.title"},
            {"translate": "advancement.eldritch_horror.cities.description",
             "with": [{"translate": f"advancement.eldritch_horror.cities.{key}.title"}]})[1]
    nodes["cities/all_contracts"] = node(
        "cities/all_contracts", "cities/london", "world_atlas",
        {"translate": "advancement.eldritch_horror.cities.all.title"},
        {"translate": "advancement.eldritch_horror.cities.all.description"},
        "challenge")[1]

    nodes["threats/first_ancient_one"] = node(
        "threats/first_ancient_one", "prologue/first_rite", "eye_of_the_watcher",
        {"translate": "advancement.eldritch_horror.threats.first_ancient_one.title"},
        {"translate": "advancement.eldritch_horror.threats.first_ancient_one.description"},
        "challenge")[1]
    nodes["threats/fallen_city"] = node(
        "threats/fallen_city", "prologue/first_steps", "cursed_charm",
        {"translate": "advancement.eldritch_horror.threats.fallen_city.title"},
        {"translate": "advancement.eldritch_horror.threats.fallen_city.description"}, "goal")[1]
    title, desc = quest("reclaim_the_city")
    nodes["threats/reclaim_the_city"] = node(
        "threats/reclaim_the_city", "threats/fallen_city", "order_sigil", title, desc, "goal")[1]

    return nodes


# --- language --------------------------------------------------------------------------------

def lang_entries(cities):
    en = {
        "advancement.eldritch_horror.root.title": ROOT_TEXT["title"],
        "advancement.eldritch_horror.root.description": ROOT_TEXT["description"],
        "advancement.eldritch_horror.prologue.choose.title": PROLOGUE_CHOOSE["title"],
        "advancement.eldritch_horror.prologue.choose.description": PROLOGUE_CHOOSE["description"],
        "advancement.eldritch_horror.cult.rank.title": CULT_RANK_TEXT["title"],
        "advancement.eldritch_horror.cult.rank.description": CULT_RANK_TEXT["description"],
        "advancement.eldritch_horror.cities.description": CITY_TEXT["description"],
        "advancement.eldritch_horror.cities.all.title": CITY_CONTRACT["title"],
        "advancement.eldritch_horror.cities.all.description": CITY_CONTRACT["description"],
    }
    ru = {
        "advancement.eldritch_horror.root.title": ROOT_TEXT_RU["title"],
        "advancement.eldritch_horror.root.description": ROOT_TEXT_RU["description"],
        "advancement.eldritch_horror.prologue.choose.title": PROLOGUE_CHOOSE_RU["title"],
        "advancement.eldritch_horror.prologue.choose.description": PROLOGUE_CHOOSE_RU["description"],
        "advancement.eldritch_horror.cult.rank.title": CULT_RANK_TEXT_RU["title"],
        "advancement.eldritch_horror.cult.rank.description": CULT_RANK_TEXT_RU["description"],
        "advancement.eldritch_horror.cities.description": CITY_TEXT_RU["description"],
        "advancement.eldritch_horror.cities.all.title": CITY_CONTRACT_RU["title"],
        "advancement.eldritch_horror.cities.all.description": CITY_CONTRACT_RU["description"],
    }
    for key, name in cities:
        en[f"advancement.eldritch_horror.cities.{key}.title"] = name
        ru[f"advancement.eldritch_horror.cities.{key}.title"] = CITY_NAMES_RU.get(key, name)
    for threat, text in THREAT_TEXTS.items():
        en[f"advancement.eldritch_horror.threats.{threat}.title"] = text["title"]
        en[f"advancement.eldritch_horror.threats.{threat}.description"] = text["description"]
        translated = THREAT_TEXTS_RU[threat]
        ru[f"advancement.eldritch_horror.threats.{threat}.title"] = translated["title"]
        ru[f"advancement.eldritch_horror.threats.{threat}.description"] = translated["description"]
    return en, ru


def merge_lang(filename, entries):
    path = os.path.join(LANG, filename)
    with open(path, encoding="utf-8") as handle:
        data = json.load(handle)
    missing = {key: value for key, value in entries.items() if key not in data}
    if not missing:
        return 0
    data.update(missing)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(data, handle, ensure_ascii=False, indent=2)
        handle.write("\n")
    return len(missing)


def main():
    check = "--check" in sys.argv
    cities = load_cities()
    if len(cities) != 24:
        print(f"expected 24 curated cities, found {len(cities)}")
        return 1
    nodes = build_tree(cities)
    written = 0
    for path, body in nodes.items():
        target = os.path.join(DATA, f"{path}.json")
        rendered = json.dumps(body, ensure_ascii=False, indent=2) + "\n"
        if check:
            if not os.path.exists(target) or open(target, encoding="utf-8").read() != rendered:
                print(f"stale: {path}")
                written += 1
            continue
        os.makedirs(os.path.dirname(target), exist_ok=True)
        with open(target, "w", encoding="utf-8") as handle:
            handle.write(rendered)
        written += 1
    en, ru = lang_entries(cities)
    if check:
        for name, entries in (("en_us.json", en), ("ru_ru.json", ru)):
            with open(os.path.join(LANG, name), encoding="utf-8") as handle:
                existing = json.load(handle)
            absent = [key for key in entries if key not in existing]
            if absent:
                print(f"{name}: {len(absent)} missing advancement keys")
                written += len(absent)
        print(f"{len(nodes)} nodes; {written} stale/missing entries")
    else:
        added = merge_lang("en_us.json", en) + merge_lang("ru_ru.json", ru)
        print(f"wrote {len(nodes)} advancement nodes and {added} language keys")
    return 0


if __name__ == "__main__":
    sys.exit(main())
