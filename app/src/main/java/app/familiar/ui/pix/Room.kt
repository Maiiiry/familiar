package app.familiar.ui.pix

import app.familiar.domain.Item
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * The den: a cosy room behind the familiar, furnished with what was bought for sparks.
 * 144 x 108 pixels; the familiar stands in the middle with its feet on [FLOOR_Y].
 */
object Room {
    const val W = 144
    const val H = 108
    const val FLOOR_Y = 104
    const val FRAMES = 4

    private val plaster = Ramp(rgb(40, 30, 48), rgb(54, 42, 62), rgb(68, 54, 76), rgb(84, 68, 90), rgb(102, 84, 104))
    private val plasterDay = Ramp(rgb(96, 78, 82), rgb(122, 102, 102), rgb(148, 126, 120), rgb(172, 150, 138), rgb(194, 172, 156))
    private val sky = Ramp(rgb(10, 12, 40), rgb(18, 22, 64), rgb(30, 36, 92), rgb(48, 56, 124))
    private val skyDay = Ramp(rgb(70, 130, 200), rgb(104, 164, 224), rgb(146, 196, 240), rgb(196, 226, 250))
    private val velvet = Ramp(rgb(52, 10, 30), rgb(96, 20, 42), rgb(140, 34, 52), rgb(184, 56, 66), rgb(222, 98, 96))
    private val brew = Ramp(rgb(16, 52, 30), rgb(30, 104, 46), rgb(64, 168, 70), rgb(140, 224, 110), rgb(214, 252, 170))
    private val clay = Ramp(rgb(70, 30, 24), rgb(122, 58, 36), rgb(172, 92, 52), rgb(212, 132, 76), rgb(236, 176, 112))
    private val sea = Ramp(rgb(14, 36, 70), rgb(24, 64, 112), rgb(40, 98, 152), rgb(74, 140, 190), rgb(130, 190, 226))

    /** One frame of the den. [books] colour the bookshelf; [night] shows the moon in the window. */
    fun scene(placed: Set<Item>, books: List<Int>, night: Boolean, f: Int): Px {
        val px = Px(W, H)
        wall(px, night)
        if (Item.WINDOW_MOON in placed) windowMoon(px, night, f)
        if (Item.WINDOW_STAINED in placed) windowStained(px, night, f)
        if (Item.BOOKSHELF in placed) bookshelf(px, books)
        if (Item.PORTRAIT in placed) portrait(px)
        if (Item.BANNER in placed) banner(px, f)
        if (Item.LANTERN in placed) lantern(px, f)
        floor(px)
        if (Item.RUG_RED in placed) rug(px, R.red, R.gold, false)
        if (Item.RUG_STARS in placed) rug(px, R.night, R.gold, true)
        if (Item.ARMCHAIR in placed) armchair(px)
        if (Item.CAULDRON in placed) cauldron(px, f)
        if (Item.TELESCOPE in placed) telescope(px)
        if (Item.MANDRAKE in placed) mandrake(px, f)
        if (Item.TREASURE in placed) treasure(px, f)
        if (Item.GLOBE in placed) globe(px)
        if (Item.CANDLES in placed) candles(px, f)
        if (Item.GARLAND in placed) garland(px, f)
        // Warm light around where the familiar sits, stronger with candles and lanterns.
        val warmth = 34 + (if (Item.CANDLES in placed) 18 else 0) + (if (Item.LANTERN in placed) 18 else 0)
        px.glow(72f, 74f, 70f, rgb(255, 190, 110), warmth, 5, onlyDrawn = true)
        return px
    }

