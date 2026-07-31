# Entity Tests

One folder per plant and per zombie. Each folder holds the cases that check that
single entity against its database row and its documented ability.

```
test/entity-tests/
├── run_entity_tests.py
├── plants/     69 folders, e.g. 06-Peashooter/
└── zombies/    38 folders, e.g. 12-Hunter/
```

## How to run

Run everything (703 cases, a few minutes):

```bash
python test/entity-tests/run_entity_tests.py
```

Run only the plants or only the zombies:

```bash
python test/entity-tests/run_entity_tests.py --group zombies
```

Run one entity folder:

```bash
python test/entity-tests/run_entity_tests.py --entity 12-Hunter
```

Run a single case and print the whole game session:

```bash
python test/entity-tests/run_entity_tests.py --id Z12-7 --verbose
```

List the cases without running them, or raise the per-case time limit:

```bash
python test/entity-tests/run_entity_tests.py --list
python test/entity-tests/run_entity_tests.py --timeout 120
```

The project is compiled once, then every case runs in its own temporary `data/`
folder, so cases never see each other's save files and your real save is never
touched.

## What the output means

One line per entity folder:

```
OK   plants   06-Peashooter                   7 passed   0 failed
FAIL zombies  12-Hunter                       6 passed   1 failed
```

`OK` means every case in that folder passed. For a failing case the runner prints
the command that was sent, the substring it expected, and whether that text never
appeared at all or appeared in the wrong order:

```
FAIL  Z12-7  body health drops by exactly the damage dealt
    - after 'zombies info': expected output was not found
      expected:     health: 500
```

The run ends with the totals:

```
PASSED: 703 / 703
FAILED: 0 / 703
```

The exit code is `0` when everything passed and `1` when at least one case failed,
so the suite can be used as a CI step.

## Reading a case file

```
=== TEST Z12-7: body health drops by exactly the damage dealt
#
# Entity    : Hunter
# Body hit points: 700
#
> plant plant -t Cabbage-pult -l (1,3)
> cheat spawn-zombie -t Hunter -l 9,3
> advance time -t 150 ticks
> zombies info
? Hunter:
?     health: 500
```

| Prefix | Meaning |
|--------|---------|
| `>` | a line typed into the game |
| `?` | a substring that must appear in the output, checked in order |
| `#` | a comment for the reader; the runner ignores it |

Some headers carry a `FIX (test-side only, game code untouched)` block. It records
why that case was rewritten, for example because the zombie disables the shooter
and therefore never loses the amount of health a naive setup would expect.
