package app.familiar.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import app.familiar.domain.Item
import app.familiar.ui.pix.Look
import app.familiar.ui.pix.R
import app.familiar.ui.pix.Room
import app.familiar.ui.pix.Sprites
import app.familiar.ui.theme.U
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

private data class RoomKey(val placed: Set<Item>, val books: List<Int>, val night: Boolean)

private object RoomCache {
    private var key: RoomKey? = null
    private var frames: List<ImageBitmap>? = null

    @Synchronized fun get(k: RoomKey) = if (k == key) frames else null
    @Synchronized fun put(k: RoomKey, f: List<ImageBitmap>) { key = k; frames = f }
}

/**
 * The den with the familiar in it, both in the same pixels. Tap to make it hop, stroke it to make
 * it purr; a [toss] number that changes throws a ball of yarn for it to chase.
 */
@Composable
fun DenScene(
    look: Look,
    placed: Set<Item>,
    books: List<Int>,
    night: Boolean,
    modifier: Modifier = Modifier,
    toss: Int = 0,
    onTap: () -> Unit = {},
    onStroke: () -> Unit = {},
) {
    val key = RoomKey(placed, books, night)
    val room by produceState(RoomCache.get(key), key) {
        value = RoomCache.get(key) ?: withContext(Dispatchers.Default) {
            List(Room.FRAMES) { Room.scene(placed, books, night, it).bitmap() }
        }.also { RoomCache.put(key, it) }
    }
    val pet by produceState(null as List<ImageBitmap>?, look) { value = withContext(Dispatchers.Default) { framesOf(look) } }
    val t = rememberTime()
    val hop = remember { Animatable(0f) }
    val ball = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(toss) {
        if (toss == 0) return@LaunchedEffect
        ball.snapTo(0f)
        launch { ball.animateTo(1f, tween(1600)) }
        repeat(3) {
            hop.animateTo(1f, tween(160))
            hop.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }
    val yarn = R.pink[2]
    val yarnDark = R.pink[0]
    var stroked = 0f
    Canvas(
        modifier
            .clip(U.RXl)
            .pointerInput(Unit) {
                detectTapGestures {
                    onTap()
                    scope.launch {
                        hop.animateTo(1f, tween(140))
                        hop.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(onDragStart = { stroked = 0f }) { change, amount ->
                    change.consume()
                    stroked += amount.getDistance()
                    if (stroked > 260f) { stroked = 0f; onStroke() }
                }
            }
    ) {
        val frames = room ?: return@Canvas
        val k = maxOf(1, minOf(size.width / Room.W, size.height / Room.H).toInt())
        // Fill the box: the room is centred and its edge colours run out to the sides.
        val ox = ((size.width - Room.W * k) / 2).toInt()
        val oy = ((size.height - Room.H * k) / 2).toInt()
        val f = frames[(t * 3).toInt() % frames.size]
        drawRect(androidx.compose.ui.graphics.Color(0xFF2A2030), size = size)
        drawImage(f, dstOffset = IntOffset(ox, oy), dstSize = IntSize(Room.W * k, Room.H * k), filterQuality = FilterQuality.None)
        pet?.let { p ->
            val img = p[(t * 6).toInt() % p.size]
            val px = ox + (Room.W - img.width) / 2 * k
            val py = oy + (Room.FLOOR_Y - img.height + 3) * k - (hop.value * 6).toInt() * k
            drawImage(img, dstOffset = IntOffset(px, py), dstSize = IntSize(img.width * k, img.height * k), filterQuality = FilterQuality.None)
        }
        // The ball of yarn: rolls in from the right, bounces and rests.
        if (ball.value < 1f) {
            val b = ball.value
            val x = ox + (Room.W - 8 - b * 56) * k
            val y = oy + (Room.FLOOR_Y - 4 - kotlin.math.abs(sin(b * PI * 3)).toFloat() * 18 * (1 - b)) * k
            val r = 3 * k.toFloat()
            drawCircle(yarnColor(yarnDark), r + k, Offset(x, y))
            drawCircle(yarnColor(yarn), r, Offset(x, y))
            drawRect(yarnColor(yarnDark), Offset(x - k, y - k), Size(k * 2f, k.toFloat()))
        }
    }
}

private fun yarnColor(c: Int) = androidx.compose.ui.graphics.Color(c)
