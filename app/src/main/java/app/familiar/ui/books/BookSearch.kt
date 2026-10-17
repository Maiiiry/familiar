package app.familiar.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.familiar.R
import app.familiar.data.Covers
import app.familiar.domain.Genre
import app.familiar.ui.components.BookCover
import app.familiar.ui.components.UCard
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType
import kotlinx.coroutines.launch

/** Look a book up by title or author; picking one fills the form, cover included. */
@Composable
fun BookSearch(onPick: (Covers.Found) -> Unit) {
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    var query by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Covers.Found>?>(null) }
    var failed by remember { mutableStateOf(false) }
    fun go() {
        if (query.isBlank() || busy) return
        focus.clearFocus()
        busy = true
        failed = false
        scope.launch {
            val r = Covers.search(query)
            failed = r == null
            results = r.orEmpty()
            busy = false
        }
    }
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it.take(100) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_hint), color = U.c.faint) },
            singleLine = true,
            shape = U.RSm,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { go() }),
            trailingIcon = {
                if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = U.c.accent)
                else IconButton(onClick = { go() }) { Icon(Icons.Rounded.Search, stringResource(R.string.search), tint = U.c.accent) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = U.c.surface,
                unfocusedContainerColor = U.c.surface,
                unfocusedBorderColor = U.c.line,
                focusedBorderColor = U.c.accent,
            ),
        )
        Text(stringResource(R.string.search_note), style = UType.Small.copy(color = U.c.muted), modifier = Modifier.padding(top = 4.dp))
        val list = results
        when {
            failed -> Text(stringResource(R.string.search_failed), style = UType.Muted.copy(color = U.c.danger), modifier = Modifier.padding(top = 8.dp))
            list != null && list.isEmpty() -> Text(stringResource(R.string.search_nothing), style = UType.Muted, modifier = Modifier.padding(top = 8.dp))
            list != null -> {
                Spacer(Modifier.height(8.dp))
                UCard(padding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)) {
                    list.take(8).forEachIndexed { i, f ->
                        if (i > 0) HorizontalDivider(color = U.c.line)
                        Row(
                            Modifier.fillMaxWidth().clickable { onPick(f); results = null; query = "" }.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.width(40.dp).height(58.dp).clip(U.RSm).background(U.c.surface2)) {
                                BookCover(f.title, Genre.OTHER, Modifier.width(40.dp).height(58.dp), small = true, cover = f.thumb)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(f.title, style = UType.Strong.copy(fontSize = UType.Body.fontSize), maxLines = 2)
                                Text(
                                    listOfNotNull(f.author.takeIf { it.isNotBlank() }, f.year?.toString(), f.pages?.let { stringResource(R.string.pages_short, it) }).joinToString(" · "),
                                    style = UType.Small, maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
