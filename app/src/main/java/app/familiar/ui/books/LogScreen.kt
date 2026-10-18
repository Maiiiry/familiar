package app.familiar.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.familiar.R
import app.familiar.container
import app.familiar.data.Library
import app.familiar.domain.Species
import app.familiar.ui.FamiliarData
import app.familiar.ui.Go
import app.familiar.ui.components.TopBar
import app.familiar.ui.lastFeast
import app.familiar.ui.ritual.SittingForm
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Pages read away from the app (on the train, in bed), logged afterwards. */
@Composable
fun LogScreen(id: Long, data: FamiliarData, go: Go) {
    val container = LocalContext.current.container
    val book = data.book(id)
    if (book == null) {
        LaunchedEffect(Unit) { go.back() }
        return
    }
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().imePadding()) {
        TopBar(book.title, { go.back() })
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).navigationBarsPadding().padding(bottom = 24.dp)) {
            Text(stringResource(R.string.log_text), style = UType.Sub)
            // Listened to it instead (an audiobook, or the audio version of this one): count the minutes.
            var listened by remember { mutableStateOf(book.audio) }
            var minutes by remember { mutableStateOf("30") }
            if (!book.audio) app.familiar.ui.components.SwitchRow(stringResource(R.string.log_listened), stringResource(R.string.log_listened_sub), listened, { listened = it })
            if (listened && !book.audio) {
                app.familiar.ui.components.SectionTitle(stringResource(R.string.log_minutes))
                app.familiar.ui.components.UTextField(minutes, { minutes = it.filter(Char::isDigit).take(3) }, number = true, placeholder = "30")
            }
            androidx.compose.runtime.key(listened) {
            SittingForm(book, book.page, book.page, data.species == Species.SLIME, stringResource(R.string.feed), timed = listened && !book.audio, listen = listened && !book.audio) { input ->
                container.scope.launch {
                    val now = System.currentTimeMillis()
                    val mins = if (listened && !book.audio) (minutes.toIntOrNull() ?: 0).coerceIn(0, 600) else 0
                    val r = Library.sit(
                        container.db.dao(), book, book.page, input.toPage, now - mins * 60_000L, now, timed = mins > 0,
                        listened = listened, item = input.item, finish = input.finish,
                    )
                    container.changed()
                    withContext(Dispatchers.Main) {
                        lastFeast.value = r
                        if (r != null) go.feast() else go.back()
                    }
                }
            }
            }
        }
    }
}
