# Entity Tests — every plant and every zombie

This suite complements the two suites that already exist in this repository:

| Suite | Location | Scope |
|-------|----------|-------|
| General suite | `test/run_tests.py` + `test/testcases.txt` | broad end-to-end behaviour of the game |
| Delivery suite | `test/delivery-tests/` | one folder per rubric row of the phase-1 grading sheet |
| **Entity suite** | **`test/entity-tests/`** | **one folder per plant and per zombie, one file per ability** |

The entity suite answers a single question: *does every individual plant and every
individual zombie behave the way the project document and the game database say it
should?*

---

## Where the expectations come from

Nothing in this suite is invented. Every assertion is traced back to one of three
sources, and each test file names its source in the header block:

| Source | File | Used for |
|--------|------|----------|
| Plant database | `src/database/csv/plants.csv` | cost, base HP, damage, action interval, recharge, base ability, plant-food effect, level 2/3/4 upgrades |
| Zombie database | `src/database/csv/zombies.xlsx` | hit points, speed, eat DPS, wave cost, armour type and armour hit points |
| Project document | the phase-1 PDF | the per-zombie special abilities and the chapter mechanics that the spreadsheets do not cover |

---

## Layout

```
test/entity-tests/
├── README.md
├── run_entity_tests.py
├── plants/
│   ├── 01-Sunflower/
│   │   ├── TC-P01-1_catalogue-entry-matches-the-plant-database.txt
│   │   ├── TC-P01-2_category-damage-and-action-interval-are-exposed.txt
│   │   └── ...
│   ├── 06-Peashooter/
│   └── ...  (69 plant folders)
└── zombies/
    ├── 01-Normal/
    ├── 02-Cone-Head/
    └── ...  (38 zombie folders)
```

Folders are numbered with the same ID the entity has in the database, so
`06-Peashooter` is row 6 of `plants.csv` and the test ids inside it all start with `P06-`.

---

## Test file format

The format is the same one the delivery suite already uses, so nothing new has to be
learned:

```
=== TEST P06-3: planting costs exactly the documented sun price
#
# Entity    : Peashooter
# Cost      : 100
# Checks    : sun balance drops by exactly 100
#
# Legend:  '>' = a line typed into the game
#          '?' = a substring that must appear in the output (checked in order)
#          '#' = comment for the reader (ignored by the runner)
# --------------------------------------------------------------------------

> menu enter chapter -c Egypt -l 1
> add plant -t Peashooter
> start game
? The battle begins
> cheat add -n 9000 suns
> cheat remove-cooldown
> show sun amount
? Sun: 9050
> plant plant -t Peashooter -l (2,3)
? Peashooter planted at (2, 3).
> show sun amount
? Sun: 8950
```

* `>` lines are typed into the game in order.
* `?` lines are substrings that must appear in the output, **in the order written**.
* `#` lines are documentation for whoever reads the file and are ignored.

### The shared preamble

Every test file starts from a logged-in account, so the runner prepends the same four
lines to each case and gives each case its own throw-away user and its own working
directory:

```
register -u e00001 -p Abcd123! Abcd123! -n Tester -e e00001@mail.com -g male
pick question -q 1 -a apple -c apple
login -u e00001 -p Abcd123!
menu cheat unlock-chapters
menu cheat unlock-plants
menu cheat rich
```

`menu cheat unlock-all` is deliberately **not** used. That cheat also raises every plant
to its maximum level, which lowers the sun cost through the upgrade table and would make
the cost assertions read the upgraded price instead of the database price. Unlocking
chapters, plants and currency separately keeps every plant at level 1, so the numbers in
`plants.csv` are exactly the numbers the tests check.

---

## What is checked per plant

