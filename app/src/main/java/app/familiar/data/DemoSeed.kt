package app.familiar.data

import app.familiar.domain.Genre
import app.familiar.domain.Species
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlin.random.Random

/** Made-up history for screenshots (`-Pdemo` builds only): ten weeks of evenings with books. */
object DemoSeed {
    private class B(val en: String, val ru: String, val authorEn: String, val authorRu: String, val pages: Int, val genre: Genre, val coverId: Long = 0)

    private val done = listOf(
        B("The Hobbit", "Хоббит", "J. R. R. Tolkien", "Дж. Р. Р. Толкин", 310, Genre.FANTASY, 14627509),
        B("Murder on the Orient Express", "Убийство в «Восточном экспрессе»", "Agatha Christie", "Агата Кристи", 256, Genre.MYSTERY, 11100465),
        B("Dracula", "Дракула", "Bram Stoker", "Брэм Стокер", 418, Genre.HORROR, 12216503),
        B("Pride and Prejudice", "Гордость и предубеждение", "Jane Austen", "Джейн Остин", 432, Genre.ROMANCE, 14348537),
        B("Dune", "Дюна", "Frank Herbert", "Фрэнк Герберт", 612, Genre.SCIFI, 11481354),
        B("The Master and Margarita", "Мастер и Маргарита", "Mikhail Bulgakov", "Михаил Булгаков", 480, Genre.CLASSIC, 12947486),
        B("Sapiens", "Sapiens", "Yuval Noah Harari", "Юваль Ной Харари", 498, Genre.NONFICTION, 8634250),
    )
    private val reading = listOf(
        B("The Adventures of Sherlock Holmes", "Приключения Шерлока Холмса", "Arthur Conan Doyle", "Артур Конан Дойл", 307, Genre.MYSTERY, 6717853),
        B("The Name of the Wind", "Имя ветра", "Patrick Rothfuss", "Патрик Ротфусс", 662, Genre.FANTASY, 11480483),
    )
    private val want = listOf(
        B("The Three Musketeers", "Три мушкетёра", "Alexandre Dumas", "Александр Дюма", 704, Genre.ADVENTURE, 11929973),
        B("A Wizard of Earthsea", "Волшебник Земноморья", "Ursula K. Le Guin", "Урсула Ле Гуин", 264, Genre.FANTASY, 13617691),
        B("SPQR", "SPQR", "Mary Beard", "Мэри Бирд", 608, Genre.HISTORY, 7418788),
    )

    suspend fun seedIfEmpty(db: FamiliarDatabase, settings: SettingsStore) {
        val dao = db.dao()
        if (dao.pets().isNotEmpty()) return
        val ru = Locale.getDefault().language == "ru"
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val rnd = Random(7)
        val start = today.minusDays(74)
        fun at(d: LocalDate, h: Int, m: Int) = d.atTime(h, m).atZone(zone).toInstant().toEpochMilli()

        val fox = dao.insertPet(Pet(species = Species.KITSUNE.name, name = if (ru) "Юзу" else "Yuzu", joined = at(start, 20, 0), active = true))
        suspend fun insert(b: B, shelf: Shelf, added: LocalDate) = dao.insertBook(
            Book(title = if (ru) b.ru else b.en, author = if (ru) b.authorRu else b.authorEn, pages = b.pages, genre = b.genre.name, shelf = shelf.name, added = at(added, 19, 0),
                cover = if (b.coverId > 0) "https://covers.openlibrary.org/b/id/${b.coverId}-L.jpg" else ""),
        )
        want.forEachIndexed { i, b -> insert(b, Shelf.WANT, start.plusDays(i * 9L)) }

        // Pages a day that bring the last book to about page 420 today.
        val mean = (done.sumOf { it.pages } + 120 + 420) / (74 * 0.88f)
        var day = start
        val order = done + reading
        var moth = 0L
        for ((i, b) in order.withIndex()) {
            val id = insert(b, Shelf.READING, day)
            // Sherlock is put aside at page 120; the last book is read up to today.
            val stopAt = if (i == done.size) 120 else if (i == done.size + 1) b.pages - 1 else b.pages
            var page = 0
            while (page < stopAt && day <= today) {
                // A quiet day now and then.
                if (rnd.nextFloat() < 0.12f && day != today) { day = day.plusDays(1); continue }
                if (day == today.minusDays(9) && moth == 0L) {
                    moth = dao.insertPet(Pet(species = Species.MOTH.name, name = if (ru) "Луна" else "Luna", joined = at(day, 18, 0)))
                    dao.activate(moth)
                }
                if (day == today.minusDays(3) && moth != 0L) dao.activate(fox)
                val night = rnd.nextFloat() < 0.25f
                val h = if (night) 22 + rnd.nextInt(2) else 19 + rnd.nextInt(3)
                val read = (mean * (0.75f + rnd.nextFloat() * 0.5f)).toInt().coerceAtMost(stopAt - page)
                val minutes = (read / (1f + rnd.nextFloat() * 0.4f)).toInt().coerceAtLeast(10)
                val s = at(day, h, rnd.nextInt(50))
                val book = dao.book(id)!!
                Library.sit(
                    dao, book, page, page + read, s, s + minutes * 60_000L, timed = rnd.nextFloat() < 0.8f,
                    stillSec = if (rnd.nextFloat() < 0.4f) minutes * 40 else 0, zone = zone,
                )
                page += read
                day = day.plusDays(1)
            }
        }
        dao.activate(fox)
        // Grown, with the last stage still ahead.
        dao.pets().first { it.id == fox }.let { dao.updatePet(it.copy(mana = 3150)) }
        // Ratings on the finished ones.
        dao.books().filter { it.shelfOf == Shelf.DONE }.forEachIndexed { i, b -> dao.updateBook(b.copy(rating = listOf(5, 4, 5, 5, 4, 5, 4)[i % 7])) }
        val pets = dao.pets()
        val den = listOf(app.familiar.domain.Item.WINDOW_MOON, app.familiar.domain.Item.BOOKSHELF, app.familiar.domain.Item.RUG_RED, app.familiar.domain.Item.CAULDRON, app.familiar.domain.Item.LANTERN, app.familiar.domain.Item.CANDLES, app.familiar.domain.Item.MANDRAKE)
        settings.update {
            it.copy(
                onboarded = true, name = if (ru) "Мира" else "Mira", remindAt = 21 * 60, theme = ThemeMode.DARK,
                owned = den.map { d -> d.name }.toSet(), placed = den.map { d -> d.name }.toSet(), spent = den.sumOf { d -> d.price }, yearGoal = 24,
                seenUnlocks = Species.entries.map { s -> s.name }.toSet(),
                seenStages = pets.flatMap { p -> app.familiar.domain.Stage.entries.map { st -> "${p.id}:${st.name}" } }.toSet(),
            )
        }
    }
}
