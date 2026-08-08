# Delivery Tests

One case per row of the phase-1 grading sheet. Each file names the row it covers
in its header, so the suite can be walked in the same order as the sheet during
the delivery session.

```
test/delivery-tests/
├── run_delivery_tests.py
├── 01-infrastructure/            git tag, dependency management, architecture
├── 02-menus/                     signup, login, main, profile, news, settings, collection
├── 03-quests/                    travel log, quest logic, rewards, priority
├── 04-levels/                    the four chapters and the eight special levels
├── 05-core-mechanics/            time, sun, waves, eating, garden, selection, win/lose
├── 06-zombies/                   plus all-zombies/ with one case per zombie
├── 07-plants/                    plus all-plants/ with one case per plant
├── 08-greenhouse/
├── 09-shop/
├── 10-score-and-leaderboard/
├── 11-minigames/                 all five minigames
└── 12-user-experience/           UX, linter, git diff size
```

## How to run

Run everything (423 automatic cases plus 17 manual ones):

```bash
python test/delivery-tests/run_delivery_tests.py
```

Run one group:

```bash
python test/delivery-tests/run_delivery_tests.py 05
```

Run a single case and print the whole game session:

```bash
python test/delivery-tests/run_delivery_tests.py TC-0201-1 -v
```

Every case runs in its own temporary `data/` folder, so cases never affect each
other or your real save. A case that fails is retried once automatically, which
absorbs the random parts of the game.

## What the output means

The runner prints one line per case, grouped by folder:

```
--- 02-menus
  ok    TC-0201-1    a valid register creates the account
  FAIL  TC-0204-2    login refuses an unknown username
      - after 'login -u ghost -p Abcd123!': expected output was not found
        expected: Username does not exist.
```

The run ends with the totals and, when something failed, the list of files to
open:

```
AUTOMATIC : 423 passed / 423
MANUAL    : 17 case(s) for the TA to check by hand
```

`MANUAL` cases have no `>` lines. They are repository or user-experience checks
(git tag, `gradlew build`, linter report, password hashing on disk) and the runner
only lists them, with the steps written inside the file.

The exit code is `0` when every automatic case passed and `1` otherwise.

## Reading a case file

```
=== TEST TC-0201-1: a valid register creates the account
#
# Rubric (Phase 1) : signup menu > create a new user
# Group            : 02 - Game start & menus
# Kind             : automatic (deterministic)
#
> register -u tester -p Abcd123! Abcd123! -n Tester -e tester@mail.com -g male
? Validation passed. Please pick a security question.
```

| Prefix | Meaning |
|--------|---------|
| `>` | a line typed into the game |
| `?` | a substring that must appear in the output, checked in order |
| `#` | a comment for the reader; the runner ignores it |
| `>> RESTART` | closes the game and starts it again on the same `data/` folder |

`>> RESTART` is what makes "stay logged in" and "progress is kept" testable: it is
exactly the quit-and-relaunch a grader would do by hand.

Cases whose header says `Kind: automatic (RANDOM: ...)` assert only the part of
the output that is deterministic. They end with a `SAMPLE RUN` block holding real
output recorded from this project, so the expected shape of a random result — a
wave roll, a grave position, a sun type, a vase content — is still documented.
