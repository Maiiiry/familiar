package app.familiar.ui.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import app.familiar.container
import app.familiar.ui.books
import app.familiar.ui.components.Chip
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import app.familiar.domain.Series
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.familiar.R
import app.familiar.data.Shelf
import app.familiar.domain.Genre
import app.familiar.ui.FamiliarData
import app.familiar.ui.Go
import app.familiar.ui.components.Bar
import app.familiar.ui.components.BookCover
import app.familiar.ui.shelfName
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType

@Composable
fun ShelfTab(data: FamiliarData, go: Go) {
    // Which shelf is open: a built-in one, the cycles, or one of your own ("own:<name>").
    var view by rememberSaveable { mutableStateOf(Shelf.READING.name) }
    val own = view.removePrefix(OWN).takeIf { view.startsWith(OWN) && it in data.settings.shelves }
    if (view.startsWith(OWN) && own == null) view = Shelf.READING.name
    val shelf: Shelf? = Shelf.entries.firstOrNull { it.name == view }
    val list = when {
        own != null -> data.books.filter { own in it.tagList }.sortedByDescending { it.added }
        shelf == Shelf.READING -> data.reading + data.books.filter { it.shelfOf == Shelf.PAUSED }
        shelf == Shelf.DONE -> data.books.filter { it.shelfOf == Shelf.DONE }.sortedByDescending { it.finished ?: it.added }
        shelf != null -> data.books.filter { it.shelfOf == shelf }
        else -> emptyList()
    }
    val container = androidx.compose.ui.platform.LocalContext.current.container
    var adding by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<String?>(null) }
    var removing by remember { mutableStateOf<String?>(null) }
    val cycles = remember(data.books) { Series.all(data.books) }
    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            GridCells.Fixed(3),
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { GridItemSpan(3) }) {
                Column {
                    Text(stringResource(R.string.tab_shelf), style = UType.Display, modifier = Modifier.padding(top = 14.dp))
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(Shelf.READING, Shelf.WANT, Shelf.DONE)) { sh -> Chip(shelfName(sh), view == sh.name, { view = sh.name }) }
                        item { Chip(stringResource(R.string.cycles), view == CYCLES, { view = CYCLES }) }
                        items(data.settings.shelves) { n -> Chip(n, own == n, { view = OWN + n }, U.c.gold) }
                        item { Chip("+ " + stringResource(R.string.shelf_new), false, { adding = true }, U.c.gold) }
                    }
                    if (own != null) Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(books(list.size), style = UType.Muted, modifier = Modifier.weight(1f))
                        androidx.compose.material3.TextButton(onClick = { renaming = own }) { Text(stringResource(R.string.shelf_rename), color = U.c.accent, style = UType.Button) }
                        androidx.compose.material3.TextButton(onClick = { removing = own }) { Text(stringResource(R.string.delete), color = U.c.muted, style = UType.Button) }
                    }
                    if (view == CYCLES && cycles.isEmpty()) Text(stringResource(R.string.cycles_empty), style = UType.Sub, modifier = Modifier.padding(top = 24.dp))
                    if (own != null && list.isEmpty()) Text(stringResource(R.string.shelf_own_empty), style = UType.Sub, modifier = Modifier.padding(top = 16.dp))
                    if (shelf != null && list.isEmpty()) Text(
                        stringResource(when (shelf) { Shelf.READING -> R.string.empty_reading; Shelf.WANT -> R.string.empty_want; else -> R.string.empty_done }),
                        style = UType.Sub, modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }
            if (view == CYCLES) items(cycles, key = { it.key }, span = { GridItemSpan(3) }) { c -> CycleCard(c) { go.series(c.name) } }
            items(list, key = { it.id }) { b ->
                Column(Modifier.clickable { go.book(b.id) }) {
                    BookCover(b.title, Genre.of(b.genre), Modifier.fillMaxWidth().aspectRatio(0.68f), cover = b.cover)
                    Spacer(Modifier.height(6.dp))
                    Text(b.title, style = UType.Small.copy(color = U.c.ink), maxLines = 2)
                    if (b.shelfOf == Shelf.READING || b.shelfOf == Shelf.PAUSED) {
                        Spacer(Modifier.height(4.dp))
                        if (b.pages > 0) Bar(b.fraction, Modifier.height(5.dp), color = if (b.shelfOf == Shelf.PAUSED) U.c.faint else U.c.accent, track = U.c.accentSoft)
                        if (b.shelfOf == Shelf.PAUSED) Text(shelfName(Shelf.PAUSED), style = UType.Small)
                    }
                    if (b.shelfOf == Shelf.DONE && b.rating != null) Text("★".repeat(b.rating), style = UType.Small.copy(color = U.c.gold))
                }
            }
        }
        FloatingActionButton(
            onClick = { go.editBook() },
            containerColor = U.c.accent,
            contentColor = U.c.onAccent,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        ) { Icon(Icons.Rounded.Add, stringResource(R.string.book_add)) }
    }
    if (adding) ShelfNameDialog(onDismiss = { adding = false }) { n ->
        container.scope.launch { Shelves.add(container, n) }
        view = OWN + n
    }
    renaming?.let { r -> ShelfNameDialog(r, onDismiss = { renaming = null }) { n -> container.scope.launch { Shelves.rename(container, r, n) }; view = OWN + n } }
    removing?.let { r ->
        app.familiar.ui.components.ConfirmDialog(
            title = stringResource(R.string.shelf_delete_title, r),
            text = stringResource(R.string.shelf_delete_text),
            confirm = stringResource(R.string.delete),
            danger = true,
            onConfirm = { container.scope.launch { Shelves.remove(container, r) }; view = Shelf.READING.name },
            onDismiss = { removing = null },
        )
    }
}

private const val OWN = "own:"
private const val CYCLES = "cycles"

/** A cycle: its covers fanned out, and how many volumes are read. */
@Composable
fun CycleCard(c: Series, onClick: () -> Unit) {
    app.familiar.ui.components.UCard(onClick = onClick) {
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy((-22).dp)) {
                c.books.take(4).forEach { b ->
                    BookCover(b.title, Genre.of(b.genre), Modifier.width(46.dp).height(66.dp), small = true, cover = b.cover)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(c.name, style = UType.Strong, maxLines = 2)
                Text(stringResource(R.string.cycle_of, c.done, c.books.size), style = UType.Muted.copy(color = if (c.complete) U.c.gold else U.c.muted))
                Spacer(Modifier.height(6.dp))
                Bar(c.done / c.books.size.toFloat(), Modifier.height(5.dp), color = U.c.gold, track = U.c.goldSoft)
            }
        }
    }
}
