package app.familiar.domain

import app.familiar.data.Book
import app.familiar.data.Pet
import app.familiar.data.Reading
import app.familiar.data.Shelf
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.random.Random

/** A sitting started late in the evening or deep in the night. */
fun isNight(start: Long, zone: ZoneId): Boolean {
    val h = Instant.ofEpochMilli(start).atZone(zone).hour
    return h >= 22 || h < 5
}

/** Everything derived from the shelf, the sittings and the den at one moment. */
class Stats(
    val books: List<Book>,
    val readings: List<Reading>,
    val pets: List<Pet>,
    val now: Long,
    val zone: ZoneId,
    val dailyGoal: Int,
) {
    val today: LocalDate = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    private fun day(t: Long): LocalDate = Instant.ofEpochMilli(t).atZone(zone).toLocalDate()

    val readDays: Set<LocalDate> = readings.filter { it.counts }.map { day(it.end) }.toSet()
    val todayPages: Int = readings.filter { day(it.end) == today }.sumOf { it.amount }
    val fed: Float = if (dailyGoal <= 0) 1f else (todayPages.toFloat() / dailyGoal).coerceIn(0f, 1f)

    /** Days in a row with reading, counting today or (if not yet) up to yesterday. */
    val streak: Int = run {
        var d = if (today in readDays) today else today.minusDays(1)
        var n = 0
        while (d in readDays) { n++; d = d.minusDays(1) }
        n
    }
    val bestStreak: Int = longestRun(readDays)

    val lastRead: Long? = readings.filter { it.counts }.maxOfOrNull { it.end }

    /**
     * The spell of forgetting: nothing for two days and the familiar starts turning to stone,
     * a statue after a week. A new familiar (no sittings yet) is spared.
     */
    val stone: Float = lastRead?.let { ((now - it) / 3_600_000f - 48f) / 120f }?.coerceIn(0f, 1f) ?: 0f

    val finished: List<Book> = books.filter { it.shelfOf == Shelf.DONE }.sortedBy { it.finished ?: it.added }
    val genresFinished: Set<Genre> = finished.map { Genre.of(it.genre) }.toSet()
    val tails: Int = genresFinished.count { it != Genre.OTHER }.coerceIn(1, 9)
    val nights: Int = readings.count { it.counts && isNight(it.start, zone) }
    val totalPages: Int = readings.sumOf { it.pages }
    val totalMinutes: Int = readings.filter { it.timed }.sumOf { it.minutes }
    val items: List<String> = readings.filter { it.item.isNotBlank() }.sortedByDescending { it.end }.map { it.item }

    /** A sitting after five or more quiet days: the phoenix rises from that. */
    val cameBack: Boolean = readings.filter { it.counts }.sortedBy { it.start }.zipWithNext().any { (a, b) -> b.start - a.end >= 5 * 86_400_000L }

    val active: Pet? = pets.firstOrNull { it.active } ?: pets.firstOrNull()
    val activeSpecies: Species? = active?.let { Species.of(it.species) }
    val stage: Stage = Stage.of(active?.mana ?: 0)

    /** The last book read colours the mood for twelve hours. */
    val mood: Mood = readings.maxByOrNull { it.end }?.takeIf { now - it.end < 12 * 3_600_000L }
        ?.let { r -> books.firstOrNull { it.id == r.bookId } }?.let { Genre.of(it.genre).mood } ?: Mood.NONE

    val unlocked: Set<Species> = Unlocks.unlocked(this)
}

fun longestRun(days: Set<LocalDate>): Int {
    var best = 0
    for (d in days) {
        if (d.minusDays(1) in days) continue
        var n = 0
        var x = d
        while (x in days) { n++; x = x.plusDays(1) }
        best = maxOf(best, n)
    }
    return best
}

/** How each familiar is found. */
object Unlocks {
    const val DRAGON_PAGES = 400
    const val MOTH_NIGHTS = 5
    const val GRIFFIN_STREAK = 7
    const val STARTERS_BOOKS = 2

    fun unlocked(s: Stats): Set<Species> {
        val out = s.pets.map { Species.of(it.species) }.toMutableSet()
        if (s.finished.size >= STARTERS_BOOKS) out += Species.entries.filter { it.starter }
        if (s.finished.any { it.pages >= DRAGON_PAGES }) out += Species.DRAGON
        if (s.nights >= MOTH_NIGHTS) out += Species.MOTH
        if (s.cameBack) out += Species.PHOENIX
        if (s.bestStreak >= GRIFFIN_STREAK) out += Species.GRIFFIN
        if (s.finished.any { it.genre == Genre.NONFICTION.name || it.genre == Genre.HISTORY.name }) out += Species.OWLBEAR
        return out
    }

