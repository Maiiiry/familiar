package app.familiar.ui.ritual

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.familiar.R
import app.familiar.domain.Part
import app.familiar.ui.FamiliarData
import app.familiar.ui.Go
import app.familiar.ui.components.Den
import app.familiar.ui.components.FamiliarArt
import app.familiar.ui.components.PrimaryButton
import app.familiar.ui.components.Sparks
import app.familiar.ui.components.UCard
import app.familiar.ui.lastFeast
import app.familiar.ui.partName
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType
import kotlinx.coroutines.delay

/** The familiar eats what was read, and the bill of fare. */
@Composable
fun FeastScreen(data: FamiliarData, go: Go) {
    val feast by lastFeast.collectAsStateWithLifecycle()
    val r = feast
    if (r == null) {
        LaunchedEffect(Unit) { go.back() }
        return
    }
    var chewing by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(2400); chewing = false }
    BackHandler { go.back() }
    val pet = data.pets.firstOrNull { it.id == r.petId }
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Spacer(Modifier.height(16.dp))
            Den(Modifier.fillMaxWidth().height(280.dp)) {
                data.look(chew = chewing)?.let { FamiliarArt(it, Modifier.fillMaxSize().padding(top = 30.dp, bottom = 16.dp), fps = if (chewing) 10f else 6f) }
                Sparks(Modifier.fillMaxSize(), key = r.reading.id)
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.feast_title, pet?.name ?: ""), style = UType.Title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text(stringResource(R.string.mana_plus, r.feast.total), style = UType.Display.copy(color = U.c.mana), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            UCard {
                r.feast.parts.forEachIndexed { i, (part, n) ->
                    if (i > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp), color = U.c.line)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(partName(part, data.species, data.book(r.reading.bookId)?.audio == true), style = UType.Body, modifier = Modifier.weight(1f))
                        Text(if (n >= 0) "+$n" else "$n", style = UType.Strong.copy(color = if (part == Part.SNIFF) U.c.danger else U.c.mana))
                    }
                }
            }
            if (r.feast.sniffed) {
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.sniff_text, pet?.name ?: ""), style = UType.Muted)
            }
            if (r.reading.finishedBook) {
                Spacer(Modifier.height(12.dp))
                UCard {
                    Text(stringResource(R.string.book_done_title), style = UType.Strong.copy(color = U.c.gold))
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.book_done_text), style = UType.Muted)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        Box(Modifier.navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp)) {
            PrimaryButton(stringResource(R.string.done), { go.back() }, Modifier.fillMaxWidth())
        }
    }
}
