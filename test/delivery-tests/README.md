# Phase-1 delivery test cases (تست‌های تحویل فاز ۱)

این پوشه یک مجموعه‌ی **جدید** از تست‌کیس‌هاست که مخصوص **جلسه‌ی تحویل فاز ۱ به TA**
ساخته شده است. تست‌های قبلی پروژه در `test/testcases.txt` دست‌نخورده باقی مانده‌اند؛
این پوشه چیزی از آن‌ها را تغییر نمی‌دهد.

هر تست‌کیس **یک فایل `.txt` جداگانه** است و دقیقاً به **یک ردیف از شیت نمره‌دهی فاز ۱**
وصل شده است (نام آن ردیف در هدر فایل، جلوی `Rubric (Phase 1)` نوشته شده).

---

## چیدمان پوشه‌ها

| پوشه | بخش شیت نمره‌دهی |
|------|-------------------|
| `01-infrastructure/` | زیرساخت (github tag، dependency management، معماری مشخص) |
| `02-menus/` | شروع بازی و منوها (ثبت‌نام، ورود، اصلی، پروفایل، اخبار، تنظیمات، بازی، کلکسیون) |
| `03-quests/` | کوئست‌ها (ترول‌لاگ، عملکرد کوئست، جایزه، اولویت) |
| `04-levels/` | مراحل adventure و ۸ مرحله‌ی ویژه |
| `05-core-mechanics/` | مکانیزم‌های ثابت بازی (گذر زمان، خورشید، موج‌ها، خوردن، باغ، انتخاب گیاه، برد/باخت) |
| `06-zombies/` | زامبی‌ها + پوشه‌ی `all-zombies/` با **یک تست برای هر ۳۸ زامبی** |
| `07-plants/` | گیاهان + پوشه‌ی `all-plants/` با **یک تست برای هر ۶۹ گیاه** |
| `08-greenhouse/` | گلخانه |
| `09-shop/` | فروشگاه |
| `10-score-and-leaderboard/` | لیدربورد و بازی امتیازی |
| `11-minigames/` | هر ۵ مینی‌گیم |
| `12-user-experience/` | UX، لینتر و میزان تغییرات گیت |

---

## قالب یک فایل تست

```
=== TEST TC-0201-1: a valid register creates the account
#
# Rubric (Phase 1) : شروع بازی و منوها > منوی ثبت‌نام > قابلیت ساخت کاربر جدید
# Group            : 02 - Game start & menus
# Kind             : automatic (deterministic)
# Goal             : ...
# ---------------------------------------------------------------------------
> register -u tester -p Abcd123! Abcd123! -n Tester -e tester@mail.com -g male
? Validation passed. Please pick a security question.
> pick question -q 1 -a blue -c blue
? Account created successfully. Please log in.
```

| نشانه | معنی |
|-------|------|
| `>`   | خطی که در بازی تایپ می‌شود |
| `?`   | زیررشته‌ای که **باید** در خروجی بیاید (ترتیب مهم است) |
| `#`   | توضیح برای TA (اجراکننده نادیده می‌گیرد) |
| `>> RESTART` | بازی بسته و **روی همان پوشه‌ی data دوباره باز** می‌شود (برای تست `stay logged in` و «نگه داشتن پیشرفت») |

### تست‌های تصادفی

بخش‌هایی از بازی تصادفی‌اند: اسپاون زامبی‌ها، جای قبرها، نوع خورشیدی که می‌افتد،
گیاه گلخانه، محتوای کوزه‌ها و … . در این تست‌ها:

* فقط چیزی assert می‌شود که **قطعی** است (مثلاً خودِ پیام موج)،
* در هدر فایل نوشته شده `Kind: automatic (RANDOM: ...)`,
* و در انتهای فایل یک بخش **`SAMPLE RUN`** هست که **خروجی واقعی اجرا شده روی همین
  پروژه** را نشان می‌دهد تا TA بداند شکل درست خروجی چیست.

مثال (از `05-core-mechanics/TC-0504-1_...`):

```
# SAMPLE RUN - real output recorded from this project. This part of the game
# is random, so your numbers/positions will differ; the SHAPE must match.
#   New special sun is dropping at position (8, 5)
#   Sun reached the ground at position (8, 5)
#   New normal sun is dropping at position (7, 1)
```

### تست‌های دستی

چند تست (تگ گیت، gradle، SHA-256 روی فایل، لینتر، UX) با اجرای خودکار قابل بررسی
نیستند. آن‌ها هیچ خط `>` ندارند و به‌جایش `# STEPS` و `# EXPECTED RESULT` دارند.
اجراکننده آن‌ها را با برچسب **MANUAL** فهرست می‌کند.

---

## اجرا

```bash
python test/delivery-tests/run_delivery_tests.py
```

```bash
python test/delivery-tests/run_delivery_tests.py 05
```

```bash
python test/delivery-tests/run_delivery_tests.py TC-0201-1 -v
```

* بدون آرگومان: کل مجموعه.
* با شماره‌ی گروه (`02`, `07`, …): فقط آن پوشه.
* با شناسه‌ی تست: فقط همان تست؛ `-v` کل خروجی بازی را هم چاپ می‌کند.

هر تست در یک پوشه‌ی `data/` موقت و مستقل اجرا می‌شود، پس نه روی سیو واقعی شما اثر
می‌گذارد و نه تست‌ها روی هم اثر می‌گذارند. اگر یک تست به‌خاطر تصادفی‌بودن بازی رد شود،
اجراکننده خودش یک‌بار دیگر امتحان می‌کند.

---

## وضعیت فعلی

همه‌ی تست‌های خودکار این پوشه روی کد فعلی پروژه **پاس می‌شوند**.

| گروه | خودکار | دستی |
|------|:------:|:----:|
| 01 - Infrastructure | 0 | 9 |
| 02 - Menus | 71 | 4 |
| 03 - Quests | 31 | 0 |
| 04 - Levels | 40 | 0 |
| 05 - Core mechanics | 108 | 0 |
| 06 - Zombies (+38 per-zombie) | 56 | 0 |
| 07 - Plants (+69 per-plant) | 94 | 0 |
| 08 - Greenhouse | 29 | 0 |
| 09 - Shop | 17 | 0 |
| 10 - Score & leaderboard | 25 | 1 |
| 11 - Minigames | 37 | 0 |
| 12 - User experience | 14 | 3 |

---

## نکته برای جلسه‌ی تحویل

سریع‌ترین مسیر دمو این است:

```bash
python test/delivery-tests/run_delivery_tests.py
```

و بعد برای هر ردیفی از شیت که TA پرسید، فایل مربوط به آن ردیف را باز کنید و
دستورهای `>` را دستی داخل بازی تایپ کنید — همان خروجی‌های `?` باید بیاید.