    /** One piece alone on a bit of wall and floor, for the shop. */
    fun preview(item: Item, books: List<Int>, night: Boolean, f: Int): Px {
        val full = scene(setOf(item), books, night, f)
        val (x0, y0, w, h) = when (item.place) {
            app.familiar.domain.Place.WINDOW -> listOf(2, 6, 42, 50)
            app.familiar.domain.Place.WALL -> listOf(102, 4, 42, 56)
            app.familiar.domain.Place.TOP -> if (item == Item.GARLAND) listOf(0, 0, 60, 30) else listOf(46, 0, 52, 30)
            app.familiar.domain.Place.FLOOR_LEFT -> listOf(0, 50, 42, 58)
            app.familiar.domain.Place.FLOOR_RIGHT -> listOf(102, 54, 42, 54)
            app.familiar.domain.Place.RUG -> listOf(24, 84, 96, 24)
            app.familiar.domain.Place.LIGHT -> if (item == Item.LANTERN) listOf(28, 0, 34, 40) else listOf(0, 84, 30, 24)
        }
        val out = Px(w, h)
        for (y in 0 until h) for (x in 0 until w) out.c[y * w + x] = full[x0 + x, y0 + y]
        return out
    }

    private fun wall(px: Px, night: Boolean) {
        val r = if (night) plaster else plasterDay
        px.shade(rect(0f, 0f, W.toFloat(), 74f), { _, y -> V3(0f, -0.2f + y / 160f, 1f) }, r, outline = false,
            texture = { x, y -> noise(x / 3, y / 3, 70) * 0.18f })
        // Wooden wainscot and a skirting board.
        px.shade(rect(0f, 52f, W.toFloat(), 22f), facing(0f, 0.1f, 1f), R.wood, outline = false, lo = 1, hi = 4,
            texture = { x, _ -> if (x % 12 == 0) -0.35f else if (x % 12 == 1) 0.15f else 0f })
        px.hline(0, W - 1, 52, R.wood[4]); px.hline(0, W - 1, 53, R.wood[2])
        px.hline(0, W - 1, 72, R.wood[1]); px.hline(0, W - 1, 73, R.wood[0])
    }

    private fun floor(px: Px) {
        for (y in 74 until H) for (x in 0 until W) {
            // Long planks running across, with a soft grain and the odd board end.
            val row = (y - 74) / 5
            val seam = (y - 74) % 5 == 4
            val end = (x + row * 37) % 61 == 0
            val grain = noise(x / 9 + row * 11, (y - 74) % 5, 71) * 1.4f + if ((x / 4 + row) % 7 == 0 && (y - 74) % 5 == 2) -0.8f else 0f
            val v = if (seam) 1 else if (end) 0 else (3 + grain + bayer(x, y) * 0.5f).toInt().coerceIn(2, 4)
            px.set(x, y, R.wood[v])
        }
        for (x in 0 until W) px.set(x, 74, R.wood[0], 160)
    }

    private fun frame(px: Px, x0: Int, y0: Int, w: Int, h: Int) {
        px.shade(rect(x0 - 2f, y0 - 2f, w + 4f, h + 4f), facing(-0.3f, -0.3f, 1f), R.wood, lo = 1, hi = 5)
    }

    private fun windowMoon(px: Px, night: Boolean, f: Int) {
        val x0 = 10; val y0 = 12; val w = 26; val h = 34
        frame(px, x0, y0, w, h)
        val s = if (night) sky else skyDay
        for (y in y0 until y0 + h) for (x in x0 until x0 + w) px.set(x, y, s[((y - y0) * s.size / h + (bayer(x, y) * 0.8f).toInt()).coerceIn(0, s.size - 1)])
        if (night) {
            px.shade(ellipse(x0 + 18f, y0 + 9f, 4.5f, 4.5f), dome(x0 + 17f, y0 + 8f, 4.5f, 4.5f), R.paper, outline = false, lo = 2)
            px.shade(ellipse(x0 + 20f, y0 + 8f, 3.5f, 3.5f), facing(), Ramp(s[1]), outline = false)
            for (i in 0 until 9) {
                val sx = x0 + 2 + ((noise(i, 1, 80) + 0.5f) * (w - 4)).toInt()
                val sy = y0 + 2 + ((noise(i, 2, 80) + 0.5f) * (h - 6)).toInt()
                if ((i + f) % 4 != 0) px.set(sx, sy, if (i % 3 == 0) WHITE else R.gold[4])
            }
        } else {
            px.shade(ellipse(x0 + 18f, y0 + 8f, 4f, 4f), facing(), R.gold, outline = false, lo = 4)
            for ((cx, cy) in listOf(x0 + 7f to y0 + 16f, x0 + 11f to y0 + 15f, x0 + 15f to y0 + 17f)) px.shade(ellipse(cx, cy, 4f, 2.6f), dome(cx, cy - 1, 4f, 2.6f), R.snow, outline = false, lo = 3)
        }
        // Mullions and the sill.
        px.vline(x0 + w / 2, y0, y0 + h - 1, R.wood[2]); px.hline(x0, x0 + w - 1, y0 + h / 2, R.wood[2])
        px.shade(rect(x0 - 4f, y0 + h + 1f, w + 8f, 3f), facing(0f, -0.6f, 1f), R.wood, lo = 1, hi = 5)
    }