| # | Test | Source |
|---|------|--------|
| 1 | catalogue entry matches the plant database | cost, HP, recharge from `plants.csv` |
| 2 | category, damage and action interval are exposed | category, damage, interval |
| 3 | planting costs exactly the documented sun price | cost |
| 4 | the same tile cannot hold two plants | one-plant-per-tile rule |
| 5 | the plant appears in the battlefield status list | planting works |
| 6 | plant food can be given to this plant | plant-food column |
| 7 | upgrade path is reachable from the collection menu | level 2/3/4 columns |
| 8 | category behaviour | sun production, damage output, wall endurance or explosion, depending on the category |

Plants that have no cost, no HP or no plant-food effect simply skip the cases that cannot
apply to them, so a consumable such as Cherry Bomb gets a different set from Sunflower.

## What is checked per zombie

| # | Test | Source |
|---|------|--------|
| 1 | spawns with the documented health pool | `zombies.xlsx` hit points |
| 2 | armour layer is reported with its full strength | armour type and armour HP |
| 3 | walking behaviour over time | speed (a speed of 0 is asserted to stay put) |
| 4 | the zombie takes damage from a plant in its row | combat wiring |
| 5 | reaching the house triggers the lawn mower | eat DPS and the mower rule |
| 6 | almanac entry unlocks after the zombie is met | HP, speed, eat DPS, wave cost |
| 7 | exact damage arithmetic | body HP and armour HP |
| 8 | magnet interaction *(armoured zombies only)* | the metallic flag of the armour |
| 9 | signature ability *(zombies that have one)* | the ability text in the project document |

### Test 7 — the exact arithmetic

One level 1 Peashooter deals 20 damage every 1.5 seconds, so in 150 ticks it deals
exactly 200 damage. Test 7 uses that fixed budget and then reads `zombies info` back:

* **Armoured zombie** — the armour must absorb all 200 first and the body must be
  untouched. For Bucket Head the case asserts `bucket: 900` (1100 − 200) *and*
  `health: 190` on the same zombie. This proves both the armour value and the rule that
  armour is consumed before the body.
* **Unarmoured zombie** — the body must drop by exactly 200, so Gargantuar is asserted at
  `health: 3400` (3600 − 200).
* **Fragile zombie** — if 200 damage already exceeds its total, the case demands the death
  line `Zombie of type ... is dead`.

### Test 8 — the metallic flag

Magnet-shroom may only pull metal. The suite turns that into two opposite assertions:

* Bucket Head and Knight are metallic, so the case demands
  `Magnet-shroom pulled the metal armor off the ...`.
* Cone Head, Brick Head, Pharaoh and Newspaper are **not** metallic, so their case demands
  that the armour layer is still listed at full strength after the magnet has been standing
  next to them.

### Test 9 — the signature ability

Twenty-three zombies have an ability described in the project document, and each one gets a
case built around a concrete observable. A few examples:

| Zombie | Documented ability | What the case asserts |
|--------|--------------------|-----------------------|
| Gargantuar | throws an Imp at half health | `Imp:` appears in `zombies info` |
| King | upgrades a plain zombie into a Knight | `Knight:` appears in `zombies info` |
| Explorer | its torch burns a plant one tile ahead | `Plant Sunflower at (4, 3) is destroyed.` |
| Tomb Raiser | spits bones that create graves | `GRAVE` appears in `show map` |
| Barrel Roller | a broken barrel releases two imps | `Imp:` appears in `zombies info` |
| Troglobite | the pushed ice block destroys plants | the plant destruction line |
| Imp Dragon | fire does not hurt it | `health: 190` after a Fire Peashooter fires at it |

A failure in test 9 does not mean the runner is broken — it means the ability described in
the document is not modelled in the code yet, and the header of the file tells you exactly
which behaviour is missing.

---

## Running the suite

From `test/entity-tests/`:

```bash
python run_entity_tests.py
```

The runner compiles `src/` once into a temporary directory, then runs each case in its own
temporary working directory so no saved data leaks between cases.

### Useful filters

