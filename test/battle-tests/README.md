# Battle Tests — how plants and zombies interact

This is the fourth suite in the repository. The other three check things one at a time;
this one only checks what happens when a plant and a zombie meet on the same board.

| Suite | Location | Question it answers |
|-------|----------|---------------------|
| General suite | `test/run_tests.py` | does the game work end to end? |
| Delivery suite | `test/delivery-tests/` | is every row of the grading sheet covered? |
| Entity suite | `test/entity-tests/` | does each plant and each zombie match its database row? |
| **Battle suite** | **`test/battle-tests/`** | **do the combat rules between them actually fire?** |

The entity suite asks *"does Snow Pea cost 150 sun?"*. This suite asks
*"does a Snow Pea shot really slow a zombie down, and by how much?"*.

---

## Layout

Folders are interaction categories, not entities, because one rule usually involves two
different plants or a plant and a zombie:

```
test/battle-tests/
├── README.md
├── run_battle_tests.py
├── 01-projectile-physics/        straight, lobbed, piercing, multi-lane, backward, bouncing
├── 02-elemental-effects/         fire, ice, poison, warmth, butter
├── 03-armor-interactions/        damage ordering, magnet, multi-layer armour
├── 04-defensive-plants/          walls, reflect, sun on hit, stacking
├── 05-explosives-and-traps/      area damage, lane damage, arming delay, freezes
├── 06-zombie-attacks-plants/     eating, one-shot smash, torch, ice, sheep, octopus
├── 07-mind-control-and-movement/ hypnosis, swallowing, lane pushing and pulling
└── 08-lane-defense/              lawn mower, graves, flying over obstacles
```

## Test file format

Identical to the other suites, so nothing new has to be learned:

```
=== TEST BT-0202: torchwood turns a passing pea into a fire pea
#
# Interaction : a modifier plant changing a projectile that flies through it
# Source      : plants.csv row 52 and the Pea tag description
# Setup       : Peashooter at column 1, Torchwood at column 3, zombie further right
# Arithmetic  : 20 damage doubled to 40, 10 shots = 400 damage
# Rule        : without the Torchwood the same setup would leave 3400
#
> menu scoring-game
? Scoring game started
> add plant -t Peashooter
> add plant -t Torchwood
> start game
? The battle begins
> cheat add -n 9000 suns
> cheat remove-cooldown
> plant plant -t Peashooter -l (1,3)
> plant plant -t Torchwood -l (3,3)
> cheat spawn-zombie -t Gargantuar -l 9,3
> advance time -t 150 ticks
> zombies info
? Gargantuar:
?     health: 3200
```

## How the numbers are made deterministic

Three choices keep every assertion an exact integer instead of a guess:

1. **The scoring-game board.** Its lawn is built with no graves, no sliders and no water,
   so nothing blocks a projectile by accident. The Egypt board scatters two random graves
   and would make the arithmetic differ from run to run.
2. **A fixed damage window.** One level 1 Peashooter deals 20 damage every 1.5 seconds, so
   150 ticks are always exactly 10 shots and exactly 200 damage. Every other plant is
   measured over the same window using its own interval from `plants.csv`.
3. **Gargantuar and King as measuring targets.** Gargantuar has 3600 hit points and no
   armour, so it survives any single measurement and its remaining health is a clean
   subtraction. King has speed 0, so when a backward-shooting plant is tested the target
   cannot walk out of range.

The preamble unlocks chapters, plants and money separately instead of `unlock-all`,
because `unlock-all` also raises every plant to maximum level and would change the damage
and cost numbers.

---

## Running the suite

From `test/battle-tests/`:

```bash
python run_battle_tests.py
```

### Useful filters

```bash
python run_battle_tests.py --group 02-elemental
```

```bash
python run_battle_tests.py --id BT-0202 --verbose
```

```bash
python run_battle_tests.py --list
```

| Flag | Meaning |
|------|---------|
| `--group <text>` | only categories whose folder name contains the text |
| `--id <test id>` | one case, for example `BT-0107` |
| `--list` | print the matching cases grouped by category without running them |
| `--verbose` | print every case as it runs and the failure detail |
| `--timeout <seconds>` | per-case timeout, default 90 |

---

## Current status: 37 of 47 pass

### Interactions that work

| Case | Rule proven |
|------|-------------|
| `BT-0102` | a lobbed cabbage flies over a Wall-nut and still deals its 200 damage |
| `BT-0103` | a Cactus shot pierces the front zombie and deals the full 300 to the one behind |
| `BT-0104` | a Threepeater hits its own lane and the neighbour lane in the same window |
| `BT-0105` | Split Pea sends two peas backward, 400 damage against a stationary King |
| `BT-0106` | Starfruit sends one pea backward, 200 damage |
| `BT-0201` | a fire pea deals exactly double, 400 instead of 200 |
| `BT-0202` | a Torchwood upgrades a passing pea and doubles the damage to 400 |
| `BT-0203` | an ice shot leaves the zombie at 7.2 instead of 5.4 after 15 seconds |
| `BT-0204` | poison ignores a 1100 point bucket and kills the 190 point body |
| `BT-0301` | armour is spent before the body, bucket 900 while health stays 190 |
| `BT-0302` | Magnet-shroom pulls a metal bucket |
| `BT-0303` | Magnet-shroom leaves a non metal cone at its full 370 |
| `BT-0501` | Cherry Bomb clears a 3x3 area including the neighbour row |
| `BT-0502` | Jalapeno kills zombies at both ends of the lane |
| `BT-0504` | Potato Mine arms after 15 seconds and then destroys the zombie |
| `BT-0602` | a Gargantuar destroys a 4000 point Wall-nut in one hit |

### Interactions the document describes but the code does not do yet

| Case | Missing rule |
|------|--------------|
| `BT-0107` | Bowling Bulb does not bounce into the neighbour lane |
| `BT-0205` | a fire plant next to a frozen plant does not melt its ice |
| `BT-0404` | Endurian does not reflect damage to the zombie eating it |
| `BT-0405` | Sun Bean does not produce sun when it is bitten |
| `BT-0406` | a Pumpkin cannot be stacked on top of another plant |
| `BT-0505` | Iceberg Lettuce does not freeze the zombie that steps on it |
| `BT-0703` | Garlic does not push its eater into another lane |
| `BT-0704` | Sweet Potato does not pull zombies into its lane |
| `BT-0802` | the lawn mower is not consumed, so the row is saved more than once |
| `BT-0804` | a Dodo does not fly over a Wall-nut, it eats it |

---

## Notes

* Cases in `06-zombie-attacks-plants` assert that the plant is destroyed. For the Explorer
  torch and the Troglobite ice block that message is also produced by a zombie simply
  eating the plant, so those two cases confirm the outcome but not the mechanism.
* One JVM is started per case, so a full run takes a few minutes. Use `--group` while
  working on one rule.
* The runner never writes inside the repository; each case gets a fresh temporary working
  directory and its own throw-away user.