    private fun windowStained(px: Px, night: Boolean, f: Int) {
        val cx = 23f; val top = 10f; val w = 24f; val h = 38f
        val arch = union(rect(cx - w / 2, top + w / 2, w, h - w / 2), ellipse(cx, top + w / 2, w / 2, w / 2))
        val outer = union(rect(cx - w / 2 - 2, top + w / 2, w + 4, h - w / 2 + 2), ellipse(cx, top + w / 2, w / 2 + 2, w / 2 + 2))
        px.shade(outer, facing(-0.3f, -0.3f, 1f), R.iron, lo = 1, hi = 4)
        val colors = listOf(R.red, R.sky, R.gold, R.leaf, R.violet)
        val m = px.cover(arch)
        for (y in 0 until H) for (x in 0 until W) {
            if (!m[y * W + x]) continue
            val cell = ((x - 11) / 6) + ((y - 10) / 7) * 3
            val r = colors[Math.floorMod(cell, colors.size)]
            val lead = (x - 11) % 6 == 0 || (y - 10) % 7 == 0
            val glowing = !night || (cell + f) % 5 != 0
            px.set(x, y, if (lead) R.iron[0] else r[if (glowing) 4 else 3])
        }
        if (!night) px.glow(cx, top + h / 2, 22f, rgb(255, 240, 200), 40, 4, onlyDrawn = true)
        px.shade(rect(cx - w / 2 - 4, top + h + 2, w + 8, 3f), facing(0f, -0.6f, 1f), R.iron, lo = 1, hi = 4)
    }

    private fun bookshelf(px: Px, books: List<Int>) {
        val x0 = 110; val y0 = 10; val w = 30; val h = 60
        px.shade(rect(x0.toFloat(), y0.toFloat(), w.toFloat(), h.toFloat()), facing(-0.2f, 0f, 1f), R.wood, lo = 0, hi = 4)
        val palette = books.ifEmpty { listOf(0xFF8E6BD8.toInt(), 0xFF3FB0C9.toInt(), 0xFFB8394A.toInt(), 0xFFE08A3C.toInt(), 0xFF4E9A69.toInt()) }
        for (shelf in 0 until 4) {
            val by = y0 + 3 + shelf * 14
            px.rect(x0 + 2, by, w - 4, 12, R.wood[0])
            var x = x0 + 3
            var i = shelf * 7
            while (x < x0 + w - 4) {
                val bw = 2 + (i % 2)
                val bh = 8 + ((noise(i, shelf, 90) + 0.5f) * 4).toInt()
                val r = R.around(palette[i % palette.size])
                px.shade(rect(x.toFloat(), (by + 12 - bh).toFloat(), bw.toFloat(), bh.toFloat()), facing(-0.3f, 0f, 1f), r, outline = false, lo = 2, hi = 5)
                px.set(x, by + 12 - bh + 2, R.gold[4])
                x += bw + if (i % 5 == 4) 2 else 0
                i++
            }
            px.hline(x0 + 1, x0 + w - 2, by + 12, R.wood[4])
            px.hline(x0 + 1, x0 + w - 2, by + 13, R.wood[2])
        }
    }

