package app.familiar.content

import app.familiar.domain.Mood
import app.familiar.domain.Species
import java.util.Locale

/**
 * What the familiar is up to, told like a line from a storybook. Russian lines stay in the
 * present tense so they fit any name, whatever its grammatical gender.
 */
object Lines {
    enum class State { EGG, STONE, ASH, HUNGRY, FED, READING, SLEEP, WOKEN, PURR }

    fun of(locale: Locale): Map<Species, Map<State, List<String>>> = if (locale.language == "ru") RU else EN

    /** A line for [state], picked by [seed] so it holds still for a while. */
    fun line(locale: Locale, sp: Species, state: State, name: String, seed: Int): String {
        val st = if (state == State.ASH && sp != Species.PHOENIX) State.STONE else state
        val list = of(locale)[sp]?.get(st).orEmpty().ifEmpty { common(locale)[st].orEmpty() }
        if (list.isEmpty()) return ""
        return list[Math.floorMod(seed, list.size)].replace("{n}", name)
    }

    fun mood(locale: Locale, mood: Mood, name: String): String? {
        val ru = locale.language == "ru"
        return when (mood) {
            Mood.SCARED -> if (ru) "{n} прячется за книгу. Было страшно, но интересно." else "{n} is hiding behind the book. Scary, but good."
            Mood.HEARTS -> if (ru) "{n} вздыхает и смотрит в окно. Какая история!" else "{n} sighs and gazes out of the window. What a story!"
            Mood.SUSPICIOUS -> if (ru) "{n} щурится: кажется, убийца — садовник." else "{n} squints: the gardener did it, surely."
            Mood.SPARKLE -> if (ru) "{n} размахивает воображаемым мечом." else "{n} swings an imaginary sword."
            Mood.STARS -> if (ru) "{n} смотрит на звёзды и что-то считает." else "{n} looks at the stars, counting something."
            Mood.GLASSES -> if (ru) "{n} поправляет очки и выглядит очень учёно." else "{n} adjusts the spectacles, looking very learned."
            Mood.NONE -> null
        }?.replace("{n}", name)
    }

    private fun common(locale: Locale): Map<State, List<String>> = if (locale.language == "ru") mapOf(
        State.EGG to listOf("Яйцо тёплое. Кажется, оно слушает, как ты читаешь.", "Внутри что-то шевелится. Ещё пара страниц…", "Тук-тук. Яйцо ждёт историю."),
        State.STONE to listOf("{n} каменеет: на нём чары забвения. Пара страниц снимет их.", "{n} почти статуя. Открой книгу, и камень треснет."),
        State.FED to listOf("{n} сыто мурлычет и греется у свечи.", "{n} дремлет у камина. Сегодня был хороший ужин."),
        State.READING to listOf("{n} заглядывает через плечо: что там дальше?", "{n} тихо ждёт следующую страницу."),
        State.SLEEP to listOf("{n} спит и сопит. Тсс…", "{n} видит сны о недочитанных главах."),
        State.WOKEN to listOf("{n} сонно приоткрывает один глаз…", "{n} зевает: «Уже утро?»"),
        State.PURR to listOf("{n} жмурится и мурлычет.", "{n} подставляет макушку: ещё, ещё!"),
    ) else mapOf(
        State.EGG to listOf("The egg is warm. It seems to be listening to you read.", "Something stirs inside. A few more pages…", "Tap, tap. The egg is waiting for a story."),
        State.STONE to listOf("{n} is turning to stone: the spell of forgetting. A page or two will break it.", "{n} is almost a statue. Open a book and the stone will crack."),
        State.FED to listOf("{n} purrs, full, warming up by the candle.", "{n} dozes by the fire. Dinner was good today."),
        State.READING to listOf("{n} peeks over your shoulder: what happens next?", "{n} waits quietly for the next page."),
        State.SLEEP to listOf("{n} is asleep and snoring softly. Shh…", "{n} dreams of unfinished chapters."),
        State.WOKEN to listOf("{n} opens one sleepy eye…", "{n} yawns: “Is it morning already?”"),
        State.PURR to listOf("{n} squeezes its eyes shut and purrs.", "{n} pushes its head into your hand: more, more!"),
    )

