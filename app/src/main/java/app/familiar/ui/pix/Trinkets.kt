package app.familiar.ui.pix

import app.familiar.domain.Trinket

/** Nine-pixel pictures of the things familiars bring back, outlined like the glyphs. */
object Trinkets {
    const val SIZE = 11

    private val pal = mapOf(
        'a' to R.iron[5], 'b' to R.iron[3], 'B' to R.iron[1], 'g' to R.gold[3], 'G' to R.gold[5], 'y' to R.gold[4],
        'h' to R.wood[3], 'H' to R.wood[1], 'n' to R.tan[3], 'N' to R.tan[1], 'r' to R.red[3], 'R' to R.red[5],
        'w' to WHITE, 'p' to R.paper[3], 'd' to R.paper[1], 'm' to R.paper[2], 'v' to R.violet[3], 'V' to R.violet[1],
        'c' to R.sky[3], 'C' to R.sky[5], 'l' to R.leaf[4], 'L' to R.leaf[2], 'k' to R.pink[2], 'K' to R.pink[3],
        's' to R.snow[4], 'S' to R.snow[2], 'e' to EYE, 'o' to R.flame[4], 'O' to R.flame[2], 't' to R.teal[3], 'T' to R.teal[5],
    )

    private val art = mapOf(
        Trinket.BOOKMARK to listOf("...g.....", "..rrrr...", "..rRrr...", "..rRrr...", "..rrrr...", "..rrrr...", "..rrrr...", "..rr.r...", "..r..r..."),
        Trinket.ACORN to listOf("....H....", "..HhhhH..", ".hhhhhhh.", ".HHHHHHH.", "..nnnnn..", "..nGnnn..", "..nnnnn..", "...nnn...", "....n...."),
        Trinket.BUTTON to listOf(".........", ".kkkkkkk.", "kkKKKKKkk", "kKKeKeKKk", "kKKKKKKKk", "kKKeKeKKk", "kkKKKKKkk", ".kkkkkkk.", "........."),
        Trinket.CANDLE_STUB to listOf("....o....", "...oOo...", "....O....", "...www...", "...wsw...", "...wsw...", "..mwwwm..", ".ggggggg.", "..ggggg.."),
        Trinket.TEACUP to listOf("..s..s...", "...s..s..", ".........", "wwwwwww..", "wkkkkkwww", "wwwwwww.w", "wwwwwwwww", ".wwwww...", "ggggggggg"),
        Trinket.DICE to listOf(".wwwwww..", "wwwwwwwS.", "wewwwewS.", "wwwwwwwS.", "wwwewwwS.", "wwwwwwwS.", "wewwwewS.", "wwwwwwwS.", ".SSSSSS.."),
        Trinket.RIBBON to listOf(".........", "kk.....kk", "kKk...kKk", "kKKkrkKKk", "kKKkrkKKk", "kKk.r.kKk", "kk..r..kk", "...k.k...", "..k...k.."),
        Trinket.SEASHELL to listOf(".........", "....K....", "...KkK...", "..KkKkK..", ".KkKkKkK.", "KkKkKkKkK", "KkKkKkKkK", ".KKKKKKK.", "...kkk..."),
        Trinket.BELL to listOf("....g....", "...ggg...", "..gGGgg..", "..gGggg..", "..gGggg..", ".gGgggggg", "ggggggggg", "....H....", "........."),
        Trinket.MOON_PEBBLE to listOf(".........", "...SSS...", ".SSsssSS.", "SsssCsssS", "SssCCsssS", "SssCCsssS", "SsssCsssS", ".SSsssSS.", "...SSS..."),
        Trinket.DRAGON_SCALE to listOf("....T....", "...TtT...", "..TtttT..", ".TttTttT.", ".TtTtTtT.", ".TttTttT.", "..TtttT..", "...TtT...", "....T...."),
        Trinket.GEAR to listOf("...b.b...", "..bbbbb..", "bbbaaabbb", ".ba...ab.", "bba...abb", ".ba...ab.", "bbbaaabbb", "..bbbbb..", "...b.b..."),
        Trinket.MAGNIFIER to listOf("..aaa....", ".aCCca...", "aCwcca...", "acccca...", ".acca....", "..aaahh..", "....hh...", ".....hh..", "......hh."),
        Trinket.SPIDER to listOf("....e....", "....e....", "e..eee..e", ".eeeeeee.", "e.ewewe.e", ".eeeeeee.", "e..eee..e", "...e.e...", "..e...e.."),
        Trinket.LOVE_LETTER to listOf(".........", "ppppppppp", "pdpppppdp", "ppdpppdpp", "pppdrdppp", "ppprRrppp", "pppprpppp", "ppppppppp", "........."),
        Trinket.COMPASS to listOf("...ggg...", "..gpppg..", ".gppRppg.", "gpppRpppg", "gpppepppg", "gpppbpppg", ".gppbppg.", "..gpppg..", "...ggg..."),
        Trinket.QUILL to listOf("......www", ".....wws.", "....wws..", "...wws...", "..wws....", ".gws.....", ".g.......", "g........", "........."),
        Trinket.OLD_COIN to listOf(".........", "..ggggg..", ".gGGGGGg.", "gGGgggGGg", "gGgGGgGGg", "gGgGGgGGg", "gGGgggGGg", ".gGGGGGg.", "..ggggg.."),
        Trinket.SPECTACLES to listOf(".........", ".........", "aaaa.aaaa", "aCca.aCca", "acCaaacCa", "acca.acca", "aaaa.aaaa", ".........", "........."),
        Trinket.PRESSED_FLOWER to listOf("...K.K...", "..KKKKK..", "..KKyKK..", "...KKK...", "....L....", "..l.L....", "...lL.l..", "....LL...", "....L...."),
        Trinket.PUMPKIN to listOf("....L....", "..ooLoo..", ".oOoooOo.", "oOeOoeOoO", "oOoOoOoOo", "oOeeeeeOo", ".oOoOoOo.", "..ooooo..", "........."),
        Trinket.SNOW_GLOBE to listOf("..CCCCC..", ".CsCCCsC.", "CCCClCCCC", "CsClllCsC", "CCClllCCC", ".CCChCCC.", "..hhhhh..", ".HHHHHHH.", "........."),
        Trinket.VALENTINE to listOf(".........", "kkkkkkkkk", "kwrrwrrwk", "kwrRrrrwk", "kwrrrrrwk", "kwwrrrwwk", "kwwwrwwwk", "kkkkkkkkk", "........."),
        Trinket.FIRST_EDITION to listOf(".VVVVVVV.", ".VgggggVp", ".VgVVVgVp", ".VgVGVgVp", ".VgVVVgVp", ".VgggggVp", ".VVVVVVVp", "..ppppppp", "........."),
        Trinket.FIREFLY_JAR to listOf("..HHHHH..", "..aaaaa..", ".aSSSSSa.", ".aSGSSSa.", ".aSSSSGa.", ".aSGSSSa.", ".aSSSSSa.", "..aaaaa..", "........."),
    )

    /** [t] on its own little canvas, outlined; [dim] draws a dark silhouette of one not found yet. */
    fun draw(t: Trinket, dim: Boolean = false): Px {
        val px = Px(SIZE, SIZE)
        val m = BooleanArray(SIZE * SIZE)
        art[t]?.forEachIndexed { y, row ->
            row.forEachIndexed { x, ch ->
                val c = pal[ch] ?: return@forEachIndexed
                px.set(x + 1, y + 1, if (dim) R.iron[2] else c)
                m[(y + 1) * SIZE + x + 1] = true
            }
        }
        px.ring(m, if (dim) R.iron[1] else OUT)
        return px
    }
}
