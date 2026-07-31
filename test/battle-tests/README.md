# Battle Tests

Cases that put a plant and a zombie on the same board and check that the combat
rule between them actually fires. Folders are interaction categories, because one
rule usually involves two different entities.

```
test/battle-tests/
├── run_battle_tests.py
├── 01-projectile-physics/        straight, lobbed, piercing, multi-lane, bouncing
├── 02-elemental-effects/         fire, ice, poison, warmth, butter
├── 03-armor-interactions/        damage ordering, magnet, multi-layer armour
├── 04-defensive-plants/          walls, reflect, sun on hit, stacking
├── 05-explosives-and-traps/      area damage, lane damage, arming delay, freezes
├── 06-zombie-attacks-plants/     eating, one-shot smash, torch, ice, sheep, octopus
├── 07-mind-control-and-movement/ hypnosis, swallowing, lane pushing and pulling
└── 08-lane-defense/              lawn mower, graves, flying over obstacles
```

## How to run

Run everything (47 cases):

```bash
python test/battle-tests/run_battle_tests.py
```

Run one category:

```bash
python test/battle-tests/run_battle_tests.py --group 04-defensive-plants
```

Run a single case and print the whole game session:

```bash
python test/battle-tests/run_battle_tests.py --id BT-0404 --verbose
```

List the cases without running them, or raise the per-case time limit:

```bash
python test/battle-tests/run_battle_tests.py --list
python test/battle-tests/run_battle_tests.py --timeout 180
```

The project is compiled once, then every case runs in its own temporary `data/`
folder, so cases never affect each other or your real save.

## What the output means

Failing cases are listed first, then one line per category:

```
FAIL  BT-0404  endurian reflects damage to the zombie eating it

PER INTERACTION GROUP
OK   03-armor-interactions               5 passed   0 failed
FAIL 04-defensive-plants                 5 passed   1 failed
```

For a failing case the runner prints the command that was sent, the substring it
expected, and whether that text never appeared or appeared in the wrong order:

```
    - after 'zombies info': expected output was not found
      expected: Zombie of type Normal is dead
```

The run ends with the totals:

```
PASSED: 47 / 47
FAILED: 0 / 47
```

The exit code is `0` when everything passed and `1` otherwise.

## Reading a case file

```
=== TEST BT-0404: endurian reflects damage to the zombie eating it
#
# Interaction : a wall that hurts the zombie chewing on it
# Source      : plants.csv row 46
#
> plant plant -t Endurian -l (2,3)
> cheat spawn-zombie -t Normal -l 3,3
> advance time -t 100 ticks
? Zombie of type Normal is dead
```

| Prefix | Meaning |
|--------|---------|
| `>` | a line typed into the game |
| `?` | a substring that must appear in the output, checked in order |
| `#` | a comment for the reader; the runner ignores it |

Some headers carry a `FIX (test-side only, game code untouched)` block. It records
why that case was rewritten, for example because a Gargantuar one-shots a wall and
therefore can never trigger a reflect.