    private fun portrait(px: Px) {
        val x0 = 114; val y0 = 16; val w = 22; val h = 26
        px.shade(rect(x0 - 3f, y0 - 3f, w + 6f, h + 6f), facing(-0.4f, -0.4f, 1f), R.gold, spec = true)
        for (y in y0 until y0 + h) for (x in x0 until x0 + w) px.set(x, y, sea[((y - y0) * 4 / h).coerceIn(0, 4)])
        // Mountains under a moon.
        px.shade(poly(x0.toFloat(), y0 + h.toFloat(), x0 + 8f, y0 + 12f, x0 + 13f, y0 + 18f, x0 + 18f, y0 + 10f, x0 + w.toFloat(), y0 + 17f, x0 + w.toFloat(), y0 + h.toFloat()), facing(-0.4f, -0.5f, 1f), R.leaf, outline = false, lo = 1, hi = 4)
        px.shade(ellipse(x0 + 16f, y0 + 6f, 2.5f, 2.5f), facing(), R.paper, outline = false, lo = 3)
    }

    private fun banner(px: Px, f: Int) {
        val x0 = 60f; val w = 24f
        px.hline(56, 87, 1, R.gold[2]); px.hline(56, 87, 2, R.gold[1])
        val sway = if (f % 2 == 0) 0f else 0.6f
        val cloth = poly(x0, 3f, x0 + w, 3f, x0 + w + sway, 24f, x0 + w / 2 + sway, 19f, x0 + sway, 24f)
        px.shade(cloth, { x, _ -> V3(((x - (x0 + w / 2)) / w) * 0.8f, 0f, 1f) }, R.violet, lo = 1, hi = 5)
        // A gold open book on it.
        val cx = (x0 + w / 2).toInt(); val cy = 11
        px.hline(cx - 5, cx - 1, cy, R.gold[4]); px.hline(cx + 1, cx + 5, cy, R.gold[4])
        px.hline(cx - 5, cx - 1, cy + 3, R.gold[3]); px.hline(cx + 1, cx + 5, cy + 3, R.gold[3])
        px.vline(cx, cy, cy + 4, R.gold[2]); px.vline(cx - 5, cy, cy + 3, R.gold[3]); px.vline(cx + 5, cy, cy + 3, R.gold[3])
    }

    private fun garland(px: Px, f: Int) {
        val bulbs = listOf(R.gold, R.pink, R.sky, R.leaf)
        var k = 0
        for (x in 0 until W) {
            val y = 6 + (4 * abs(sin((x / (W / 3f)) * PI))).toInt()
            px.set(x, y, R.leaf[1])
            if (x % 9 == 4) {
                val r = bulbs[k % bulbs.size]
                val on = (k + f) % 4 != 0
                px.set(x, y + 1, r[if (on) 3 else 1]); px.set(x, y + 2, r[if (on) 2 else 0])
                if (on) px.glow(x + 0.5f, y + 2f, 4f, r[3], 60, 3, onlyDrawn = true)
                k++
            }
        }
    }

    private fun lantern(px: Px, f: Int) {
        val cx = 45f
        px.vline(45, 0, 11, R.iron[1])
        px.shade(poly(cx - 5, 12f, cx + 5, 12f, cx + 3, 9f, cx - 3, 9f), facing(0f, -0.5f, 1f), R.iron, lo = 1)
        px.shade(rect(cx - 5, 13f, 10f, 12f), facing(), R.iron, lo = 0, hi = 2)
        val flame = if (f % 2 == 0) R.ember else R.flame
        px.shade(rect(cx - 3.5f, 14f, 7f, 10f), { _, y -> V3(0f, (y - 19f) / 6f, 1f) }, flame, outline = false, lo = 3)
        px.vline(45, 14, 23, R.iron[1])
        px.shade(poly(cx - 5, 25f, cx + 5, 25f, cx + 3, 28f, cx - 3, 28f), facing(0f, 0.4f, 1f), R.iron, lo = 1)
        px.glow(cx, 19f, 16f, R.gold[4], 70, 4)
    }

