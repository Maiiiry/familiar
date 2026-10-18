package app.familiar.data

import app.familiar.domain.Feast
import app.familiar.domain.Genre
import app.familiar.domain.Mana
import app.familiar.domain.Species
import app.familiar.domain.isNight
import java.time.Instant
import java.time.ZoneId

/** The writes that feed the familiar: a sitting with a book, a guild reward. */
object Library {
    const val SHINY_ODDS = 16

    class Result(val reading: Reading, val feast: Feast, val petId: Long, val manaBefore: Int)

    /** Records reading [book] up to [toPage] and feeds the active familiar. */
    suspend fun sit(
        dao: FamiliarDao,
        book: Book,
        fromPage: Int,
        toPage: Int,
        start: Long,
        end: Long,
        timed: Boolean,
        stillSec: Int = 0,
        item: String = "",
        finish: Boolean = false,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Result? {
        val pet = dao.activePet() ?: dao.pets().firstOrNull() ?: return null
        val sp = Species.of(pet.species)
        val to = toPage.coerceIn(0, book.cap)
        val done = finish || (book.pages > 0 && to >= book.pages)
        val past = dao.readings()
        val books = dao.books().associateBy { it.id }
        val previousGenre = past.filter { it.bookId != book.id }.maxByOrNull { it.end }?.let { books[it.bookId] }?.let { Genre.of(it.genre) }
        val lastEnd = past.filter { it.pages > 0 }.maxOfOrNull { it.end }
        val gapDays = lastEnd?.let { ((start - it) / 86_400_000L).toInt() } ?: 0
        val days = (past.filter { it.pages > 0 }.map { it.end } + end).map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }.toSet()
        var d = Instant.ofEpochMilli(end).atZone(zone).toLocalDate()
        var streak = 0
        while (d in days) { streak++; d = d.minusDays(1) }
        val minutes = ((end - start) / 60_000).toInt()
        val feast = Mana.feast(
            sp,
            Mana.Sitting(
                pages = (to - fromPage).coerceAtLeast(0), timed = timed, minutes = minutes,
                stillFraction = if (end > start) stillSec * 1000f / (end - start) else 0f,
                night = isNight(start, zone), genre = Genre.of(book.genre), previousGenre = previousGenre,
                bookPages = book.pages, streak = streak, gapDays = gapDays, finishedBook = done && book.shelfOf != Shelf.DONE,
                item = item,
            ),
        )
        val reading = Reading(
            bookId = book.id, petId = pet.id, start = start, end = end, fromPage = fromPage, toPage = to, mana = feast.total,
            timed = timed, stillSec = stillSec, item = item.trim(), finishedBook = done && book.shelfOf != Shelf.DONE,
        )
        val id = dao.insertReading(reading)
        dao.updateBook(
            book.copy(
                page = if (done && book.pages > 0) book.pages else maxOf(book.page, to),
                shelf = if (done) Shelf.DONE.name else Shelf.READING.name,
                started = book.started ?: start,
                finished = if (done) (book.finished ?: end) else book.finished,
            )
        )
        dao.feed(pet.id, feast.total)
        return Result(reading.copy(id = id), feast, pet.id, pet.mana)
    }

    suspend fun reward(dao: FamiliarDao, mana: Int) {
        dao.activePet()?.let { dao.feed(it.id, mana) }
    }

    /** Brings a familiar into the den (as an egg) and makes it the one you read with. */
    suspend fun adopt(dao: FamiliarDao, sp: Species, name: String, now: Long = System.currentTimeMillis()): Long {
        val existing = dao.pets().firstOrNull { it.species == sp.name }
        // One egg in sixteen hatches in a rare colouring.
        val id = existing?.id ?: dao.insertPet(Pet(species = sp.name, name = name, joined = now, shiny = kotlin.random.Random.nextInt(SHINY_ODDS) == 0))
        dao.activate(id)
        return id
    }
}
