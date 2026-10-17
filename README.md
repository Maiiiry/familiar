<div align="center">

<img src="docs/logo.png" width="96" alt="Familiar logo" />

# Familiar

### A familiar that feeds on stories

*A reading tracker with a pixel-art pet. Every page you read is mana; your familiar eats it, hatches, grows and learns. Free, no ads, no account.*

[![Android CI](https://github.com/bigdevwhale/familiar/actions/workflows/android.yml/badge.svg)](https://github.com/bigdevwhale/familiar/actions/workflows/android.yml)
[![Release](https://img.shields.io/github/v/release/bigdevwhale/familiar?color=AE93FF&label=download&logo=android)](https://github.com/bigdevwhale/familiar/releases/latest)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-15121C?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Languages](https://img.shields.io/badge/lang-EN%20%7C%20RU-AE93FF)
[![License: MIT](https://img.shields.io/badge/license-MIT-15121C)](LICENSE)

**English** · [Русский](README.ru.md) · [Changelog](CHANGELOG.md)

<img src="docs/screenshots/overview.png" alt="Familiar on a phone: the den with a kitsune, the reading candle, the feast after a sitting and the bestiary" width="100%" />

</div>

---

## The idea

You are a reading mage, and every mage needs a familiar. Yours lives on mana, and mana comes from pages. Read and it hatches from an egg, grows through five stages and changes with what you read. Forget about books and it slowly turns to stone, from the paws up, until a few pages break the spell.

## The familiars

<img src="docs/familiars.png" alt="The eight familiars: mimic, kitsune, slime, dragon, phoenix, griffin, owlbear and moon moth" width="100%" />

All the art is pixel art painted by the app itself (shaded shapes, hue-shifted ramps, dither, rim light), so every familiar shows what *you* read:

| | Familiar | Loves | Grows |
|---|---|---|---|
| 📦 | **Mimic** | finishing a book | from a little chest to a chest of books with a crown jewel |
| 🦊 | **Kitsune** | a new genre after the last one | a tail in each finished genre's colour, up to nine |
| 🟢 | **Slime** | things from books | swallows one thing you name after a chapter (a sword, a letter…), and you see it inside |
| 🐉 | **Dragon** | long books | sits on a hoard of the books you finished |
| 🔥 | **Phoenix** | coming back after a pause | never turns to stone: burns to ash and rises again |
| 🦅 | **Griffin** | reading streaks | its wings spread wider every stage |
| 🦉 | **Owlbear** | non-fiction and history | spectacles, then a scholar's mantle |
| 🦋 | **Moon moth** | reading after ten at night | a star on its wings for every night of reading |

Three can hatch at the start; the other five are found by reading (a 400-page book, five late nights, seven days in a row, a non-fiction or history book, a comeback after a pause).

## What's inside

| | Feature | How it works |
|---|---|---|
| 🕯️ | **The ritual** | A candle that burns down while you read, a big "turn the page" button with a rustle, a synthesised fireplace or rain. Lay the phone face down and the familiar dozes on your lap; quiet time feeds a bonus. |
| ✍️ | **Log pages** | Read on the train? Tell it where you stopped and it eats those pages too. |
| 🍖 | **The feast** | After each sitting: pages, candlelight, stillness, the familiar's favourite, a finished book. Too many pages in too little time and it sniffs at them suspiciously. |
| 🎭 | **Moods** | After horror it trembles, after romance it blushes, after a mystery it squints, after non-fiction it puts on spectacles. |
| 🔎 | **Find a book** | Search Open Library (Google Books as a fallback) by title or author: the title, author, pages and the real cover fill in. Covers are kept on the phone. Or type it all by hand and get a cover drawn in the genre's colour. |
| 🌳 | **Tree of stories** | Every finished book grows a glowing fruit in its genre's colour. Tap one to open the book. |
| ⚔️ | **The guild** | Three monthly errands, the same for every reader, none of them a must. |
| 📖 | **Saga** | Your year of reading told as a legend, ready to share. |
| 🔔 | **Reminder and widget** | An evening line from your familiar, quiet on days you've read; a home-screen widget with the familiar and today's pages. |

## Download

Get **Familiar.apk** from the [latest release](https://github.com/bigdevwhale/familiar/releases/latest) and open it on your phone. Android 8.0 or newer.

## Privacy

No account, no ads, no analytics. The app goes online only when you search for a book and to download its cover; your reading stays on the phone unless you export a backup.

## Build

```bash
./gradlew testDebugUnitTest     # unit tests
./gradlew assembleDebug         # app.familiar.dev, installs next to a release
./gradlew assembleDebug -Pdemo  # app.familiar.demo with ten weeks of made-up reading, for screenshots
./gradlew testDebugUnitTest -Prender --tests '*RenderArt*'   # sprite sheets to app/build/art, launcher icons
```

The sprites live in `app/src/main/java/app/familiar/ui/pix`: `Pix.kt` is the shading toolkit, `Sprites.kt` holds the eight familiars and their eggs, `Scenes.kt` the candle and the tree.

## License

[MIT](LICENSE). Part of [Stillcraft](https://bigdevwhale.github.io/stillcraft/).
