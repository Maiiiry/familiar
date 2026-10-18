# Changelog

## [1.1.1] - 2026-10-19

- **Fixed: 1.1.0 didn't start after updating from 1.0.0.** The database changed without a new version. Updating now starts with a fresh database and a new egg; the books, sittings and familiars from 1.0.0 are not carried over.
- A book with no page count shows an empty field in the editor instead of 0, so it can be saved.
- The first book in onboarding can get its own cover from the gallery.

## [1.1.0] - 2026-10-19

- **Read or listen.** Any book can be switched between reading and its audio version, sitting by sitting: a gramophone turns instead of the candle, the minutes count by themselves, and the page in the text is optional. Listened sittings are marked with 🎧 in the book's chronicle.
- **Log reading** without the app by the page you reached, or just by minutes of reading or listening.
- **Minutes count like pages:** they feed the daily fill, the streak, nights, guild errands and the heatmap, so a day of listening keeps the chain.
- **Pages are optional:** a book's length can be left out, and a ritual can end without a page; the familiar then eats the minutes.
- **Your own shelves**, with books on as many as you like; rename or delete them any time.
- **Your own cover** from the gallery.
- The first book in onboarding gets its own author field.
- The yarn game is gone; stroking the familiar is sideways only, so the page scrolls over the den.
- A just-made shelf opens at once; a listening ritual has its own wording.

## [1.0.0] - 2026-10-17

The first release of Familiar: a reading tracker with a pixel-art familiar that feeds on stories.

- **Eight familiars** in rich pixel art painted on the phone: the mimic, kitsune and slime (the three you can hatch first), and the dragon, phoenix, griffin, owlbear and moon moth (found by reading). Five stages from egg to ancient, each familiar with its own favourite kind of reading. About one egg in sixteen hatches shining, in a rare colouring.
- **They show what you read:** the kitsune grows a tail for every genre, the dragon sits on a hoard of finished books, the moth's wings fill with a star for every night, the slime keeps what you named inside. Moods follow the last genre read.
- **The spell of forgetting:** two quiet days and the familiar starts turning to stone from the paws up; the phoenix burns to ash instead. A few pages break the spell.
- **The den:** a pixel room furnished with sparks (one for every mana ever eaten), sixteen pieces from a moonlit window to a bubbling cauldron. Stroke the familiar to make it purr, toss it yarn once a day; it sleeps after midnight.
- **The ritual:** a candle that burns while you read, a page counter with a rustle or the page at the end, a synthesised fireplace or rain, and a stillness bonus while the phone lies face down. Audiobooks count minutes with a gramophone. Or log pages read elsewhere.
- **Find a book** in Open Library (Google Books as a fallback): title, author, pages and the real cover, kept on the phone. Or a cover drawn in the genre's colour.
- **Cycles** of books with a reward for reading them all, the tree of stories, a goal for the year with pace, three monthly guild errands and the saga of your year with a square for every day.
- An evening reminder told by the familiar, a home-screen widget, JSON backup, English and Russian, parchment and candlelight themes.
