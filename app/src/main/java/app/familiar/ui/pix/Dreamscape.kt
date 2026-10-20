package app.familiar.ui.pix

import app.familiar.domain.Genre
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** What a familiar dreams of after a book: a little scene for every genre, inside a cloud. */
object Dreamscape {
    const val W = 64
    const val H = 44
    const val FRAMES = 4

    fun scene(g: Genre, f: Int): Px {
        val px = Px(W, H)
        val base = R.around(g.argb.toInt())
        // A soft night sky in the colour of the genre.
        for (y in 0 until H) for (x in 0 until W) px.set(x, y, base[(1 + y * 3 / H + (bayer(x, y) * 0.9f).toInt()).coerceIn(0, 3)])
        for (i in 0 until 10) {
            val x = ((noise(i, 1, 300) + 0.5f) * W).toInt(); val y = ((noise(i, 2, 300) + 0.5f) * H * 0.5f).toInt()
            if ((i + f) % 4 != 0) px.set(x, y, if (i % 3 == 0) WHITE else base[6])
        }
        when (g) {
            Genre.FANTASY -> castle(px, f)
            Genre.SCIFI -> space(px, f)
            Genre.MYSTERY -> mystery(px, f)
            Genre.HORROR -> haunted(px, f)
            Genre.ROMANCE -> hearts(px, f)
            Genre.ADVENTURE -> ship(px, f)
            Genre.CLASSIC -> ball(px, f)
            Genre.HISTORY -> pyramids(px, f)
            Genre.NONFICTION -> bulb(px, f)
            Genre.POETRY -> lake(px, f)
            Genre.OTHER -> sheep(px, f)
        }
        cloud(px)
        return px
    }

    /** Cuts the scene into a cloud shape, like a thought bubble. */
    private fun cloud(px: Px) {
        val c = union(
            ellipse(W / 2f, H / 2f + 2, W * 0.42f, H * 0.36f), ellipse(W * 0.22f, H * 0.56f, 13f, 12f), ellipse(W * 0.78f, H * 0.56f, 13f, 12f),
            ellipse(W * 0.36f, H * 0.3f, 14f, 12f), ellipse(W * 0.64f, H * 0.3f, 14f, 12f), ellipse(W / 2f, H * 0.78f, 20f, 9f),
        )
        val m = px.cover(c)
        for (i in m.indices) if (!m[i]) px.c[i] = 0
        px.ring(m, WHITE)
        val outer = BooleanArray(m.size) { m[it] || px.c[it].al() > 0 }
        px.ring(outer, R.lilac[3])
    }

    private fun ground(px: Px, ramp: Ramp, y0: Int) {
        for (y in y0 until H) for (x in 0 until W) px.set(x, y, ramp[(2 - (y - y0) / 4).coerceIn(0, ramp.size - 1)])
    }

    private fun castle(px: Px, f: Int) {
        px.shade(ellipse(48f, 10f, 4f, 4f), facing(), R.paper, outline = false, lo = 3)
        ground(px, R.leaf, 34)
        val walls = union(rect(20f, 20f, 24f, 15f), rect(16f, 14f, 6f, 21f), rect(42f, 14f, 6f, 21f), rect(29f, 10f, 6f, 25f))
        px.shade(walls, { x, _ -> V3((x - 32f) / 30f, 0f, 1f) }, R.stone, lo = 1, hi = 5)
        for ((cx, top) in listOf(19f to 14f, 45f to 14f, 32f to 10f)) px.shade(poly(cx - 4f, top, cx, top - 7f, cx + 4f, top), facing(-0.3f, -0.4f, 1f), R.violet, lo = 1)
        px.vline(32, 0, 3, R.iron[2]); px.rect(33, 0, 4, 2, R.red[3 + f % 2])
        px.shade(rect(29f, 27f, 6f, 8f), facing(), R.wood, outline = false, lo = 0, hi = 1)
        for ((wx, wy) in listOf(19 to 20, 45 to 20, 32 to 16)) px.rect(wx - 1, wy, 2, 3, if ((wx + f) % 3 == 0) R.gold[5] else R.gold[4])
    }

    private fun space(px: Px, f: Int) {
        px.shade(ellipse(40f, 24f, 11f, 11f), dome(37f, 21f, 11f, 11f), R.sky, spec = true)
        val ring = minus(ellipse(40f, 25f, 19f, 4f), ellipse(40f, 24f, 15f, 2.5f))
        px.shade(and(ring, union(rect(0f, 25f, W.toFloat(), 10f), minus(rect(0f, 0f, W.toFloat(), H.toFloat()), ellipse(40f, 24f, 11f, 11f)))), facing(), R.gold, outline = false, lo = 2, hi = 4)
        val ry = 26f - f * 1.5f
        px.shade(poly(16f, ry, 19f, ry - 9f, 22f, ry), facing(), R.snow, lo = 2)
        px.shade(rect(16f, ry, 6f, 6f), column(19f, 3f), R.snow, lo = 2)
        px.set(19, (ry + 2).toInt(), R.sky[4])
        px.shade(poly(17f, ry + 6f, 21f, ry + 6f, 19f, ry + 10f + f % 2 * 2), facing(), R.ember, outline = false, lo = 3)
    }

