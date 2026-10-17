package app.familiar.ui.pix

/** The little things a slime can swallow, picked by a word in what you typed. */
enum class Glyph(vararg val words: String) {
    SWORD("меч", "клин", "кинжал", "нож", "шпаг", "sword", "blade", "dagger", "knife"),
    RING("кольц", "перстень", "ring"),
    KEY("ключ", "key"),
    LETTER("письм", "записк", "конверт", "letter", "note", "envelope"),
    POTION("зель", "яд", "бутыл", "флакон", "potion", "poison", "bottle", "vial"),
    GEM("камень", "кристал", "алмаз", "рубин", "изумруд", "gem", "crystal", "stone", "diamond", "ruby"),
    COIN("монет", "золот", "деньг", "coin", "gold", "money"),
    FEATHER("перо", "перь", "feather", "quill"),
    APPLE("яблок", "фрукт", "груш", "apple", "fruit", "pear"),
    SKULL("череп", "кост", "skull", "bone"),
    CROWN("корон", "трон", "crown", "throne"),
    BOOK("книг", "дневник", "том", "book", "diary", "journal"),
    STAR("звезд", "star"),
    HEART("сердц", "любов", "heart", "love"),
    FLOWER("цвет", "роз", "flower", "rose"),
    MAP("карт", "map");

    companion object {
        fun of(text: String): Glyph {
            val s = text.lowercase()
            return entries.firstOrNull { g -> g.words.any { it in s } } ?: entries[Math.floorMod(s.hashCode(), entries.size)]
        }
    }
}

/** Seven-pixel icons for the glyphs, outlined. */
object Glyphs {
    private val pal = mapOf(
        'a' to R.iron[5], 'b' to R.iron[3], 'g' to R.gold[3], 'G' to R.gold[5], 'h' to R.wood[3],
        'r' to R.red[3], 'R' to R.red[5], 'w' to WHITE, 'p' to R.paper[3], 'd' to R.paper[1],
        'v' to R.violet[3], 'V' to R.violet[5], 'c' to R.sky[3], 'C' to R.sky[5], 'l' to R.leaf[4],
        'y' to R.gold[4], 'k' to R.pink[2], 'm' to R.paper[2], 's' to R.snow[4], 'e' to EYE,
    )

    private val art = mapOf(
        Glyph.SWORD to listOf(".....aw", "....ab.", "...ab..", ".gab...", "..g....", ".h.g...", "h......"),
        Glyph.RING to listOf("...R...", "..grg..", ".g...g.", "g.....g", "g.....g", ".g...g.", "..ggg.."),
        Glyph.KEY to listOf(".GGG...", "G...g..", "G...g..", ".ggg...", "..g....", "..gg...", "..g.g.."),
        Glyph.LETTER to listOf(".......", "ppppppp", "pdpppdp", "ppdpdpp", "pppRppp", "ppppppp", "......."),
        Glyph.POTION to listOf("..hh...", "..ss...", "..ss...", ".vvvv..", "vVvvvv.", "vvvvvv.", ".vvvv.."),
        Glyph.GEM to listOf(".......", ".ccccc.", "cCCcccc", ".ccccc.", "..ccc..", "...c...", "......."),
        Glyph.COIN to listOf(".......", "..ggg..", ".gGGGg.", ".gGgGg.", ".gGGGg.", "..ggg..", "......."),
        Glyph.FEATHER to listOf(".....ww", "....wws", "...wws.", "..wws..", ".wws...", ".ws....", "s......"),
        Glyph.APPLE to listOf("...h.ll", "...hll.", ".rrrrr.", "rrRrrrr", "rRrrrrr", "rrrrrrr", ".rr.rr."),
        Glyph.SKULL to listOf(".wwwww.", "wwwwwww", "weewwew", "wwwewww", ".wwwww.", ".w.w.w.", "......."),
        Glyph.CROWN to listOf(".......", "g..g..g", "gg.g.gg", "ggggggg", "gGrGcGg", "ggggggg", "......."),
        Glyph.BOOK to listOf(".vvvvv.", ".vVvvvp", ".vvvvvp", ".vgggvp", ".vvvvvp", ".vvvvvp", ".vvvvv."),
        Glyph.STAR to listOf("...y...", "..yGy..", "yyyGyyy", ".yyyyy.", "..yyy..", ".yy.yy.", ".y...y."),
        Glyph.HEART to listOf(".......", ".rr.rr.", "rRrrrrr", "rrrrrrr", ".rrrrr.", "..rrr..", "...r..."),
        Glyph.FLOWER to listOf("..k.k..", ".kkkkk.", "kkyyykk", ".kyGyk.", "kkyyykk", ".kkkkk.", "...l..."),
        Glyph.MAP to listOf(".......", "mmmmmmm", "mdmmmrm", "mmdmrmm", "mmmdmrm", "mmmmmmm", "......."),
    )

    /** Draws [g] with its top-left at (x0, y0) and a dark outline around it. */
    fun draw(px: Px, g: Glyph, x0: Int, y0: Int) {
        val rows = art[g] ?: return
        val m = BooleanArray(px.w * px.h)
        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, ch ->
                val c = pal[ch] ?: return@forEachIndexed
                px.set(x0 + x, y0 + y, c)
                val i = (y0 + y) * px.w + x0 + x
                if (i in m.indices) m[i] = true
            }
        }
        px.ring(m, OUT)
    }
}
