# Ability Tests

Cases for the abilities the other suites do not touch: the sun economy, the plant
food reserve, plant lifespan, range limits, area splash and the command guards.

```
test/ability-tests/
├── run_ability_tests.py
├── 01-sun-economy/       how much sun each producer drops and when it is banked
├── 02-plant-food/        filling the reserve, spending it, what it changes
├── 03-plant-lifecycle/   limited lifespan, plucking, tile reporting
├── 04-range-and-area/    magnet range, area splash, multi-lane, charged shots
└── 05-command-guards/    refusals when a command cannot apply
```

## How to run

Run everything (23 cases):

```bash
python test/ability-tests/run_ability_tests.py
```

Run one group:

```bash
python test/ability-tests/run_ability_tests.py --group 01-sun-economy
```

Run a single case and print the whole game session:

```bash
python test/ability-tests/run_ability_tests.py --id AT-0403 --verbose
```

List the cases without running them, or raise the per-case time limit:

```bash
python test/ability-tests/run_ability_tests.py --list
python test/ability-tests/run_ability_tests.py --timeout 180
```

The project is compiled once, then every case runs in its own temporary `data/`
folder, so cases never affect each other or your real save.

## What the output means

Failing cases are listed first, then one line per group, then the totals:

```
PER ABILITY GROUP
OK   01-sun-economy                       5 passed   0 failed
OK   04-range-and-area                    6 passed   0 failed

PASSED: 23 / 23
FAILED: 0 / 23
```

The exit code is `0` when everything passed and `1` otherwise.

## Reading a case file

```
=== TEST AT-0102: sunflower drops a sun worth 50
#
# Ability      : plants.csv: this producer creates a sun on its own tile
# Cost         : 50 sun, so the balance is 9050 - 50 = 9000 after planting
# Rule         : collecting the produced sun adds exactly 50
# Measured     : Collected 50 sun. Balance: 9050
#
> plant plant -t Sunflower -l (1,1)
> show sun amount
? Sun: 9000
> advance time -t 250 ticks
? plant Sunflower produced a sun at (1, 1)
> collect sun -l (1,1)
? Collected 50 sun. Balance: 9050
```

| Prefix | Meaning |
|--------|---------|
| `>` | a line typed into the game |
| `?` | a substring that must appear in the output, checked in order |
| `#` | a comment for the reader; the runner ignores it |

Every header carries a `Measured` line. That is the literal text the game printed
while the case was being written, so the expected values are recordings rather
than predictions.

## Why the numbers never drift

Two board choices remove the randomness that would otherwise make these cases flaky.

**Sun cases run in Dark Ages, not in the scoring game.** Dark Ages is a night
chapter, so no sun falls from the sky. Every sun on the lawn can only have come
from the plant under test, which means `collect sun` can never pick up a stray
sky sun by accident. Plants are placed in column 1 because the chapter scatters
its graves from column 3 onwards.

**Every other case runs in the scoring game.** That board is built with no graves,
no sliders and no water, so nothing blocks a projectile and no tile refuses a
plant.

On top of that the damage windows are fixed. One level 1 Peashooter deals 20
damage every 1.5 seconds, so 150 ticks are always exactly 200 damage, and a
Gargantuar with 3600 hit points and no armour survives every single measurement,
which keeps the remaining health a clean subtraction. The preamble unlocks
chapters, plants and money separately instead of `unlock-all`, because
`unlock-all` also raises every plant to maximum level and would change both the
sun costs and the damage numbers.

## What is covered

### 01 — sun economy

| Case | Rule |
|------|------|
| `AT-0101` | Gold Bloom bursts into 375 sun and costs nothing |
| `AT-0102` | Sunflower drops a sun worth 50 |
| `AT-0103` | Twin Sunflower drops a sun worth 100 |
| `AT-0104` | Primal Sunflower drops a sun worth 75 |
| `AT-0105` | a produced sun waits on the lawn and is only banked when collected |

### 02 — plant food

| Case | Rule |
|------|------|
| `AT-0201` | the cheat fills the reserve one unit at a time |
| `AT-0202` | feeding a plant spends exactly one unit |
| `AT-0203` | plant food doubles a Peashooter, 400 damage instead of 200 |
| `AT-0204` | a sun producer can be fed too |

### 03 — plant lifecycle

| Case | Rule |
|------|------|
| `AT-0301` | Puff-shroom is still at full health after 55 seconds |
| `AT-0302` | it withers away past 60 seconds and frees the tile |
| `AT-0303` | plucking removes a plant and frees the tile |
| `AT-0304` | a planted Wall-nut reports 4000 out of 4000 |

### 04 — range and area

| Case | Rule |
|------|------|
| `AT-0401` | Magnet-shroom cannot reach a bucket six tiles away |
| `AT-0402` | the same magnet does reach one standing close |
| `AT-0403` | Melon-pult splashes into the neighbouring lane, both zombies take 320 |
| `AT-0404` | Threepeater hits three lanes at once, 200 damage in each |
| `AT-0405` | Citron fits two 800 point shots into 15 seconds |
| `AT-0406` | Winter Melon damages and chills with the same shot |

### 05 — command guards

| Case | Rule |
|------|------|
| `AT-0501` | feeding is refused with an empty reserve |
| `AT-0502` | collecting sun from an empty tile is refused |
| `AT-0503` | plucking an empty tile is refused |
| `AT-0504` | a tile cannot hold two plants |

## Relation to the other suites

| Suite | Scope |
|-------|-------|
| `test/run_tests.py` | broad end to end behaviour |
| `test/delivery-tests/` | one folder per row of the grading sheet |
| `test/entity-tests/` | each plant and each zombie against its database row |
| `test/battle-tests/` | combat rules between a plant and a zombie |
| **`test/ability-tests/`** | **sun economy, plant food, lifespan, range, area and guards** |