    private val EN: Map<Species, Map<State, List<String>>> = mapOf(
        Species.MIMIC to mapOf(
            State.HUNGRY to listOf("{n} gnaws on the bookmark out of boredom.", "{n} opens and shuts its lid hopefully. Book?", "{n} has eaten a receipt. Please, a real book."),
            State.FED to listOf("{n} burps up a comma, very pleased.", "{n} snores with its lid half open."),
        ),
        Species.KITSUNE to mapOf(
            State.HUNGRY to listOf("{n} sniffs the cover of your book.", "{n} counts her tails and wonders which one comes next.", "{n} flicks a tail at the bookshelf."),
            State.FED to listOf("{n} curls up in her tails like a blanket.", "{n} grooms a tail the colour of your last book."),
        ),
        Species.SLIME to mapOf(
            State.HUNGRY to listOf("{n} wobbles sadly. Nothing new inside.", "{n} tries to swallow the TV remote. Not that."),
            State.FED to listOf("{n} jiggles happily, showing off what it swallowed.", "{n} glows softly, full of stories."),
        ),
        Species.DRAGON to mapOf(
            State.HUNGRY to listOf("{n} counts the hoard. One more book would be nice.", "{n} breathes a sad little puff of smoke."),
            State.FED to listOf("{n} sleeps on the hoard, one eye open.", "{n} polishes a book spine with its tail."),
        ),
        Species.PHOENIX to mapOf(
            State.HUNGRY to listOf("{n} flickers low, waiting for a story.", "{n} preens, a little dimmer than yesterday."),
            State.ASH to listOf("{n} has burned down to ash. One page, and it will rise again.", "Only warm ash is left of {n}. Read, and it rises brighter."),
            State.FED to listOf("{n} burns bright and warm.", "{n} sings softly; sparks fly up."),
        ),
        Species.GRIFFIN to mapOf(
            State.HUNGRY to listOf("{n} paces by the window, wings restless.", "{n} looks at you sternly. Reading today?"),
            State.FED to listOf("{n} stretches its wings, proud of the streak.", "{n} dozes with its beak tucked under a wing."),
        ),
        Species.OWLBEAR to mapOf(
            State.HUNGRY to listOf("{n} has read its book three times already.", "{n} hoots softly: a new chapter, please?"),
            State.FED to listOf("{n} underlines something important with a claw.", "{n} hugs the book and dozes."),
        ),
        Species.MOTH to mapOf(
            State.HUNGRY to listOf("{n} circles the lamp, waiting for the night pages.", "{n} folds its wings and waits for dark."),
            State.FED to listOf("{n} rests on the lampshade, wings full of stars.", "{n} dusts the page with a little moonlight."),
        ),
    )

    private val RU: Map<Species, Map<State, List<String>>> = mapOf(
        Species.MIMIC to mapOf(
            State.HUNGRY to listOf("{n} грызёт закладку от скуки.", "{n} с надеждой хлопает крышкой. Книга?", "{n} съедает чек из магазина. Нужна настоящая книга."),
            State.FED to listOf("{n} довольно отрыгивает запятую.", "{n} храпит, приоткрыв крышку."),
        ),
        Species.KITSUNE to mapOf(
            State.HUNGRY to listOf("{n} принюхивается к обложке твоей книги.", "{n} пересчитывает хвосты и гадает, какой будет следующим.", "{n} машет хвостом в сторону книжной полки."),
            State.FED to listOf("{n} сворачивается в хвостах, как в одеяле.", "{n} вылизывает хвост цвета последней книги."),
        ),
        Species.SLIME to mapOf(
            State.HUNGRY to listOf("{n} грустно колышется. Внутри ничего нового.", "{n} пытается проглотить пульт от телевизора. Не то."),
            State.FED to listOf("{n} радостно трясётся и хвастается тем, что проглотил.", "{n} мягко светится изнутри: полон историй."),
        ),
        Species.DRAGON to mapOf(
            State.HUNGRY to listOf("{n} пересчитывает сокровища. Ещё бы одну книгу.", "{n} грустно выпускает колечко дыма."),
            State.FED to listOf("{n} спит на книгах, приоткрыв один глаз.", "{n} полирует хвостом корешок книги."),
        ),
        Species.PHOENIX to mapOf(
            State.HUNGRY to listOf("{n} горит вполсилы и ждёт историю.", "{n} чистит перья, чуть тусклее, чем вчера."),
            State.ASH to listOf("От огня остаётся только пепел. Одна страница, и {n} восстанет.", "{n} — горстка тёплого пепла. Почитай, и огонь вернётся ярче."),
            State.FED to listOf("{n} горит ярко и тепло.", "{n} тихо поёт, и вверх летят искры."),
        ),
        Species.GRIFFIN to mapOf(
            State.HUNGRY to listOf("{n} ходит у окна и беспокойно шевелит крыльями.", "{n} строго смотрит на тебя. Сегодня читаем?"),
            State.FED to listOf("{n} расправляет крылья: гордится серией.", "{n} дремлет, спрятав клюв под крыло."),
        ),
        Species.OWLBEAR to mapOf(
            State.HUNGRY to listOf("{n} перечитывает свою книгу в третий раз.", "{n} тихо ухает: можно новую главу?"),
            State.FED to listOf("{n} подчёркивает когтем что-то важное.", "{n} обнимает книгу и дремлет."),
        ),
        Species.MOTH to mapOf(
            State.HUNGRY to listOf("{n} кружит у лампы и ждёт ночных страниц.", "{n} складывает крылья и ждёт темноты."),
            State.FED to listOf("{n} сидит на абажуре, крылья полны звёзд.", "{n} посыпает страницу лунной пыльцой."),
        ),
    )
}