    private fun mystery(px: Px, f: Int) {
        ground(px, R.stone, 33)
        for (i in 0 until 4) { val x = 8 + i * 9; val y = 38 - (i % 2) * 2; px.rect(x, y, 3, 2, R.stone[0]); px.set(x + 1, y - 1, R.stone[0]) }
        val cx = 42f + if (f % 2 == 0) 0f else 1f
        px.shade(minus(ellipse(cx, 18f, 9f, 9f), ellipse(cx, 18f, 6.5f, 6.5f)), facing(-0.3f, -0.3f, 1f), R.gold, spec = true)
        px.shade(ellipse(cx, 18f, 6.5f, 6.5f), facing(), R.glass, outline = false, lo = 1, hi = 3)
        px.set(cx.toInt() - 3, 15, WHITE); px.set(cx.toInt() - 2, 15, WHITE)
        px.shade(poly(cx + 5f, 24f, cx + 8f, 22f, cx + 15f, 31f, cx + 12f, 33f), facing(), R.wood, lo = 1)
        for (i in 0 until 3) px.glow(10f + i * 18f, 30f, 9f, R.snow[4], 40, 3)
    }

    private fun haunted(px: Px, f: Int) {
        px.shade(ellipse(46f, 13f, 7f, 7f), facing(), R.paper, outline = false, lo = 3)
        px.glow(46f, 13f, 12f, R.paper[4], 50)
        ground(px, R.ash, 35)
        for ((tx, h) in listOf(12f to 9f, 26f to 7f, 52f to 8f)) px.shade(union(rect(tx - 3f, 35f - h, 6f, h), ellipse(tx, 35f - h, 3f, 3f)), facing(-0.4f, 0f, 1f), R.stone, lo = 1, hi = 4)
        val gy = 22f + if (f % 2 == 0) 0f else 1.5f
        px.shade(union(ellipse(32f, gy, 6f, 6f), rect(26f, gy, 12f, 7f)), dome(31f, gy, 6f, 9f), R.fluff, lo = 1)
        px.rect(29, gy.toInt() - 1, 2, 2, EYE); px.rect(33, gy.toInt() - 1, 2, 2, EYE)
        for (i in 0 until 2) { val bx = 10 + i * 12 + f * 3; val by = 10 + i * 4; px.hline(bx - 2, bx + 2, by, EYE); px.set(bx - 3, by - 1, EYE); px.set(bx + 3, by - 1, EYE) }
    }

    private fun hearts(px: Px, f: Int) {
        ground(px, R.pink, 36)
        for (i in 0 until 5) {
            val p = ((f / 4f + i / 5f) % 1f)
            val x = 8 + i * 12
            val y = (34 - p * 26).toInt()
            Sprites.stamp(px, Sprites.HEART, x, y, if (i % 2 == 0) R.red[3] else R.pink[3], WHITE)
        }
        // A rose in the middle.
        px.vline(32, 26, 36, R.leaf[2]); px.set(33, 31, R.leaf[4]); px.set(31, 29, R.leaf[4])
        px.shade(ellipse(32f, 23f, 4f, 3.6f), dome(31f, 22f, 4f, 3.6f), R.red, lo = 1)
        px.set(32, 22, R.red[1]); px.set(31, 23, R.red[1])
    }

    private fun ship(px: Px, f: Int) {
        px.shade(ellipse(50f, 11f, 5f, 5f), facing(), R.gold, outline = false, lo = 4)
        for (y in 32 until H) for (x in 0 until W) px.set(x, y, R.sky[if ((x + y * 2 + f * 2) % 9 < 2) 4 else 2])
        val bob = if (f % 2 == 0) 0f else 1f
        px.shade(poly(18f, 30f + bob, 46f, 30f + bob, 42f, 36f + bob, 22f, 36f + bob), facing(0f, 0.3f, 1f), R.wood, lo = 1)
        px.vline(31, (12 + bob).toInt(), (30 + bob).toInt(), R.wood[1])
        px.shade(poly(32f, 13f + bob, 43f, 27f + bob, 32f, 27f + bob), facing(0.3f, 0f, 1f), R.paper, lo = 2)
        px.shade(poly(30f, 15f + bob, 30f, 27f + bob, 22f, 27f + bob), facing(-0.3f, 0f, 1f), R.paper, lo = 2)
        px.rect(32, (11 + bob).toInt(), 4, 2, R.red[3])
    }

