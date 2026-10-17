package app.familiar.data

import app.familiar.domain.Genre
import app.familiar.domain.Species
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlin.random.Random

/** Made-up history for screenshots (`-Pdemo` builds only): ten weeks of evenings with books. */
object DemoSeed {
    private class B(val en: String, val ru: String, val authorEn: String, val authorRu: String, val pages: Int, val genre: Genre)

    private val done = listOf(
        B("The Hobbit", "Хоббит", "J. R. R. Tolkien", "Дж. Р. Р. Толкин", 310, Genre.FANTASY),
        B("Murder on the Orient Express", "Убийство в «Восточном экспрессе»", "Agatha Christie", "Агата Кристи", 256, Genre.MYSTERY),
        B("Dracula", "Дракула", "Bram Stoker", "Брэм Стокер", 418, Genre.HORROR),
        B("Pride and Prejudice", "Гордость и предубеждение", "Jane Austen", "Джейн Остин", 432, Genre.ROMANCE),
        B("Dune", "Дюна", "Frank Herbert", "Фрэнк Герберт", 612, Genre.SCIFI),
        B("The Master and Margarita", "Мастер и Маргарита", "Mikhail Bulgakov", "Михаил Булгаков", 480, Genre.CLASSIC),
        B("Sapiens", "Sapiens", "Yuval Noah Harari", "Юваль Ной Харари", 498, Genre.NONFICTION),
    )
    private val reading = listOf(
        B("The Adventures of Sherlock Holmes", "Приключения Шерлока Холмса", "Arthur Conan Doyle", "Артур Конан Дойл", 307, Genre.MYSTERY),
        B("The Name of the Wind", "Имя ветра", "Patrick Rothfuss", "Патрик Ротфусс", 662, Genre.FANTASY),
    )
    private val want = listOf(
        B("The Three Musketeers", "Три мушкетёра", "Alexandre Dumas", "Александр Дюма", 704, Genre.ADVENTURE),
        B("A Wizard of Earthsea", "Волшебник Земноморья", "Ursula K. Le Guin", "Урсула Ле Гуин", 264, Genre.FANTASY),
        B("SPQR", "SPQR", "Mary Beard", "Мэри Бирд", 608, Genre.HISTORY),
    )

    private val notesEn = listOf(
        "Bilbo finally leaves the Shire. Adventure starts with a forgotten handkerchief.",
        "The train is stuck in the snow, and everyone has an alibi.",
        "Jonathan writes in his diary that the castle has no mirrors. Brr.",
        "Elizabeth refuses Darcy. Good for her!",
        "The spice must flow. Paul and Jessica are in the desert.",
        "The ball at Satan's. Margarita is fearless.",
        "Why did people start farming? It wasn't for the bread.",
        "Kvothe arrives at the University with two pennies.",
        "Holmes reads a whole life from a hat.",
    )
    private val notesRu = listOf(
        "Бильбо наконец уходит из Шира. Приключение начинается с забытого платка.",
        "Поезд застрял в снегу, и у каждого есть алиби.",
        "Джонатан пишет в дневнике, что в замке нет зеркал. Брр.",
        "Элизабет отказывает Дарси. Так ему и надо!",
        "Пряность должна течь. Пол и Джессика в пустыне.",
        "Бал у сатаны. Маргарита ничего не боится.",
        "Зачем люди начали сеять пшеницу? Не ради хлеба.",
        "Квоут приходит в Университет с двумя пенни.",
        "Холмс читает целую жизнь по одной шляпе.",
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
            Book(title = if (ru) b.ru else b.en, author = if (ru) b.authorRu else b.authorEn, pages = b.pages, genre = b.genre.name, shelf = shelf.name, added = at(added, 19, 0)),
        )
        want.forEachIndexed { i, b -> insert(b, Shelf.WANT, start.plusDays(i * 9L)) }

        // Pages a day that bring the last book to about page 420 today.
        val mean = (done.sumOf { it.pages } + 120 + 420) / (74 * 0.88f)
        var day = start
        var noteI = 0
        val order = done + reading
        var moth = 0L
        for ((i, b) in order.withIndex()) {
            val id = insert(b, Shelf.READING, day)
            // Sherlock is put aside at page 120; the last book is read up to today.
            val stopAt = if (i == done.size) 120 else if (i == done.size + 1) b.pages - 1 else b.pages
            var page = 0
            var first = true
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
                val note = if (first || rnd.nextFloat() < 0.18f) (if (ru) notesRu else notesEn)[minOf(noteI, notesEn.size - 1)].also { if (first) noteI++ } else ""
                Library.sit(
                    dao, book, page, page + read, s, s + minutes * 60_000L, timed = rnd.nextFloat() < 0.8f,
                    stillSec = if (rnd.nextFloat() < 0.4f) minutes * 40 else 0, note = if (first) note else "", zone = zone,
                )
                first = false
                page += read
                day = day.plusDays(1)
            }
        }
        dao.activate(fox)
        // Grown, with the last stage still ahead.
        dao.pets().first { it.id == fox }.let { dao.updatePet(it.copy(mana = 3150)) }
        val books = dao.books()
        fun bookOf(en: String) = books.firstOrNull { it.title == (order.first { it.en == en }.let { b -> if (ru) b.ru else b.en }) }?.id
        val quotes = listOf(
            Triple("Pride and Prejudice", "It is a truth universally acknowledged, that a single man in possession of a good fortune, must be in want of a wife.", "Все знают, что молодой человек, располагающий средствами, должен подыскивать себе жену."),
            Triple("The Master and Margarita", "Manuscripts don't burn.", "Рукописи не горят."),
            Triple("Dracula", "Welcome to my house! Enter freely and of your own will!", "Добро пожаловать в мой дом! Входите свободно и по своей воле!"),
            Triple("The Adventures of Sherlock Holmes", "You see, but you do not observe.", "Вы смотрите, но не наблюдаете."),
            Triple("The Hobbit", "In a hole in the ground there lived a hobbit.", "В норе под землёй жил да был хоббит."),
            Triple("Dune", "Fear is the mind-killer.", "Страх убивает разум."),
        )
        quotes.forEachIndexed { i, (b, en, r) ->
            dao.insertQuote(Quote(bookId = bookOf(b), text = if (ru) r else en, created = at(today.minusDays(60L - i * 10), 21, 0)))
        }
        // Ratings on the finished ones.
        dao.books().filter { it.shelfOf == Shelf.DONE }.forEachIndexed { i, b -> dao.updateBook(b.copy(rating = listOf(5, 4, 5, 5, 4, 5, 4)[i % 7])) }
        val pets = dao.pets()
        settings.update {
            it.copy(
                onboarded = true, name = if (ru) "Мира" else "Mira", remindAt = 21 * 60, theme = ThemeMode.DARK,
                seenUnlocks = Species.entries.map { s -> s.name }.toSet(),
                seenStages = pets.flatMap { p -> app.familiar.domain.Stage.entries.map { st -> "${p.id}:${st.name}" } }.toSet(),
            )
        }
    }
}
