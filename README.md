# group-33 — Plants vs. Zombies 2 (Phase 1)

## Welcome To Our Hell🔥

### Team Members

| Full Name | Student ID |
|-----------|------------|
| AmirHossein Yousefi | 404106571 |
| Kamyar Haghighatdoost | 404105778 |
| Hirad Sirati | 404105961 |

---

## Table of Contents

- [How to Run](#how-to-run)
- [How to Test](#how-to-test)
- [General Menu Rules](#general-menu-rules)
- [Command Reference by Menu](#command-reference-by-menu)
  - [Signup Menu](#signup-menu)
  - [Login Menu](#login-menu)
  - [Main Menu](#main-menu)
  - [Settings Menu](#settings-menu)
  - [News Menu](#news-menu)
  - [Profile Menu](#profile-menu)
  - [Game Menu — Plant Selection](#game-menu--plant-selection-preparation-phase)
  - [Game Menu — In Battle](#game-menu--in-battle)
  - [Collection Menu](#collection-menu)
  - [Greenhouse Menu](#greenhouse-menu)
  - [Shop Menu](#shop-menu)
  - [Travel Log Menu](#travel-log-menu)
  - [Leaderboard Menu](#leaderboard-menu)
- [Design Decisions Left to Us](#design-decisions-left-to-us)
  - [Levels & Chapters](#levels--chapters)
  - [Special Levels](#special-levels)
  - [Quests](#quests)
  - [Scoring Game (Miopoints)](#scoring-game-miopoints)
  - [Minigames](#minigames)
  - [Plant Upgrades](#plant-upgrades)
  - [Difficulty](#difficulty)
  - [Economy Numbers](#economy-numbers)
- [Cheat Commands](#cheat-commands)

---

## How to Run

The project has no external dependencies — plain Java (JDK 17+).

**Recommended (never hits a stale build):**

```bash
python run_game.py
```

This compiles every `.java` file under `src/` into `out/production/AP/` and then launches the game.

**Manual:**

```bash
javac -encoding UTF-8 -d out/production/AP $(find src -name "*.java")
java -cp out/production/AP Main
```

**IntelliJ:** run `Main.java`. If you ever see `Could not find or load main class Main`, do `Build → Rebuild Project` (a partial/incremental build can miss the default-package `Main` class).

All persistent data (accounts, progress, greenhouse, news, quests) is written to a `data/` folder next to where you launch the program.

---

## How to Test

A suite of 1000 doc-based test cases lives in `test/`:

```bash
python test/run_tests.py            # run all 1000 tests
python test/run_tests.py 372        # run a single test by id (prints the full game output)
```

Each test is run in its own isolated temp `data/` folder, so tests never affect each other or your real save. On any mismatch the runner prints the **test id**, **the exact command**, **the expected substring**, and whether the output was **missing** or **appeared out of order**. Current status: **1000 / 1000 passing**.

The test cases themselves are human-readable in `test/testcases.txt`:

```
=== TEST 0001: signup rejects invalid username 1
> register -u user! -p Abcd123! Abcd123! -n Nick -e a@b.com -g male
? Username can only contain letters, digits, and hyphens.
```

`>` is a command sent to the game, `?` is a substring expected in the output (checked in order).

---

## General Menu Rules

These commands work in **every** menu:

| Command | Description |
|---------|-------------|
| `menu show current` | Prints the name of the menu you are currently in. |
| `menu enter <menu_name>` | Moves to another menu (only to menus reachable from the current one). |
| `menu exit` | Leaves the current menu (see per-menu behavior below). |
| `quit` (aliases: `exit game`, `force quit`) | **Global force-quit** — closes the game from *any* menu, including while logged in. |

**Menu graph (who can go where):**

- **Signup** → Login. Exiting Signup ends the program.
- **Login** → Main (only after a successful login). Exiting Login returns to Signup.
- **Main** → Game (via chapter/scoring), Settings, News, Profile, Greenhouse, Travel Log, Leaderboard. Main can only be left with `menu logout`.
- **Game** → Collection. Exiting Game returns to Main.
- **Collection** → Game. Exiting returns to Game.
- **Greenhouse** → Shop (`enter shop`). Exiting returns to Main.
- **Shop** → exiting returns to Greenhouse.
- **Settings / News / Profile / Travel Log / Leaderboard** → exiting returns to Main.

Progress is **never** lost when the program closes — everything is saved to disk immediately.

Names for plants, zombies, chapters and menus are matched **case-insensitively and ignore spaces/dashes/underscores** (`wall-nut`, `Wall Nut`, `wallnut` all resolve to the same plant).

---

## Command Reference by Menu

> Notation: `<x>` = required argument, `[flag]` = optional. Coordinates on the lawn are `(<x>, <y>)` where `x` is the column (1–9, left→right) and `y` is the row (1–5, top→bottom).

### Signup Menu

The entry point of the game.

| Command | Description |
|---------|-------------|
| `register -u <username> -p <password> <password_confirm> -n <nickname> -e <email> -g <gender>` | Validates all fields; on success asks you to pick a security question. |
| `pick question -q <number> -a <answer> -c <answer_confirm>` | Chooses a security question (by number) and finalizes the account. |
| `menu enter login` | Go to the login menu. |

**Validation rules (errors are printed, no account is created until everything is valid):**

- **Username:** letters, digits and hyphens only; must not already exist.
- **Password:** at least 8 characters and must contain a lowercase letter, an uppercase letter, a digit **and** a special character. If it is weak, the reason is printed. `password` and `password_confirm` must match.
- **Nickname:** 3–30 characters (used as the display name).
- **Email:** exactly one `@`; a valid local part before it; a domain after it with at least one dot and a ≥2-letter TLD; no leading/trailing dots, no `..`, no forbidden symbols. (Examples of invalid: `john..doe@example.com`, `user@domain`, `user@domain.c`, `user@.com`.)
- **Gender:** `male` or `female`.

Passwords are stored **hashed (SHA-256)**, never in plain text — this satisfies the optional security bonus in the doc.

### Login Menu

| Command | Description |
|---------|-------------|
| `login -u <username> -p <password> [-stay-logged-in]` | Logs in. With `-stay-logged-in` you remain logged in across program restarts. On login you are told if you have unread news. |
| `forget password -u <username> -e <email>` | Starts password recovery. Both the username **and** the email must match the account; otherwise `The email does not match this username.` is shown. On a match, your saved security question is printed. |
| `answer -a <answer>` | Answers the security question. If correct, you may set a new password. |
| `new password -p <password> <confirm>` | Sets a new password (same strength rules as signup). |
| `quit password reset` | Aborts an in-progress password reset. |
| `menu exit` | Returns to the Signup menu. |

### Main Menu

The hub. Reached after authentication.

| Command | Description |
|---------|-------------|
| `menu enter chapter -c <chaptername>` | Enters a chapter and loads its furthest-unlocked level, then drops you into plant selection. Chapters: `Egypt`, `Frost Bite`, `Wavey Beach`, `Dark Ages`. |
| `menu enter chapter -c <chaptername> -l <level>` | Jumps straight to a specific level of a chapter (must be unlocked — see [cheats](#cheat-commands)). |
| `menu scoring-game` | Starts the scoring (miopoint) game — same daily zombie algorithm for every player. |
| `menu greenhouse` | Shortcut to the Greenhouse menu. |
| `menu travel-log` | Shortcut to the Travel Log (quests + minigames). |
| `menu leaderboard` | Shortcut to the Leaderboard. |
| `menu coin-wallet` | Shows your coin balance. |
| `menu gem-wallet` | Shows your diamond (gem) balance. |
| `menu enter <settings\|news\|profile\|greenhouse\|travellog\|leaderboard>` | Enter one of the sub-menus. |
| `menu logout` | Logs out and returns to the Signup menu. This is the **only** way to leave the main menu. |

Several cheat commands live here (`menu cheat unlock-all`, `unlock-chapters`, `unlock-plants`, `max-plants`, `rich`, `add <n> <coin|diamond>`) — see [Cheat Commands](#cheat-commands). For a demo, `menu cheat unlock-all` opens the whole game at once.

### Settings Menu

| Command | Description |
|---------|-------------|
| `menu settings change-difficulty -l <level>` | Sets the difficulty level (1–5). Default is 3. See [Difficulty](#difficulty). |
| `menu exit` | Return to Main. |

### News Menu

News items are generated when you unlock a plant, encounter a new zombie, unlock a level/minigame, or complete a quest. A red-flag notice is shown when you log in and have unread news.

| Command | Description |
|---------|-------------|
| `menu news show-unread` | Shows unread news, then marks it as read (won't show again). |
| `menu news show-all` | Shows the full news history (read + unread). |
| `menu exit` | Return to Main. |

### Profile Menu

| Command | Description |
|---------|-------------|
| `menu profile change-username -u <username>` | Changes username (error if identical to current or already taken). |
| `menu profile change-nickname -u <nickname>` | Changes the display nickname (error if identical to current). |
| `menu profile change-email -e <email>` | Changes email (error if identical to current). |
| `menu profile change-password -p <new_password> -o <old_password>` | Changes password (error if the old one is wrong or the new one equals the old). |
| `menu profile show-info` | Shows username, nickname, games played, coins, diamonds, levels passed and best miopoint. |
| `menu exit` | Return to Main. |

### Game Menu — Plant Selection (Preparation phase)

After entering a chapter (or a minigame/scoring game) you first pick your seeds. Default is **8 slots**.

| Command | Description |
|---------|-------------|
| `show all plants` | Lists every plant defined in the game. |
| `show available plants` | Lists the plants you may pick for this level. |
| `add plant -t <type>` | Adds a plant to your selection (errors: locked, unknown, already selected, slots full). |
| `remove plant -t <type>` | Removes a plant from your selection. |
| `boost plant -t <type>` | Spends 2 diamonds (or a stored greenhouse boost) so this plant's plant-food effect triggers instantly the first time you plant it this level. |
| `start game` | Begins the battle (needs at least one selected plant, except on conveyor-belt levels). |

### Game Menu — In Battle

Time is discrete: **1 tick = 0.1 second in-game**, so **10 ticks = 1 second**. Nothing happens until you advance time.

**Time & waves**

| Command | Description |
|---------|-------------|
| `advance time -t <count> ticks` | Advances the simulation by `count` ticks — plants act, zombies move/eat, sun falls, waves progress. |
| `start zombie waves` | Summons the horde. Wave 1 starts; each next wave is 25% harder, the final wave is doubled, and a new wave begins once 75% of the previous wave's health is gone. |

**Sun**

| Command | Description |
|---------|-------------|
| `show sun amount` | Prints your current sun. |
| `collect sun -l (<x>, <y>)` | Collects sun at a tile. Sky sun is normal (80%, 25 each), special (15%, 100 each) or radioactive (5%); a radioactive sun caught mid-air explodes (150 dmg to zombies in 5×5, 80 to plants in 3×3). |

**Plants**

| Command | Description |
|---------|-------------|
| `plant plant -t <type> -l (<x>, <y>)` | Plants a selected seed. Handles cost, recharge, terrain (water needs a Lily Pad, graves need Grave Buster, frozen tiles need Hot Potato), stacking (Pea Pod / Pumpkin) and instant plants (bombs, mints, Gold Bloom). |
| `pluck plant -l (<x>, <y>)` | Removes a plant (or a Lily Pad) from a tile. |
| `feed plant -l (<x>, <y>)` | Uses one plant food on the plant, triggering its special effect. |

**Info / map**

| Command | Description |
|---------|-------------|
| `show map` | Renders the lawn plus a header (wave, sun, plant foods) and a legend. Water `~`, grave `#`, sliders `^`/`v`, vases `U`, mowers `[M]`, brains `[B]`. |
| `show plants status` | For each selected plant: cost and whether/when it can be planted. |
| `show tile status -l (<x>, <y>)` | Details of the plant(s)/zombie(s) on a tile. |
| `zombies info` | Every zombie on the lawn: position, health, armor pieces and active effects (chilled/frozen/hypnotized), in the doc's format. |

**Minigame-only battle commands** (see [Minigames](#minigames)): `break vase -l (<x>, <y>)`, `place zombie -t <type> -l (<x>, <y>)`, `swap -l (<x1>, <y1>) (<x2>, <y2>)`, `upgrade -t <type>`.

Win message: `Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.` — you return to the main menu as a winner. If a zombie reaches the house on a mower-less row: `The zombie ate your brain; LOSER!!!`.

### Collection Menu

Reached from the Game menu. Zombies appear here only after you have seen them in battle.

| Command | Description |
|---------|-------------|
| `menu collection show-plants` | Plants you own (with level and seed-packet count). |
| `menu collection show-all-plants` | Every plant defined in the game. |
| `menu collection show-zombies` | Zombies you have encountered. |
| `menu collection show-all-zombies` | Every zombie defined in the game. |
| `menu collection show-plant -p <plant_name>` | Full stats of one plant. |
| `menu collection show-zombie -z <zombie_name>` | Full stats of one zombie. |
| `menu collection upgrade-plant -p <plant_name>` | Upgrades a plant (needs coins + seed packets of that plant). |
| `menu collection purchase-plant -p <plant_name>` | Buys a brand-new plant for **2000 coins**. |

### Greenhouse Menu

A 4×5 grid of pots (20 total). The first column (5 pots) is free; the rest are unlocked by buying pots in the shop. Growth is based on the **real system clock**.

| Command | Description |
|---------|-------------|
| `show greenhouse` | Shows every pot: locked / empty / growing (with time left) / `ready`. |
| `plant pot at (<x>, <y>)` | Plants a random plant in a free pot: 50% a Marigold, 50% one of your unlocked plants. |
| `collect (<x>, <y>)` | Harvests a fully grown pot. Marigold → 500 coins; an unlocked plant → stores one boost for that plant. |
| `grow (<x>, <y>)` | Instantly finishes growth for **1 diamond per remaining hour (rounded up)**. |
| `enter shop` | Opens the shop. |
| `menu exit` | Return to Main. |

Marigold grows in 2h; a random unlocked plant grows in 8h.

### Shop Menu

Reached from the Greenhouse. Two sections: **permanent items** and a **daily offer** (resets at 00:00 system time, buyable once per day).

| Command | Description |
|---------|-------------|
| `shop list` | Lists the permanent items. |
| `shop daily` | Shows today's daily offer. |
| `shop buy -i <item_id> -n <count> [-t <plant_type>]` | Buys `count` of an item. `-t` is required for the choice-bundle. |

**Item ids:**

| id | Item | Price | Effect |
|----|------|-------|--------|
| 1 | Pot | 2000 coins | Unlocks one greenhouse pot (max 20). |
| 2 | Plant Food | 3 diamonds | +1 plant food for the start of your next level (max 3 stored). |
| 3 | Random Seed Bundle | 1000 coins | 5 seed packets of a random unlocked plant. |
| 4 | Choice Seed Bundle | 5 diamonds | 10 seed packets of a chosen (`-t`) unlocked plant. |
| 5 | Currency Exchange | 5 diamonds | +500 coins. |
| 6 | Daily Offer | 1600 coins (20% off 2000) | 10 seed packets of a random unlocked plant, once per day. |

### Travel Log Menu

Holds the quests (grouped into pages by priority) and the minigames.

| Command | Description |
|---------|-------------|
| `travel log page <page_name>` | Shows a page. Pages: `critical`, `high`, `daily`, `minigame`. |
| `play minigame -n <name> -d <1\|2\|3>` | Starts a minigame at difficulty stage 1–3. Names: `vasebreaker`, `wallnut-bowling`, `i-zombie`, `beghouled`, `zombotany`. |
| `menu exit` | Return to Main. |

### Leaderboard Menu

| Command | Description |
|---------|-------------|
| `show leaderboard [-s <column>] [-o <asc\|desc>]` | Lists all players. Sortable columns: `levels`, `minigames`, `quests`, `dailyquests`, `miopoint` (default). Order defaults to `desc`. |
| `menu exit` | Return to Main. |

Columns shown: username, last level reached, minigames won, daily quests done, other quests done, best miopoint.

---

## Design Decisions Left to Us

The doc explicitly leaves many details to the team. This section documents **our** choices so a TA can grade them precisely.

### Levels & Chapters

The adventure has **4 chapters**, each with **4 levels** in a fixed order: an ordinary level, two special levels, then a boss level (**boss levels are Phase 2**, so they are placeholders here). Chapters unlock in order — you must finish a chapter to open the next one (or use the [`unlock-chapters` cheat](#cheat-commands)).

Enter a chapter at its furthest-unlocked level:

```
menu enter chapter -c <chaptername>
```

Or jump straight to a specific level (only if it is unlocked, or after the unlock cheat):

```
menu enter chapter -c <chaptername> -l <level>
```

**Full level list:**

| Chapter | L1 | L2 | L3 | L4 | Extra zombies |
|---------|----|----|----|----|---------------|
| **Egypt** | Ordinary | Conveyor Belt | Locked Plants | Boss *(P2)* | Ra, Explorer, Tomb Raiser, Pharaoh, Camel |
| **Frost Bite** | Ordinary | Save Our Seeds | Timed War | Boss *(P2)* | Dodo, Hunter, Troglobite, Weasel Hoarder |
| **Wavey Beach** | Ordinary | Dead Line | Love Your Plants | Boss *(P2)* | Fisherman, Octopus, Snorkel, Surfer, Fast Swimmer |
| **Dark Ages** | Ordinary | Night Ops | Plant What You Get | Boss *(P2)* | Juggler, Wizard, King, Imp Dragon |

Chapter names accept any spacing/casing: `Egypt`, `Frost Bite`/`frostbite`, `Wavey Beach`/`waveybeach`, `Dark Ages`/`darkages`.

**Difficulty formula per level (our choice, doc allows it):** a level has `2 + levelNumber` waves; the first wave's zombie-cost budget is `100 + 100 × levelNumber`; each subsequent wave is ×1.25, and the final wave is ×2 of the previous one (per the doc).

**Environment effects we added:**
- *Egypt:* graves block straight shots (700 HP); on the final wave some zombies enter via a whirlwind, 1–4 columns ahead.
- *Frost Bite:* two frozen zombies start on the lawn; each new wave an icy wind may add a freeze level to plants in some rows (3 levels = fully frozen, 600 HP ice); slider tiles push zombies up/down a lane.
- *Wavey Beach:* the tide shifts each wave, changing how many right columns are water and sweeping away land plants left on water.
- *Dark Ages:* night (no sky sun); each wave spawns random graves (some carrying 50 sun or a plant food) and necromancy may raise a zombie from a grave.

### Special Levels

All 8 special types from the doc are implemented (each appears at least once across the chapters):

| Special | Our rule |
|---------|----------|
| Conveyor Belt | No plant selection; a random plant arrives on the belt every 12s (first one on entry). |
| Locked Plants | Some selection slots are locked (every other unlocked plant is unavailable). |
| Save Our Seeds | Protected Wall-nuts are pre-placed on rows 2 & 4; losing one loses the level. |
| Timed War | Kill 12 zombies before a 120s timer runs out. |
| Night Ops | No sky sun — survive on plant-produced sun only. |
| Dead Line | A vertical line near the house; any zombie crossing it loses the level instantly. |
| Love Your Plants | Lose the level if you lose 5 plants. |
| Plant What You Get | Start with 800 sun, no more falls, and Sunflowers are unavailable. |

### Quests

The **20 quests from `Quests.csv`** are implemented with real reward payouts (coins / diamonds / seed packets / plant unlocks) and event tracking. Daily quests reset per calendar day; their variable part (`sun_amount`, empty column/row, restricted family, specialist plant) is **derived from the date** so it is stable for the whole day and identical for everyone.

Pages by priority:

- **critical:** story quests (complete first level of each chapter) + "Defense master: finish a level with exactly 0 sun" → 200 gems.
- **high:** epic + daily gem/packet quests — Only Cactus, Plant specialist, Economical gardener, Blooming in limits, Night or morning, Cloudy day, One column less, Defenseless row, Defenseless cross, Hunter of each chapter.
- **daily:** Daily sun collector, Quick trigger, Demolition expert, Symmetry, No OCD, Family killer, Almost winner, Back-to-back, Buy the daily offer.

Reward types follow the doc's three categories: **Currency** (coins/gems), **Unlockable** (a plant/level goes Locked→Available), **Inventory** (seed packets).

### Scoring Game (Miopoints)

Miopoint = score. We defined **5 scoring patterns** (doc requires ≥5), on top of a base of 10 per kill:

1. **Multi-kill** — several zombies dying in the same tick (+50).
2. **Speed kill** — a zombie dies within 5s of spawning (+20).
3. **Mass kill** — 4+ zombies die within one second (+100).
4. **Streak** — 5 kills without losing a plant (+75).
5. **Untouched defense** — end-of-game bonus per unused lawnmower (+150 each).

The daily zombie stream uses a date-seeded RNG, so the run is the same for every player that day.

### Minigames

All 5 minigames run at 3 escalating stages (`-d 1|2|3`). The command set and the free parameters below are **our** choices, as the doc allows.

**Vasebreaker** — no plant selection, no sky sun; everything comes from vases. Break a vase with `break vase -l (<x>, <y>)`. A vase may be empty, hide a zombie (Normal/Cone/Bucket, plus one guaranteed Gargantuar vase and one guaranteed plant vase), or drop a seed-packet you must plant before it fades (30s). Win by clearing all vases and surviving.

**Wallnut Bowling** — a conveyor belt hands you bowling nuts (Wall-nut / Explode-o-nut / Tall-nut). Plant one from behind the red line (columns 1–3) with `plant plant`; it rolls right, bounces 90° off zombies and the top/bottom walls, deals ~one-normal-zombie of damage per hit. Explode-o-nut blows a 3×3 on first contact; Tall-nut (giant) crushes straight through.

**I, Zombie** — you play the **zombie** side. Plants are pre-placed on the left; a brain sits at the end of each row. Spend sun to place zombies right of the red line (columns 6–9) with `place zombie -t <type> -l (<x>, <y>)`. Each level offers 5 of a 10-zombie roster (any two levels differ by at least one). Win by eating every brain; lose if you run out of zombies and sun. Zombie prices and the per-level roster are our choice.

**Beghouled** — a full 5×9 board of 5 plant types; zombies never stop coming. `swap -l (<x1>, <y1>) (<x2>, <y2>)` swaps two adjacent plants, but only if it forms a 3-in-a-row match. Matches clear, cascade, and grant sun (50 per match, more for 4/5-lines and cascades). Spend sun on `upgrade -t <type>` to convert every plant of a type into the next tier. A zombie eating a plant leaves a permanent crater. Win by making the target number of matches (grows per stage). Upgrade choices & win target are ours.

**Zombotany** — a normal adventure level, but some zombies have plant powers: Peashooter Zombie (shoots left), Wall-nut Zombie (tanky/slow), Jalapeno Zombie (burns its lane after 10s), Squash Zombie (fast, crushes the first plant and itself). Everything else (selection→win/lose) is a normal level.

**Beghouled upgrade paths (our mapping / cost in sun):**

| From | To | Cost |
|------|-----|------|
| Peashooter | Repeater | 500 |
| Repeater | Mega Gatling Pea | 1500 |
| Wall-nut | Tall-nut | 500 |
| Puff-shroom | Fume-shroom | 250 |
| Cabbage-pult | Melon-pult | 1000 |
| Melon-pult | Winter Melon | 750 |

### Plant Upgrades

Plant levels (1–5) come from collecting seed packets and paying coins in the Collection menu. The per-level bonuses (`HP +`, `Cost −`, `Damage +`, `Recharge −`) are read from the **`plants.csv` upgrade columns** (see `PlantUpgrades.java`) and applied to the plant's runtime cost, HP, damage and recharge during battle. Each plant food effect is plant-specific (e.g. Kernel-pult butters every zombie, Melon-pult drops giant melons, Repeater/Pea Pod fire a 20× giant pea, Chomper devours 3, Caulipower hypnotizes 3, Magnet-shroom strips all metal armor, …).

### Difficulty

Difficulty `dl` (1–5, default 3) scales the game by the doc's `dl/3` factor:
- Zombie **health** ×`dl/3`
- Zombie **damage** ×`dl/3`
- Game **speed** (zombie movement) ×`dl/3`
- Sun **drop interval** ×`dl/3` (higher difficulty = sun falls less often)
- Wave **cost budget** ×`3/dl` for the zombie side is applied via the wave cost factor.

### Economy Numbers

| Thing | Value |
|-------|-------|
| Starting sun (normal level) | 50 |
| New plant purchase | 2000 coins |
| Plant upgrade | 1000 coins + 5 seed packets |
| Pot | 2000 coins |
| Greenhouse Marigold harvest | 500 coins |
| Grow speed-up | 1 diamond / remaining hour |
| Zombie drop on death | 10% chance of 1 diamond / 50 coins / 1 pot |
| Glowing zombie chance | 5% (drops a plant food, max 3 held) |
| Level win reward | `100 + 50 × levelNumber` coins |
| Minigame win reward | 200 coins |

---

## Cheat Commands

Available in the relevant menus for testing and for demos.

### Progression cheats (Main menu) — for a quick presentation

These let you reach any locked part of the game instantly, so you don't have to play through everything before a demo.

| Command | Effect |
|---------|--------|
| `menu cheat unlock-all` | **The one-shot demo cheat.** Unlocks every chapter & level, unlocks all plants at max level, and grants 1,000,000 coins + 1,000,000 diamonds + 15 pots + 3 plant foods. |
| `menu cheat unlock-chapters` | Unlocks all 4 chapters and all of their levels (including the boss levels). |
| `menu cheat unlock-plants` | Unlocks every plant defined in the game. |
| `menu cheat max-plants` | Sets every plant to max level (5). |
| `menu cheat rich` | Grants 1,000,000 coins & diamonds, 15 pots and 3 plant foods. |
| `menu cheat add <n> <coin\|diamond>` | Adds a specific amount of coins or diamonds. |

After `unlock-chapters` (or `unlock-all`) you can jump directly to any level:

```
menu enter chapter -c <chaptername> -l <level>      # e.g. menu enter chapter -c Dark Ages -l 3
```

> **Typical demo flow:** `menu cheat unlock-all` → `menu enter chapter -c <any> -l <any>` → play. No grinding required.

### In-battle cheats

| Command | Effect |
|---------|--------|
| `cheat add -n <count> suns` | Adds sun. |
| `cheat add-plant-food` | Grants 1 plant food (max 3). |
| `cheat remove-cooldown` | Removes all plant recharge limits. |
| `cheat spawn-zombie -t <type> -l <x, y>` | Spawns a zombie at a tile. |
| `release the nuke` | Kills every zombie on the map. |