    /** How far along the way to [sp]: current and target. */
    fun progress(sp: Species, s: Stats): Pair<Int, Int> = when (sp) {
        Species.DRAGON -> (s.books.filter { it.shelfOf != Shelf.WANT }.maxOfOrNull { it.page } ?: 0).coerceAtMost(DRAGON_PAGES) to DRAGON_PAGES
        Species.MOTH -> s.nights.coerceAtMost(MOTH_NIGHTS) to MOTH_NIGHTS
        Species.PHOENIX -> (if (s.cameBack) 1 else 0) to 1
        Species.GRIFFIN -> s.bestStreak.coerceAtMost(GRIFFIN_STREAK) to GRIFFIN_STREAK
        Species.OWLBEAR -> s.finished.count { it.genre == Genre.NONFICTION.name || it.genre == Genre.HISTORY.name }.coerceAtMost(1) to 1
        else -> s.finished.size.coerceAtMost(STARTERS_BOOKS) to STARTERS_BOOKS
    }
}

enum class Part { PAGES, TIME, LISTEN, RITUAL, STILL, FAVOURITE, BOOK_DONE, ITEM, SNIFF }

class Feast(val parts: List<Pair<Part, Int>>) {
    val total: Int get() = parts.sumOf { it.second }.coerceAtLeast(0)
    val sniffed: Boolean get() = parts.any { it.first == Part.SNIFF }
}

/** What a sitting is worth to the familiar that eats it. */
object Mana {
    const val BOOK_DONE = 50
    const val MIMIC_FEAST = 100
    const val SLIME_ITEM = 15
    const val QUEST = 100

    /** Faster than this many pages a minute is more than a person reads; the rest counts half. */
    const val MAX_PACE = 2.5f

    /** Minutes counted for a ritual without a page, at most. */
    const val MAX_MINUTES = 180

    class Sitting(
        val pages: Int,
        val timed: Boolean,
        val minutes: Int,
        val stillFraction: Float,
        val night: Boolean,
        val genre: Genre,
        /** Genre of the book read before this one, or null. */
        val previousGenre: Genre?,
        val bookPages: Int,
        /** The streak including this sitting. */
        val streak: Int,
        /** Whole days since the sitting before, or 0. */
        val gapDays: Int,
        val finishedBook: Boolean,
        val item: String,
        val listened: Boolean = false,
    )

    fun feast(sp: Species, s: Sitting): Feast {
        val parts = mutableListOf<Pair<Part, Int>>()
        // No page given after a ritual: the familiar eats the minutes by the candle instead.
        if (s.timed && s.pages == 0 && s.minutes > 0) {
            parts += (if (s.listened) Part.LISTEN else Part.TIME) to s.minutes.coerceAtMost(MAX_MINUTES)
            if (s.finishedBook) parts += Part.BOOK_DONE to BOOK_DONE
            if (sp == Species.SLIME && s.item.isNotBlank()) parts += Part.ITEM to SLIME_ITEM
            return Feast(parts)
        }
        var pages = s.pages
        parts += Part.PAGES to s.pages
        if (s.timed) {
            val allowed = (s.minutes * MAX_PACE + 5).toInt()
            if (s.pages > allowed) {
                val cut = (s.pages - allowed) / 2
                parts += Part.SNIFF to -cut
                pages -= cut
            }
            if (pages > 0) parts += Part.RITUAL to (pages * 0.1f).toInt().coerceAtLeast(1)
            val still = (pages * 0.2f * s.stillFraction.coerceIn(0f, 1f)).toInt()
            if (still > 0) parts += Part.STILL to still
        }
        val loves = when (sp) {
            Species.KITSUNE -> s.previousGenre != null && s.previousGenre != s.genre
            Species.DRAGON -> s.bookPages >= Unlocks.DRAGON_PAGES
            Species.PHOENIX -> s.gapDays >= 3
            Species.GRIFFIN -> s.streak >= 3
            Species.OWLBEAR -> s.genre == Genre.NONFICTION || s.genre == Genre.HISTORY
            Species.MOTH -> s.night
            Species.MIMIC, Species.SLIME -> false
        }
        if (loves && pages > 0) parts += Part.FAVOURITE to (pages * if (sp == Species.PHOENIX) 1f else 0.5f).toInt().coerceAtLeast(1)
        if (s.finishedBook) {
            parts += Part.BOOK_DONE to BOOK_DONE
            if (sp == Species.MIMIC) parts += Part.FAVOURITE to MIMIC_FEAST
        }
        if (sp == Species.SLIME && s.item.isNotBlank()) parts += Part.ITEM to SLIME_ITEM
        return Feast(parts)
    }

}

