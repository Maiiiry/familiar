package app.familiar

import android.graphics.Bitmap
import app.familiar.domain.Genre
import app.familiar.domain.Mood
import app.familiar.domain.Species
import app.familiar.domain.Stage
import app.familiar.ui.pix.Look
import app.familiar.ui.pix.Px
import app.familiar.ui.pix.Sprites
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Paints the sprites to PNG sheets (run with `-Prender`), so they can be looked at without a phone. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class RenderArt {
    private val out = File(System.getProperty("render.out") ?: "build/art").apply { mkdirs() }

    private fun sheet(name: String, cells: List<List<Px?>>, scale: Int = 4, bg: Int = 0xFFF3EBDD.toInt()) {
        val cw = Sprites.SIZE * scale
        val rows = cells.size
        val cols = cells.maxOf { it.size }
        val bmp = Bitmap.createBitmap(cols * cw, rows * cw, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(bg)
        cells.forEachIndexed { r, row ->
            row.forEachIndexed { c, px ->
                if (px == null) return@forEachIndexed
                for (y in 0 until px.h) for (x in 0 until px.w) {
                    val p = px.c[y * px.w + x]
                    val a = (p ushr 24) and 0xFF
                    if (a == 0) continue
                    val col = blend(bg, p, a / 255f)
                    for (j in 0 until scale) for (i in 0 until scale) bmp.setPixel(c * cw + x * scale + i, r * cw + y * scale + j, col)
                }
            }
        }
        File(out, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun blend(a: Int, b: Int, t: Float): Int {
        fun ch(s: Int) = (((a shr s) and 0xFF) + ((((b shr s) and 0xFF) - ((a shr s) and 0xFF)) * t)).toInt()
        return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }

    private val tails = Genre.tails.map { it.argb.toInt() }

    private fun look(sp: Species, st: Stage, mood: Mood = Mood.NONE, stone: Float = 0f) = Look(
        species = sp, stage = st, mood = mood, stone = stone, hatch = 0.6f,
        tails = tails.take(when (st) { Stage.BABY -> 1; Stage.YOUNG -> 3; Stage.ADULT -> 6; else -> 9 }),
        hoard = tails.take(when (st) { Stage.BABY -> 0; Stage.YOUNG -> 3; Stage.ADULT -> 8; else -> 10 }) + tails.take(if (st == Stage.ANCIENT) 4 else 0),
        stars = when (st) { Stage.BABY -> 3; Stage.YOUNG -> 10; Stage.ADULT -> 22; else -> 40 },
        items = listOf("меч", "кольцо", "письмо", "зелье", "ключ", "яблоко", "череп", "корона", "перо", "карта").take(when (st) { Stage.BABY -> 1; Stage.YOUNG -> 4; else -> 10 }),
    )

    @Test
    fun stages() = sheet("stages", Species.entries.map { sp -> Stage.entries.map { Sprites.frame(look(sp, it), 0) } }, scale = 3)

    @Test
    fun moods() = sheet("moods", Species.entries.map { sp ->
        Mood.entries.map { Sprites.frame(look(sp, Stage.ADULT, it), 1) } + listOf(Sprites.frame(look(sp, Stage.ADULT, stone = 0.45f), 1), Sprites.frame(look(sp, Stage.ADULT, stone = 1f), 1))
    }, scale = 2)

    @Test
    fun hero() = sheet("hero", Species.entries.chunked(4).map { row -> row.map { Sprites.frame(look(it, Stage.ADULT), 0) } }, scale = 6)

    @Test
    fun anim() = sheet("anim", listOf(Species.KITSUNE, Species.MIMIC, Species.MOTH).map { sp -> List(Sprites.FRAMES) { Sprites.frame(look(sp, Stage.ADULT), it) } }, scale = 2)

    /** The launcher icon: a kitsune with three tails, pixel for pixel, on the night-violet ground. */
    @Test
    fun icon() {
        val res = File(System.getProperty("render.res") ?: return)
        val px = Sprites.frame(Look(Species.KITSUNE, Stage.ADULT, tails = tails.take(5)), 0)
        var x0 = px.w; var y0 = px.h; var x1 = 0; var y1 = 0
        for (y in 0 until px.h) for (x in 0 until px.w) if ((px.c[y * px.w + x] ushr 24) > 0) { x0 = minOf(x0, x); y0 = minOf(y0, y); x1 = maxOf(x1, x); y1 = maxOf(y1, y) }
        val w = x1 - x0 + 1
        val h = y1 - y0 + 1
        for ((dir, side) in listOf("mdpi" to 108, "hdpi" to 162, "xhdpi" to 216, "xxhdpi" to 324, "xxxhdpi" to 432)) {
            val k = maxOf(1, (side * 0.66f / maxOf(w, h)).toInt())
            fun draw(bmp: Bitmap, size: Int, scale: Int) {
                val ox = (size - w * scale) / 2
                val oy = (size - h * scale) / 2
                for (y in 0 until h) for (x in 0 until w) {
                    val p = px.c[(y0 + y) * px.w + x0 + x]
                    if ((p ushr 24) == 0) continue
                    for (j in 0 until scale) for (i in 0 until scale) bmp.setPixel(ox + x * scale + i, oy + y * scale + j, p)
                }
            }
            val fg = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
            draw(fg, side, k)
            File(res, "mipmap-$dir").mkdirs()
            File(res, "mipmap-$dir/ic_launcher_foreground.png").outputStream().use { fg.compress(Bitmap.CompressFormat.PNG, 100, it) }
            // Legacy square icon (48dp) for launchers without adaptive icons.
            val legacy = side * 48 / 108
            val lg = Bitmap.createBitmap(legacy, legacy, Bitmap.Config.ARGB_8888)
            val c = android.graphics.Canvas(lg)
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
            paint.shader = android.graphics.RadialGradient(legacy / 2f, legacy * 0.44f, legacy * 0.7f, 0xFF4A3488.toInt(), 0xFF17112A.toInt(), android.graphics.Shader.TileMode.CLAMP)
            c.drawRoundRect(0f, 0f, legacy.toFloat(), legacy.toFloat(), legacy * 0.22f, legacy * 0.22f, paint)
            c.drawBitmap(Bitmap.createScaledBitmap(fg, (legacy * 1.25f).toInt(), (legacy * 1.25f).toInt(), true), -legacy * 0.125f, -legacy * 0.125f, null)
            for (name in listOf("ic_launcher", "ic_launcher_round")) File(res, "mipmap-$dir/$name.png").outputStream().use { lg.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        // A big one for the README and the website.
        val docs = File(System.getProperty("render.docs") ?: return).apply { mkdirs() }
        val big = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val c = android.graphics.Canvas(big)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        paint.shader = android.graphics.RadialGradient(256f, 225f, 360f, 0xFF4A3488.toInt(), 0xFF17112A.toInt(), android.graphics.Shader.TileMode.CLAMP)
        c.drawRoundRect(0f, 0f, 512f, 512f, 112f, 112f, paint)
        val k = (512 * 0.78f / maxOf(w, h)).toInt()
        for (y in 0 until h) for (x in 0 until w) {
            val p = px.c[(y0 + y) * px.w + x0 + x]
            if ((p ushr 24) == 0) continue
            for (j in 0 until k) for (i in 0 until k) big.setPixel((512 - w * k) / 2 + x * k + i, (512 - h * k) / 2 + y * k + j, p)
        }
        File(docs, "logo.png").outputStream().use { big.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