```bash
python run_entity_tests.py --group plants
python run_entity_tests.py --group zombies
python run_entity_tests.py --entity Peashooter
python run_entity_tests.py --entity 06-Peashooter
python run_entity_tests.py --id P06-3 --verbose
python run_entity_tests.py --list
```

| Flag | Meaning |
|------|---------|
| `--group plants\|zombies\|all` | restrict to one half of the suite |
| `--entity <text>` | only folders whose name contains the text |
| `--id <test id>` | a single case, for example `Z06-7` |
| `--list` | print the matching cases without running them |
| `--verbose` | print every failure inline as it happens |
| `--timeout <seconds>` | per-case timeout, default 60 |

### Reading the report

```
========================================================================
PER ENTITY
========================================================================
OK   plants   06-Peashooter                    7 passed    0 failed
FAIL zombies  25-Prospector                    6 passed    1 failed

========================================================================
PASSED: 676 / 703
FAILED:  27 / 703
```

The per-entity block is the part to read first: it shows at a glance which plant or which
zombie is incomplete, instead of a flat list of failures.

To dig into one failure:

```bash
python run_entity_tests.py --id Z25-7 --verbose
```

That prints the full game transcript for the case underneath the failure list.

---

## Current status

The suite holds **703 cases over 69 plant folders and 38 zombie folders**. On the code as
it stands today **676 pass and 27 fail**. Every remaining failure is a real difference
between the specification and the implementation, grouped here so they can be picked up
one by one:

### Abilities described in the document that are not modelled yet

| Case | Entity | Missing behaviour |
|------|--------|-------------------|
| `Z06-9` | Gargantuar | does not throw an Imp when it drops to half health |
| `Z10-9` | Tomb Raiser | does not create graves on the lawn |
| `Z17-9` | Juggler | does not spin and reflect straight shots |
| `Z19-9` | King | does not upgrade a nearby zombie into a Knight |
| `Z23-9` | Umbrella | the umbrella does not block lobbed shots |
| `Z24-9` | Turquoise | does not steal sun and does not fire the laser |
| `Z28-9` | Barrel Roller | a broken barrel does not release two imps |
| `Z31-9` | Weasel Hoarder | never summons weasels |

### Instant kill and explosive plants that do not resolve

| Case | Entity | Observation |
|------|--------|-------------|
| `P14-7` | Caulipower | marked `Insta-kill` in the database but a normal zombie survives |
| `P41-7` | Chomper | marked `Insta-kill` but a normal zombie survives |
| `P30-7` | Potato Mine | the mine does not destroy the zombie standing on its tile |
| `P31-7` | Primal Potato Mine | same as Potato Mine |
| `P37-7` | Tangle Kelp | does not pull the adjacent zombie under |
| `P60-7` | Grave Buster | does not resolve against the target tile |
| `P58-4` | Lily Pad | after placement the tile does not report the plant |

### Damage arithmetic that does not match the database

`Z12-7`, `Z13-7`, `Z15-7`, `Z17-7`, `Z18-7`, `Z22-7`, `Z28-7`, `Z33-7` — Hunter,
Troglobite, Octopus, Juggler, Wizard, Arcade, Barrel Roller and Surfer do not lose exactly
200 health after 200 damage. All of them are zombies whose documented behaviour changes how
they take damage, so these cases are the entry point for that work. `Z25-7` (Prospector)
and `Z27-7` (Newspaper) do not die when the damage dealt already exceeds their total.

### Movement

`Z33-3` (Surfer) and `Z37-3` (Jalapeno Zombie) do not report the expected position after
15 seconds of walking.

---

## Notes

* A failing case is not automatically a bug in the runner. Every failure listed above was
  checked by hand against the game output before being left in the suite.
* The suite launches one JVM per case, so a full run of all cases takes several minutes.
  Use the filters while working on a single entity.
* The runner never writes inside the repository; every case gets a fresh temporary
  working directory and a fresh user name.