/** Monthly guild errands: three from the board, the same three for everyone in that month. */
enum class Quest(val target: Int) {
    STREAK(5), PAGES(500), TWO_BOOKS(2), LONG_BOOK(1), NEW_GENRE(1), NIGHTS(3), HOUR(1), STILL(5);

    companion object {
        fun of(month: YearMonth): List<Quest> = entries.shuffled(Random(month.year * 12 + month.monthValue)).take(3)
    }
}

class QuestState(val quest: Quest, val progress: Int, val month: YearMonth) {
    val done: Boolean get() = progress >= quest.target
    val key: String get() = "$month:${quest.name}"
}

object Quests {
    fun state(s: Stats, month: YearMonth = YearMonth.from(s.today)): List<QuestState> {
        fun inMonth(t: Long) = YearMonth.from(Instant.ofEpochMilli(t).atZone(s.zone)) == month
        val rs = s.readings.filter { inMonth(it.end) }
        val doneNow = s.finished.filter { it.finished != null && inMonth(it.finished) }
        val monthStart = month.atDay(1).atStartOfDay(s.zone).toInstant().toEpochMilli()
        val oldGenres = s.finished.filter { (it.finished ?: 0) < monthStart }.map { it.genre }.toSet()
        return Quest.of(month).map { q ->
            val p = when (q) {
                Quest.STREAK -> longestRun(rs.filter { it.counts }.map { Instant.ofEpochMilli(it.end).atZone(s.zone).toLocalDate() }.toSet())
                Quest.PAGES -> rs.sumOf { it.amount }
                Quest.TWO_BOOKS -> doneNow.size
                Quest.LONG_BOOK -> doneNow.count { it.pages >= 300 }
                Quest.NEW_GENRE -> doneNow.count { it.genre !in oldGenres && it.genre != Genre.OTHER.name }
                Quest.NIGHTS -> rs.count { it.timed && it.counts && isNight(it.start, s.zone) }
                Quest.HOUR -> rs.count { it.timed && it.minutes >= 60 }
                Quest.STILL -> rs.count { it.stillSec >= 600 }
            }
            QuestState(q, p.coerceAtMost(q.target), month)
        }
    }
}

/** A year of reading, for the saga. */
class Year(private val s: Stats, val year: Int) {
    private fun inYear(t: Long) = Instant.ofEpochMilli(t).atZone(s.zone).year == year
    val books: List<Book> = s.finished.filter { it.finished != null && inYear(it.finished) }
    val pages: Int = s.readings.filter { inYear(it.end) }.sumOf { it.pages }
    val nights: Int = s.readings.count { inYear(it.end) && it.counts && isNight(it.start, s.zone) }
    val genre: Genre? = books.groupBy { Genre.of(it.genre) }.maxByOrNull { it.value.size }?.key
    val longest: Book? = books.maxByOrNull { it.pages }
    val bestStreak: Int = longestRun(s.readings.filter { inYear(it.end) && it.counts }.map { Instant.ofEpochMilli(it.end).atZone(s.zone).toLocalDate() }.toSet())
}

/** A cycle of books, gathered by the name typed on each volume. */
class Series(val name: String, val books: List<Book>) {
    val done: Int = books.count { it.shelfOf == Shelf.DONE }
    /** Every volume on the shelf is read, and there are at least two. */
    val complete: Boolean = books.size >= 2 && done == books.size
    val key: String get() = "series:" + name.lowercase()

    companion object {
        const val REWARD = 200

        fun all(books: List<Book>): List<Series> = books.filter { it.series.isNotBlank() }
            .groupBy { it.series.trim().lowercase() }
            .map { (_, list) -> Series(list.first().series.trim(), list.sortedWith(compareBy({ it.volume ?: Int.MAX_VALUE }, { it.added }))) }
            .sortedByDescending { s -> s.books.maxOf { it.finished ?: it.added } }
    }
}

/** Pages (and audiobook minutes) read on each day of [year]. */
fun daysOf(readings: List<Reading>, zone: ZoneId, year: Int): Map<LocalDate, Int> =
    readings.filter { Instant.ofEpochMilli(it.end).atZone(zone).year == year }
        .groupBy { Instant.ofEpochMilli(it.end).atZone(zone).toLocalDate() }
        .mapValues { (_, l) -> l.sumOf { it.amount } }