    private fun rug(px: Px, base: Ramp, edge: Ramp, stars: Boolean) {
        val s = ellipse(72f, 100f, 42f, 6.5f)
        px.shade(s, facing(0f, -0.6f, 1f), base, lo = 1, hi = 3, line = edge[2])
        val inner = px.cover(ellipse(72f, 100f, 36f, 4.5f))
        for (y in 92 until H) for (x in 28 until 116) {
            val i = y * W + x
            if (!inner[i]) continue
            val onEdge = !inner[i - 1] || !inner[i + 1] || !inner[i - W] || !inner[i + W]
            if (onEdge) px.set(x, y, edge[3])
            else if (stars && (x * 7 + y * 13) % 23 == 0) px.set(x, y, edge[4])
            else if (!stars && (x + y) % 6 == 0) px.set(x, y, base[4])
        }
    }

    private fun armchair(px: Px) {
        px.shade(rect(6f, 60f, 26f, 30f), { x, _ -> V3((x - 19f) / 16f, -0.2f, 1f) }, velvet, lo = 0, hi = 4)
        for ((bx, by) in listOf(13 to 68, 19 to 66, 25 to 68, 16 to 74, 22 to 74)) { px.set(bx, by, velvet[0]); px.set(bx, by - 1, velvet[4]) }
        px.shade(rect(4f, 82f, 30f, 10f), facing(0f, -0.6f, 1f), velvet, lo = 1, hi = 4)
        px.shade(rect(2f, 74f, 6f, 20f), { x, _ -> V3((x - 5f) / 4f, -0.3f, 1f) }, velvet, lo = 0, hi = 4)
        px.shade(rect(30f, 74f, 6f, 20f), { x, _ -> V3((x - 33f) / 4f, -0.3f, 1f) }, velvet, lo = 0, hi = 4)
        for (lx in listOf(5, 32)) px.rect(lx, 94, 2, 4, R.wood[1])
    }

    private fun cauldron(px: Px, f: Int) {
        // Fire under it.
        for (i in 0 until 3) {
            val fx = 12f + i * 5
            val fl = if ((f + i) % 2 == 0) 4f else 5.5f
            px.shade(poly(fx - 2.5f, 100f, fx, 100f - fl, fx + 2.5f, 100f), facing(), R.ember, outline = false, lo = 2)
        }
        px.shade(ellipse(17f, 90f, 13f, 9f), dome(15f, 87f, 13f, 9f), R.iron, lo = 0, hi = 3, spec = true)
        px.shade(ellipse(17f, 82f, 12f, 3f), facing(0f, -1f, 0.4f), R.iron, lo = 1, hi = 3)
        px.shade(ellipse(17f, 82f, 10f, 2.2f), facing(), brew, outline = false, lo = 2, hi = 3)
        for (i in 0 until 3) {
            val bx = 11 + i * 5 + (if (f % 2 == 0) 0 else 1)
            val by = 79 - ((f + i * 2) % 4) * 3
            px.set(bx, by, brew[4]); px.set(bx + 1, by, brew[3]); px.set(bx, by - 1, brew[3])
        }
        px.glow(17f, 80f, 10f, brew[3], 60, 3)
        for (lx in listOf(8, 25)) px.rect(lx, 97, 2, 4, R.iron[0])
    }

    private fun telescope(px: Px) {
        // Tripod.
        px.line(18, 70, 9, 100, R.wood[1]); px.line(18, 70, 27, 100, R.wood[1]); px.line(18, 70, 18, 101, R.wood[2])
        // The tube, tilted toward the window.
        val tube = poly(6f, 64f, 30f, 52f, 32f, 56f, 8f, 69f)
        px.shade(tube, { x, y -> V3(0f, ((y - (60f - (x - 6) * 0.5f)) / 4f).coerceIn(-1f, 1f), 1f) }, R.gold, spec = true)
        px.shade(ellipse(31f, 54f, 2.2f, 3f), facing(0.6f, -0.2f, 1f), R.iron, lo = 2)
        px.set(17, 60, R.gold[1]); px.set(18, 60, R.gold[1])
    }