    private fun ball(px: Px, f: Int) {
        // A chandelier over a ballroom floor, candles flickering.
        for (y in 34 until H) for (x in 0 until W) px.set(x, y, if ((x / 4 + y / 4) % 2 == 0) R.paper[3] else R.wood[2])
        px.vline(32, 0, 10, R.gold[2])
        px.shade(ellipse(32f, 16f, 14f, 3f), facing(0f, -0.5f, 1f), R.gold, spec = true)
        for (i in 0 until 5) {
            val x = 20 + i * 6
            px.vline(x, 9, 14, R.cream[4]); px.set(x, 8, if ((i + f) % 2 == 0) R.ember[4] else R.ember[5])
            px.glow(x + 0.5f, 8f, 4f, R.gold[5], 70, 2)
        }
        for (i in 0 until 4) px.set(22 + i * 6, 19 + i % 2, R.glass[3])
        // Two dancers, little silhouettes.
        val sway = if (f % 2 == 0) 0 else 1
        px.shade(poly(22f + sway, 34f, 25f + sway, 24f, 28f + sway, 34f), facing(), R.red, lo = 1, hi = 3)
        px.shade(ellipse(25f + sway, 22f, 2f, 2f), facing(), R.cream, lo = 3)
        px.shade(rect(37f - sway, 25f, 4f, 9f), facing(), R.iron, lo = 0, hi = 1)
        px.shade(ellipse(39f - sway, 22.5f, 2f, 2f), facing(), R.cream, lo = 3)
    }

    private fun pyramids(px: Px, f: Int) {
        px.shade(ellipse(50f, 10f, 5f, 5f), facing(), R.gold, outline = false, lo = 4)
        ground(px, R.belly, 33)
        for ((cx, h, w) in listOf(Triple(20f, 18f, 17f), Triple(40f, 13f, 12f))) {
            px.shade(poly(cx - w, 34f, cx, 34f - h, cx + w, 34f), { x, _ -> V3(if (x < cx) -0.6f else 0.5f, -0.2f, 1f) }, R.belly, lo = 1, hi = 4)
        }
        // A camel crossing.
        val x = 50 - f * 2
        px.rect(x, 34, 6, 2, R.wood[3]); px.set(x + 2, 33, R.wood[3]); px.rect(x + 6, 31, 1, 3, R.wood[3]); px.set(x + 7, 31, R.wood[3])
        px.vline(x, 36, 37, R.wood[2]); px.vline(x + 5, 36, 37, R.wood[2])
    }

    private fun bulb(px: Px, f: Int) {
        px.glow(32f, 18f, 18f, R.gold[5], 60 + f * 10)
        px.shade(ellipse(32f, 18f, 9f, 9f), dome(30f, 16f, 9f, 9f), R.gold, spec = true, lo = 3)
        px.shade(rect(28f, 26f, 8f, 6f), column(32f, 4f), R.iron, lo = 1, texture = { _, y -> if (y % 2 == 0) -0.3f else 0f })
        px.line(29, 20, 31, 15, R.ember[2]); px.line(31, 15, 33, 20, R.ember[2]); px.line(33, 20, 35, 15, R.ember[2])
        for (i in 0 until 6) {
            val a = (i * 60f + f * 15f) * PI.toFloat() / 180f
            val x = (32 + cos(a) * 16).toInt(); val y = (18 + sin(a) * 12).toInt()
            px.set(x, y, WHITE); px.set(x + 1, y, R.gold[5], 160)
        }
    }

    private fun lake(px: Px, f: Int) {
        px.shade(ellipse(32f, 12f, 6f, 6f), facing(), R.paper, outline = false, lo = 3)
        for (y in 26 until H) for (x in 0 until W) px.set(x, y, R.night[if ((x + y * 3 + f * 3) % 11 == 0) 4 else 2])
        for (y in 0 until 8) { val w = 5 - y / 2 + if ((y + f) % 2 == 0) 1 else 0; px.hline(32 - w, 32 + w, 28 + y * 2, R.paper[3]) }
        // Notes drifting up.
        for (i in 0 until 2) {
            val p = ((f + i * 2) % 4) / 4f
            val nx = (14 + i * 32 + p * 4).toInt(); val ny = (22 - p * 10).toInt()
            px.vline(nx + 2, ny - 4, ny, R.lilac[5]); px.rect(nx, ny, 3, 2, R.lilac[5])
        }
    }

    private fun sheep(px: Px, f: Int) {
        ground(px, R.leaf, 34)
        // The fence.
        for (x in listOf(28, 36)) px.vline(x, 26, 34, R.wood[3])
        px.hline(26, 38, 28, R.wood[4]); px.hline(26, 38, 31, R.wood[4])
        val p = f / FRAMES.toFloat()
        val sx = 10f + p * 40f
        val sy = 28f - sin(p * PI.toFloat()) * 12f
        px.shade(union(ellipse(sx, sy, 6f, 4.5f), ellipse(sx - 3f, sy - 2f, 3f, 3f), ellipse(sx + 3f, sy - 2f, 3f, 3f)), dome(sx - 1f, sy - 2f, 6f, 5f), R.fluff, lo = 2)
        px.shade(ellipse(sx + 6f, sy, 2.4f, 2f), facing(), R.iron, lo = 0, hi = 1)
        for (dx in listOf(-3, 2)) px.vline((sx + dx).toInt(), (sy + 4).toInt(), (sy + 6).toInt(), R.iron[0])
        // Counting.
        val n = abs(f % 3) + 1
        for (i in 0 until n) px.rect(4 + i * 3, 4, 2, 2, WHITE)
    }
}
