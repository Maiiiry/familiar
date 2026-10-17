package app.familiar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.familiar.R
import app.familiar.data.Shelf
import app.familiar.domain.Genre
import app.familiar.domain.Part
import app.familiar.domain.Quest
import app.familiar.domain.Species
import app.familiar.domain.Stage
import app.familiar.domain.Stats
import app.familiar.domain.Unlocks
import app.familiar.ui.pix.Look
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun locale(): Locale = LocalConfiguration.current.locales[0]

fun genreColor(g: Genre): Color = Color(g.argb)

@Composable
fun genreName(g: Genre): String = stringResource(
    when (g) {
        Genre.FANTASY -> R.string.g_fantasy
        Genre.SCIFI -> R.string.g_scifi
        Genre.MYSTERY -> R.string.g_mystery
        Genre.HORROR -> R.string.g_horror
        Genre.ROMANCE -> R.string.g_romance
        Genre.ADVENTURE -> R.string.g_adventure
        Genre.CLASSIC -> R.string.g_classic
        Genre.HISTORY -> R.string.g_history
        Genre.NONFICTION -> R.string.g_nonfiction
        Genre.POETRY -> R.string.g_poetry
        Genre.OTHER -> R.string.g_other
    }
)

@Composable
fun speciesName(sp: Species): String = stringResource(
    when (sp) {
        Species.MIMIC -> R.string.sp_mimic
        Species.KITSUNE -> R.string.sp_kitsune
        Species.SLIME -> R.string.sp_slime
        Species.DRAGON -> R.string.sp_dragon
        Species.PHOENIX -> R.string.sp_phoenix
        Species.GRIFFIN -> R.string.sp_griffin
        Species.OWLBEAR -> R.string.sp_owlbear
        Species.MOTH -> R.string.sp_moth
    }
)

@Composable
fun speciesLore(sp: Species): String = stringResource(
    when (sp) {
        Species.MIMIC -> R.string.lore_mimic
        Species.KITSUNE -> R.string.lore_kitsune
        Species.SLIME -> R.string.lore_slime
        Species.DRAGON -> R.string.lore_dragon
        Species.PHOENIX -> R.string.lore_phoenix
        Species.GRIFFIN -> R.string.lore_griffin
        Species.OWLBEAR -> R.string.lore_owlbear
        Species.MOTH -> R.string.lore_moth
    }
)

/** What it loves to eat: its mana bonus. */
@Composable
fun speciesLoves(sp: Species): String = stringResource(
    when (sp) {
        Species.MIMIC -> R.string.loves_mimic
        Species.KITSUNE -> R.string.loves_kitsune
        Species.SLIME -> R.string.loves_slime
        Species.DRAGON -> R.string.loves_dragon
        Species.PHOENIX -> R.string.loves_phoenix
        Species.GRIFFIN -> R.string.loves_griffin
        Species.OWLBEAR -> R.string.loves_owlbear
        Species.MOTH -> R.string.loves_moth
    }
)

/** How it's found. */
@Composable
fun speciesHow(sp: Species): String = when (sp) {
    Species.DRAGON -> stringResource(R.string.how_dragon, Unlocks.DRAGON_PAGES)
    Species.MOTH -> pluralStringResource(R.plurals.how_moth, Unlocks.MOTH_NIGHTS, Unlocks.MOTH_NIGHTS)
    Species.PHOENIX -> stringResource(R.string.how_phoenix)
    Species.GRIFFIN -> pluralStringResource(R.plurals.how_griffin, Unlocks.GRIFFIN_STREAK, Unlocks.GRIFFIN_STREAK)
    Species.OWLBEAR -> stringResource(R.string.how_owlbear)
    else -> pluralStringResource(R.plurals.how_starter, Unlocks.STARTERS_BOOKS, Unlocks.STARTERS_BOOKS)
}

@Composable
fun stageName(s: Stage): String = stringResource(
    when (s) {
        Stage.EGG -> R.string.st_egg
        Stage.BABY -> R.string.st_baby
        Stage.YOUNG -> R.string.st_young
        Stage.ADULT -> R.string.st_adult
        Stage.ANCIENT -> R.string.st_ancient
    }
)

