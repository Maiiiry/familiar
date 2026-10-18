package app.familiar.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import app.familiar.domain.Item
import app.familiar.ui.pix.Look
import app.familiar.ui.pix.Room
import app.familiar.ui.pix.Sprites
import app.familiar.ui.theme.U
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class RoomKey(val placed: Set<Item>, val books: List<Int>, val night: Boolean)

private object RoomCache {
    private var key: RoomKey? = null
    private var frames: List<ImageBitmap>? = null

    @Synchronized fun get(k: RoomKey) = if (k == key) frames else null
    @Synchronized fun put(k: RoomKey, f: List<ImageBitmap>) { key = k; frames = f }
}

/**
 * The den with the familiar in it, both in the same pixels. Tap to make it hop, stroke it to make
 * it purr.
 */
@Composable
fun DenScene(
    look: Look,
    placed: Set<Item>,
    books: List<Int>,
    night: Boolean,
    modifier: Modifier = Modifier,
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
    val scope = rememberCoroutineScope()
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
                // Sideways strokes only, so a vertical swipe still scrolls the page.
                detectHorizontalDragGestures(onDragStart = { stroked = 0f }) { change, amount ->
                    change.consume()
                    stroked += kotlin.math.abs(amount)
                    if (stroked > 260f) { stroked = 0f; onStroke() }
                }
            }
    ) {
        val frames = room ?: return@Canvas
        // Fill the box in whole-pixel steps, cropping a little off the edges rather than leaving a frame.
        val k = maxOf(1, kotlin.math.ceil(maxOf(size.width / Room.W, size.height / Room.H)).toInt())
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
    }
}

