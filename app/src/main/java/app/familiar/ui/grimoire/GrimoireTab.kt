package app.familiar.ui.grimoire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.familiar.R
import app.familiar.domain.Genre
import app.familiar.ui.FamiliarData
import app.familiar.ui.Go
import app.familiar.ui.components.Segmented
import app.familiar.ui.components.UCard
import app.familiar.ui.dateShort
import app.familiar.ui.genreColor
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType

private enum class Page { SPELLS, CHRONICLE }

/** Quotes kept as spells, and the one-line chronicle of every chapter. */
@Composable
fun GrimoireTab(data: FamiliarData, go: Go) {
    var page by rememberSaveable { mutableStateOf(Page.SPELLS) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column {
                    Text(stringResource(R.string.tab_grimoire), style = UType.Display, modifier = Modifier.padding(top = 14.dp))
                    Text(stringResource(R.string.grimoire_text), style = UType.Sub)
                    Spacer(Modifier.height(12.dp))
                    Segmented(Page.entries, page, { stringResource(if (it == Page.SPELLS) R.string.spells else R.string.chronicle) }, { page = it })
                }
            }
            if (page == Page.SPELLS) {
                if (data.quotes.isEmpty()) item { Text(stringResource(R.string.no_quotes), style = UType.Sub, modifier = Modifier.padding(top = 12.dp)) }
                items(data.quotes, key = { it.id }) { q ->
                    val book = q.bookId?.let { data.book(it) }
                    UCard(onClick = { go.quote(q.id) }) {
                        Text("“${q.text}”", style = UType.Quote)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            listOfNotNull(book?.title, book?.author?.takeIf { it.isNotBlank() }, q.page?.let { stringResource(R.string.page_n, it) }).joinToString(" · ").ifEmpty { dateShort(q.created) },
                            style = UType.Small.copy(color = book?.let { genreColor(Genre.of(it.genre)) } ?: U.c.faint),
                        )
                    }
                }
            } else {
                val notes = data.readings.filter { it.note.isNotBlank() }.sortedByDescending { it.end }
                if (notes.isEmpty()) item { Text(stringResource(R.string.no_notes), style = UType.Sub, modifier = Modifier.padding(top = 12.dp)) }
                items(notes, key = { it.id }) { r ->
                    val book = data.book(r.bookId)
                    UCard(onClick = { go.book(r.bookId) }) {
                        Text(listOfNotNull(book?.title, dateShort(r.end), stringResource(R.string.page_range, r.fromPage, r.toPage)).joinToString(" · "), style = UType.Small.copy(color = book?.let { genreColor(Genre.of(it.genre)) } ?: U.c.faint))
                        Spacer(Modifier.height(4.dp))
                        Text(r.note, style = UType.Body.copy(fontSize = 15.sp))
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { go.quote() },
            containerColor = U.c.accent,
            contentColor = U.c.onAccent,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        ) { Icon(Icons.Rounded.Add, stringResource(R.string.quote_add)) }
    }
}