@Composable
fun shelfName(s: Shelf): String = stringResource(
    when (s) {
        Shelf.READING -> R.string.shelf_reading
        Shelf.WANT -> R.string.shelf_want
        Shelf.DONE -> R.string.shelf_done
        Shelf.PAUSED -> R.string.shelf_paused
    }
)

@Composable
fun partName(p: Part, sp: Species?): String = when (p) {
    Part.PAGES -> stringResource(R.string.part_pages)
    Part.RITUAL -> stringResource(R.string.part_ritual)
    Part.STILL -> stringResource(R.string.part_still)
    Part.FAVOURITE -> stringResource(R.string.part_favourite, sp?.let { speciesLoves(it) } ?: "")
    Part.BOOK_DONE -> stringResource(R.string.part_book)
    Part.ITEM -> stringResource(R.string.part_item)
    Part.SNIFF -> stringResource(R.string.part_sniff)
}

@Composable
fun questTitle(q: Quest): String = stringResource(
    when (q) {
        Quest.STREAK -> R.string.q_streak
        Quest.PAGES -> R.string.q_pages
        Quest.TWO_BOOKS -> R.string.q_two_books
        Quest.LONG_BOOK -> R.string.q_long_book
        Quest.NEW_GENRE -> R.string.q_new_genre
        Quest.NIGHTS -> R.string.q_nights
        Quest.HOUR -> R.string.q_hour
        Quest.STILL -> R.string.q_still
    }
)

@Composable
fun questText(q: Quest): String = when (q) {
    Quest.STREAK -> pluralStringResource(R.plurals.qt_streak, q.target, q.target)
    Quest.PAGES -> pluralStringResource(R.plurals.qt_pages, q.target, q.target)
    Quest.TWO_BOOKS -> pluralStringResource(R.plurals.qt_books, q.target, q.target)
    Quest.LONG_BOOK -> stringResource(R.string.qt_long_book)
    Quest.NEW_GENRE -> stringResource(R.string.qt_new_genre)
    Quest.NIGHTS -> pluralStringResource(R.plurals.qt_nights, q.target, q.target)
    Quest.HOUR -> stringResource(R.string.qt_hour)
    Quest.STILL -> pluralStringResource(R.plurals.qt_still, q.target, q.target)
}

@Composable
fun pages(n: Int): String = pluralStringResource(R.plurals.pages, n, n)

@Composable
fun days(n: Int): String = pluralStringResource(R.plurals.days, n, n)

@Composable
fun books(n: Int): String = pluralStringResource(R.plurals.books, n, n)

@Composable
fun mana(n: Int): String = stringResource(R.string.mana_n, n)

@Composable
fun dateShort(t: Long): String = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale()).format(Instant.ofEpochMilli(t).atZone(ZoneId.systemDefault()))

@Composable
fun timeShort(t: Long): String = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale()).format(Instant.ofEpochMilli(t).atZone(ZoneId.systemDefault()))

/** The look of a familiar from what's been read so far. */
fun lookOf(sp: Species, mana: Int, s: Stats, activeOne: Boolean, sleeping: Boolean = false, chew: Boolean = false): Look {
    val stage = Stage.of(mana)
    return Look(
        species = sp,
        stage = stage,
        stone = if (activeOne) s.stone else 0f,
        mood = if (activeOne) s.mood else app.familiar.domain.Mood.NONE,
        tails = Genre.tails.filter { it in s.genresFinished }.map { it.argb.toInt() },
        hoard = s.finished.takeLast(16).map { Genre.of(it.genre).argb.toInt() },
        stars = s.nights,
        items = s.items.take(7),
        sleeping = sleeping,
        chew = chew,
        hatch = if (stage == Stage.EGG) (mana / Stage.BABY.from.toFloat()).coerceIn(0f, 1f) else 0f,
    ).let { if ((it.stone * 10).toInt() != 0) it.copy(stone = (it.stone * 10).toInt() / 10f) else it.copy(stone = 0f) }
}
