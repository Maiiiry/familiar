package app.familiar.domain

/** What a book is about. Each one the kitsune tastes grows her a tail of its colour. */
enum class Genre(val argb: Long, val mood: Mood) {
    FANTASY(0xFF8E6BD8, Mood.SPARKLE),
    SCIFI(0xFF3FB0C9, Mood.STARS),
    MYSTERY(0xFF4F6C9E, Mood.SUSPICIOUS),
    HORROR(0xFFB8394A, Mood.SCARED),
    ROMANCE(0xFFE2789C, Mood.HEARTS),
    ADVENTURE(0xFFE08A3C, Mood.SPARKLE),
    CLASSIC(0xFFC9A04A, Mood.GLASSES),
    HISTORY(0xFFA4673E, Mood.GLASSES),
    NONFICTION(0xFF4E9A69, Mood.GLASSES),
    POETRY(0xFFC27FD0, Mood.HEARTS),
    OTHER(0xFF8C8577, Mood.NONE);

    companion object {
        fun of(name: String): Genre = entries.firstOrNull { it.name == name } ?: OTHER

        /** Genres that grow a tail: all but OTHER, nine of them at most. */
        val tails = entries.filter { it != OTHER }
    }
}

/** How the familiar looks after the last book: hiding after horror, blushing after romance. */
enum class Mood { NONE, SPARKLE, STARS, SUSPICIOUS, SCARED, HEARTS, GLASSES }

/** The familiars of the den. The first three can hatch at the start; the rest are found by reading. */
enum class Species(val starter: Boolean) {
    MIMIC(true), KITSUNE(true), SLIME(true),
    DRAGON(false), PHOENIX(false), GRIFFIN(false), OWLBEAR(false), MOTH(false);

    companion object {
        fun of(name: String): Species = entries.firstOrNull { it.name == name } ?: MIMIC
    }
}

/** Growth by mana eaten. An egg hatches after its first twenty pages. */
enum class Stage(val from: Int) {
    EGG(0), BABY(20), YOUNG(300), ADULT(1200), ANCIENT(4000);

    val next: Stage? get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun of(mana: Int): Stage = entries.last { mana >= it.from }

        /** 0..1 on the way to the next stage; 1 when ancient. */
        fun progress(mana: Int): Float {
            val s = of(mana)
            val n = s.next ?: return 1f
            return ((mana - s.from).toFloat() / (n.from - s.from)).coerceIn(0f, 1f)
        }
    }
}
