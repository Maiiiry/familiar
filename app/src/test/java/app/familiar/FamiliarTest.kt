package app.familiar

import app.familiar.content.Lines
import app.familiar.data.Book
import app.familiar.data.Pet
import app.familiar.data.Reading
import app.familiar.data.Shelf
import app.familiar.domain.Genre
import app.familiar.domain.Mana
import app.familiar.domain.Mood
import app.familiar.domain.Part
import app.familiar.domain.Quest
import app.familiar.domain.Quests
import app.familiar.domain.Species
import app.familiar.domain.Stage
import app.familiar.domain.Stats
import app.familiar.domain.Unlocks
import app.familiar.ui.pix.Glyph
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.Locale

class FamiliarTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 10, 17)
    private val now = today.atTime(20, 0).toInstant(zone).toEpochMilli()
    private val pet = Pet(id = 1, species = Species.KITSUNE.name, name = "Yuzu", joined = 0, mana = 500, active = true)

    private fun at(daysAgo: Int, hour: Int = 20) = today.minusDays(daysAgo.toLong()).atTime(hour, 0).toInstant(zone).toEpochMilli()

    private fun read(daysAgo: Int, pages: Int = 20, hour: Int = 20, book: Long = 1, item: String = "", still: Int = 0, minutes: Int = 30) =
        Reading(bookId = book, petId = 1, start = at(daysAgo, hour), end = at(daysAgo, hour) + minutes * 60_000L, fromPage = 0, toPage = pages, mana = pages, timed = true, item = item, stillSec = still)

    private fun book(id: Long, genre: Genre = Genre.FANTASY, pages: Int = 300, done: Int? = null) =
        Book(id = id, title = "B$id", pages = pages, genre = genre.name, shelf = if (done != null) Shelf.DONE.name else Shelf.READING.name, added = 0, finished = done?.let { at(it) })

    private fun stats(readings: List<Reading>, books: List<Book> = listOf(book(1)), pets: List<Pet> = listOf(pet)) =
        Stats(books, readings, pets, now, zone, 20)

    private fun sitting(pages: Int = 30, timed: Boolean = true, minutes: Int = 30, still: Float = 0f, night: Boolean = false, genre: Genre = Genre.FANTASY, prev: Genre? = null, bookPages: Int = 300, streak: Int = 1, gap: Int = 0, finished: Boolean = false, item: String = "") =
        Mana.Sitting(pages, timed, minutes, still, night, genre, prev, bookPages, streak, gap, finished, item)

    @Test
    fun stagesByMana() {
        assertEquals(Stage.EGG, Stage.of(0))
        assertEquals(Stage.EGG, Stage.of(19))
        assertEquals(Stage.BABY, Stage.of(20))
        assertEquals(Stage.YOUNG, Stage.of(300))
        assertEquals(Stage.ANCIENT, Stage.of(99_999))
        assertEquals(0.5f, Stage.progress(160), 0.01f)
        assertEquals(1f, Stage.progress(10_000), 0f)
    }

    @Test
    fun pagesAreMana() {
        val f = Mana.feast(Species.MIMIC, sitting(pages = 30, timed = false))
        assertEquals(30, f.total)
        assertFalse(f.sniffed)
    }

    @Test
    fun candleAndStillnessAddBonus() {
        val f = Mana.feast(Species.MIMIC, sitting(pages = 40, still = 1f))
        assertEquals(40 + 4 + 8, f.total)
    }

    @Test
    fun tooFastIsSniffed() {
        // 200 pages in 10 minutes: only 28 are believable, the rest count half.
        val f = Mana.feast(Species.MIMIC, sitting(pages = 200, minutes = 10))
        assertTrue(f.sniffed)
        assertTrue(f.total < 130)
        // Logged by hand there's no clock to doubt.
        assertFalse(Mana.feast(Species.MIMIC, sitting(pages = 200, timed = false)).sniffed)
    }

    @Test
    fun favourites() {
        fun fav(sp: Species, s: Mana.Sitting) = Mana.feast(sp, s).parts.any { it.first == Part.FAVOURITE }
        assertTrue(fav(Species.KITSUNE, sitting(genre = Genre.HORROR, prev = Genre.FANTASY)))
        assertFalse(fav(Species.KITSUNE, sitting(genre = Genre.FANTASY, prev = Genre.FANTASY)))
        assertTrue(fav(Species.DRAGON, sitting(bookPages = 500)))
        assertTrue(fav(Species.MOTH, sitting(night = true)))
        assertTrue(fav(Species.GRIFFIN, sitting(streak = 4)))
        assertTrue(fav(Species.OWLBEAR, sitting(genre = Genre.HISTORY)))
        assertTrue(fav(Species.PHOENIX, sitting(gap = 6)))
        assertTrue(fav(Species.MIMIC, sitting(finished = true)))
        assertEquals(Mana.SLIME_ITEM, Mana.feast(Species.SLIME, sitting(pages = 0, timed = false, item = "sword")).total)
    }

    @Test
    fun streakCountsUpToYesterday() {
        val s = stats(listOf(read(1), read(2), read(3), read(5)))
        assertEquals(3, s.streak)
        assertEquals(3, s.bestStreak)
        assertEquals(4, stats(listOf(read(0), read(1), read(2), read(3))).streak)
    }

    @Test
    fun stoneCreepsInAfterTwoDays() {
        assertEquals(0f, stats(listOf(read(1))).stone, 0f)
        val three = stats(listOf(read(3))).stone
        assertTrue(three > 0f && three < 0.5f)
        assertEquals(1f, stats(listOf(read(9))).stone, 0f)
        // A brand-new familiar with nothing read yet is spared.
        assertEquals(0f, stats(emptyList()).stone, 0f)
    }

    @Test
    fun tailsAreGenres() {
        val books = listOf(book(1, Genre.FANTASY, done = 3), book(2, Genre.HORROR, done = 2), book(3, Genre.OTHER, done = 1), book(4, Genre.HORROR, done = 1))
        assertEquals(2, stats(emptyList(), books).tails)
        assertEquals(1, stats(emptyList(), emptyList()).tails)
    }

    @Test
    fun moodFromLastBookFadesAfterTwelveHours() {
        val books = listOf(book(1, Genre.HORROR))
        assertEquals(Mood.SCARED, stats(listOf(read(0, hour = 19)), books).mood)
        assertEquals(Mood.NONE, stats(listOf(read(1, hour = 6)), books).mood)
    }

    @Test
    fun unlocks() {
        val s0 = stats(emptyList())
        assertEquals(setOf(Species.KITSUNE), s0.unlocked)
        val two = stats(emptyList(), listOf(book(1, done = 2), book(2, done = 1)))
        assertTrue(Species.MIMIC in two.unlocked && Species.SLIME in two.unlocked)
        assertTrue(Species.DRAGON in stats(emptyList(), listOf(book(1, pages = Unlocks.DRAGON_PAGES, done = 1))).unlocked)
        assertTrue(Species.MOTH in stats((1..5).map { read(it, hour = 23) }).unlocked)
        assertTrue(Species.PHOENIX in stats(listOf(read(9), read(1))).unlocked)
        assertTrue(Species.GRIFFIN in stats((1..7).map { read(it) }).unlocked)
        assertTrue(Species.OWLBEAR in stats(emptyList(), listOf(book(1, Genre.HISTORY, done = 1))).unlocked)
    }

    @Test
    fun guildBoardIsStableForAMonth() {
        val m = YearMonth.of(2026, 10)
        assertEquals(Quest.of(m), Quest.of(m))
        assertEquals(3, Quest.of(m).toSet().size)
        val s = stats((0..9).map { read(it, pages = 60, still = 700) })
        Quests.state(s, m).forEach { q -> assertTrue(q.progress in 0..q.quest.target) }
    }

    @Test
    fun linesFitAnyName() {
        for (sp in Species.entries) for (st in Lines.State.entries) for (l in listOf(Locale.ENGLISH, Locale.forLanguageTag("ru"))) {
            val line = Lines.line(l, sp, st, "Zed", 3)
            assertTrue("$sp $st $l", line.isNotBlank() && "{n}" !in line)
        }
    }

    @Test
    fun glyphsPickByWord() {
        assertEquals(Glyph.SWORD, Glyph.of("Старый меч"))
        assertEquals(Glyph.LETTER, Glyph.of("a letter from Mr. Darcy"))
        assertEquals(Glyph.of("something odd"), Glyph.of("something odd"))
    }
}
