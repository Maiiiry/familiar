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
import app.familiar.ui.components.Segmented
import app.familiar.ui.shelfName
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType

@Composable
fun ShelfTab(data: FamiliarData, go: Go) {
    var shelf by rememberSaveable { mutableStateOf(Shelf.READING) }
    val list = when (shelf) {
        Shelf.READING -> data.reading + data.books.filter { it.shelfOf == Shelf.PAUSED }
        Shelf.DONE -> data.books.filter { it.shelfOf == Shelf.DONE }.sortedByDescending { it.finished ?: it.added }
        else -> data.books.filter { it.shelfOf == shelf }
    }
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
                    Segmented(listOf(Shelf.READING, Shelf.WANT, Shelf.DONE), shelf, { shelfName(it) }, { shelf = it })
                    if (list.isEmpty()) Text(
                        stringResource(when (shelf) { Shelf.READING -> R.string.empty_reading; Shelf.WANT -> R.string.empty_want; else -> R.string.empty_done }),
                        style = UType.Sub, modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }
            items(list, key = { it.id }) { b ->
                Column(Modifier.clickable { go.book(b.id) }) {
                    BookCover(b.title, Genre.of(b.genre), Modifier.fillMaxWidth().aspectRatio(0.68f))
                    Spacer(Modifier.height(6.dp))
                    Text(b.title, style = UType.Small.copy(color = U.c.ink), maxLines = 2)
                    if (b.shelfOf == Shelf.READING || b.shelfOf == Shelf.PAUSED) {
                        Spacer(Modifier.height(4.dp))
                        Bar(b.fraction, Modifier.height(5.dp), color = if (b.shelfOf == Shelf.PAUSED) U.c.faint else U.c.accent, track = U.c.accentSoft)
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
}
