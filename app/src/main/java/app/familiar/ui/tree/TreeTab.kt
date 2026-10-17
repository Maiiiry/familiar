package app.familiar.ui.tree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.familiar.R
import app.familiar.container
import app.familiar.data.Library
import app.familiar.domain.Genre
import app.familiar.domain.Mana
import app.familiar.domain.Quests
import app.familiar.ui.FamiliarData
import app.familiar.ui.Go
import app.familiar.ui.books
import app.familiar.ui.components.Bar
import app.familiar.ui.components.SectionTitle
import app.familiar.ui.components.SoftButton
import app.familiar.ui.components.UCard
import app.familiar.ui.components.pixels
import app.familiar.ui.components.rememberTime
import app.familiar.ui.locale
import app.familiar.ui.pix.Scenes
import app.familiar.ui.questText
import app.familiar.ui.questTitle
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.TextStyle
import kotlin.math.hypot

@Composable
fun TreeTab(data: FamiliarData, go: Go) {
    val container = LocalContext.current.container
    val s = data.stats
    val finished = s.finished
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 24.dp)) {
        Text(stringResource(R.string.tab_tree), style = UType.Display, modifier = Modifier.padding(top = 14.dp))
        Text(if (finished.isEmpty()) stringResource(R.string.tree_empty) else stringResource(R.string.tree_count, books(finished.size)), style = UType.Sub)
        Spacer(Modifier.height(12.dp))
        val colors = finished.map { Genre.of(it.genre).argb.toInt() }
        val dark = U.c.dark
        val frames by produceState<List<Pair<app.familiar.ui.pix.Px, List<Scenes.Fruit>>>?>(null, colors, dark) {
            value = withContext(Dispatchers.Default) { List(12) { Scenes.tree(colors, it, dark) } }
        }
        val bitmaps = remember(frames) { frames?.map { it.first.bitmap() } }
        val t = rememberTime()
        Canvas(
            Modifier.fillMaxWidth().aspectRatio(128f / 112f).clip(U.RXl).background(U.c.surface2)
                .pointerInput(frames) {
                    val fr = frames ?: return@pointerInput
                    detectTapGestures { o ->
                        val img = fr[0].first
                        val k = maxOf(1, minOf(size.width / img.w, size.height / img.h))
                        val ox = (size.width - img.w * k) / 2
                        val oy = (size.height - img.h * k) / 2
                        val px = (o.x - ox) / k
                        val py = (o.y - oy) / k
                        fr[0].second.minByOrNull { hypot(it.x - px, it.y - py) }
                            ?.takeIf { hypot(it.x - px, it.y - py) < 6f }
                            ?.let { go.book(finished[it.index].id) }
                    }
                }
        ) { bitmaps?.let { pixels(it[(t * 4).toInt() % it.size]) } }
        Text(stringResource(R.string.tree_hint), style = UType.Small.copy(color = U.c.muted), modifier = Modifier.padding(top = 6.dp))

        val month = java.time.YearMonth.from(s.today)
        SectionTitle(stringResource(R.string.guild, month.month.getDisplayName(TextStyle.FULL_STANDALONE, locale())))
        UCard {
            Text(stringResource(R.string.guild_text, Mana.QUEST), style = UType.Muted)
            Quests.state(s).forEach { q ->
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = U.c.line)
                val claimed = q.key in data.settings.claimed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(questTitle(q.quest), style = UType.Strong)
                        Text(questText(q.quest), style = UType.Muted)
                    }
                    if (claimed) Icon(Icons.Rounded.CheckCircle, null, tint = U.c.leaf)
                }
                Spacer(Modifier.height(8.dp))
                if (q.done && !claimed) SoftButton(stringResource(R.string.claim, Mana.QUEST), {
                    container.scope.launch {
                        Library.reward(container.db.dao(), Mana.QUEST)
                        container.settings.update { it.copy(claimed = it.claimed + q.key) }
                        container.changed()
                    }
                }, Modifier.fillMaxWidth(), color = U.c.gold, soft = U.c.goldSoft)
                else Row(verticalAlignment = Alignment.CenterVertically) {
                    Bar(q.progress / q.quest.target.toFloat(), Modifier.weight(1f), color = U.c.gold, track = U.c.goldSoft)
                    Spacer(Modifier.width(10.dp))
                    Text("${q.progress} / ${q.quest.target}", style = UType.Small)
                }
            }
        }

        SectionTitle(stringResource(R.string.saga))
        UCard(onClick = { go.saga() }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.HistoryEdu, null, tint = U.c.accent)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.saga_title, s.today.year), style = UType.Strong)
                    Text(stringResource(R.string.saga_sub), style = UType.Muted)
                }
            }
        }
    }
}