    private fun mandrake(px: Px, f: Int) {
        val sway = if (f % 2 == 0) 0f else 1f
        for ((ax, h) in listOf(-6f to 20f, 0f to 26f, 6f to 21f, -2f to 23f, 3f to 18f)) {
            val cx = 124f + ax
            px.shade(poly(cx - 2, 80f, cx + sway, 80f - h, cx + 3, 80f), facing(ax / 10f, -0.3f, 1f), R.leaf, lo = 1)
        }
        px.shade(poly(112f, 80f, 136f, 80f, 132f, 100f, 116f, 100f), { x, _ -> V3((x - 124f) / 14f, 0f, 1f) }, clay, spec = true)
        px.shade(rect(110f, 78f, 28f, 4f), facing(0f, -0.6f, 1f), clay, lo = 1)
        // A little root face peeking out of the soil.
        px.set(121, 77, EYE); px.set(126, 77, EYE)
        px.set(123, 79, R.pink[1]); px.set(124, 79, R.pink[1])
    }

    private fun treasure(px: Px, f: Int) {
        px.shade(ellipse(124f, 100f, 18f, 4.5f), dome(122f, 99f, 18f, 4.5f), R.gold, spec = true)
        px.shade(rect(112f, 80f, 24f, 16f), { x, _ -> V3((x - 124f) / 20f, 0f, 1f) }, R.wood, lo = 0, hi = 5, texture = { _, y -> if (y % 5 == 0) -0.3f else 0f })
        px.shade(poly(112f, 80f, 136f, 80f, 134f, 72f, 114f, 72f), facing(0f, -0.6f, 1f), R.wood, lo = 1, hi = 5)
        for (bx in listOf(116, 131)) px.vline(bx, 72, 95, R.gold[3])
        px.rect(122, 82, 4, 4, R.gold[4]); px.set(123, 84, EYE)
        // Coins spilling out, one glinting.
        for (i in 0 until 6) px.set(108 + i * 6, 99 + (i % 2), R.gold[5])
        px.set(128 + (f % 2) * 4, 97, WHITE)
    }

    private fun globe(px: Px) {
        px.shade(rect(118f, 96f, 12f, 3f), facing(0f, -0.6f, 1f), R.wood, lo = 1)
        px.vline(124, 84, 96, R.wood[2])
        px.shade(ellipse(124f, 74f, 11f, 11f), dome(121f, 71f, 11f, 11f), sea, spec = true)
        // Continents.
        px.shade(ellipse(120f, 70f, 4f, 3f), facing(), R.leaf, outline = false, lo = 3, hi = 4)
        px.shade(ellipse(127f, 78f, 3.5f, 2.5f), facing(), R.leaf, outline = false, lo = 2, hi = 3)
        for (a in 0 until 360 step 8) {
            val x = (124 + kotlin.math.cos(a * PI / 180) * 13).toInt()
            val y = (74 + sin(a * PI / 180) * 13).toInt()
            if (y < 86) px.set(x, y, R.gold[3])
        }
    }

    private fun candles(px: Px, f: Int) {
        for ((i, pos) in listOf(4 to 6, 9 to 10, 14 to 5).withIndex()) {
            val (cx, h) = pos
            val top = 104 - h
            px.shade(rect(cx - 1.5f, top.toFloat(), 3.5f, h.toFloat()), column(cx + 0.25f, 1.75f), R.cream, lo = 1)
            val fl = if ((f + i) % 2 == 0) 3 else 4
            px.set(cx, top - 1, R.ember[2]); px.vline(cx, top - fl, top - 2, R.ember[4]); px.set(cx, top - fl, R.ember[5])
            px.glow(cx + 0.5f, top - 2f, 7f, R.gold[5], 70, 3)
        }
    }
}
