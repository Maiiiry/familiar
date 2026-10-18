package app.familiar.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import app.familiar.ui.components.BookCover
import androidx.compose.runtime.Composable
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
import app.familiar.data.Book
import app.familiar.data.Shelf
import app.familiar.domain.Genre
import app.familiar.ui.FamiliarData
import app.familiar.ui.Go
import app.familiar.ui.components.Chip
import app.familiar.ui.components.PrimaryButton
import app.familiar.ui.components.SectionTitle
import app.familiar.ui.components.TopBar
import app.familiar.ui.components.UTextField
import app.familiar.ui.genreColor
import app.familiar.ui.genreName
import app.familiar.ui.shelfName
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookEditScreen(id: Long, data: FamiliarData, go: Go, newSeries: String = "") {
    val container = LocalContext.current.container
    val old = data.book(id)
    var title by remember { mutableStateOf(old?.title ?: "") }
    var author by remember { mutableStateOf(old?.author ?: "") }
    var pages by remember { mutableStateOf(old?.pages?.takeIf { it > 0 }?.toString() ?: "") }
    var page by remember { mutableStateOf(old?.page?.takeIf { it > 0 }?.toString() ?: "") }
    var genre by remember { mutableStateOf(old?.let { Genre.of(it.genre) } ?: Genre.FANTASY) }
    var shelf by remember { mutableStateOf(old?.shelfOf ?: Shelf.READING) }
    var cover by remember { mutableStateOf(old?.cover ?: "") }
    var audio by remember { mutableStateOf(old?.audio ?: false) }
    var series by remember { mutableStateOf(old?.series ?: newSeries) }
    // A new volume of a cycle gets the next number.
    var tags by remember { mutableStateOf(old?.tagList?.toSet() ?: emptySet()) }
    var volume by remember {
        mutableStateOf(old?.volume?.toString() ?: if (newSeries.isBlank()) "" else ((data.books.filter { it.series.equals(newSeries, true) }.mapNotNull { it.volume }.maxOrNull() ?: 0) + 1).toString())
    }
    // The length is optional: 0 means not known yet.
    val total = pages.toIntOrNull()?.takeIf { it in 1..20000 } ?: 0
    val ok = title.isNotBlank() && (pages.isBlank() || pages.toIntOrNull()?.let { it in 1..20000 } == true)
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().imePadding()) {
        TopBar(stringResource(if (old == null) R.string.book_add else R.string.book_edit), { go.back() })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            SectionTitle(stringResource(R.string.search_title))
            BookSearch { f ->
                title = f.title
                author = f.author
                f.pages?.let { pages = it.toString() }
                cover = f.cover
            }
            SectionTitle(stringResource(R.string.f_cover))
            CoverPicker(title, genre, cover) { cover = it }
            SectionTitle(stringResource(R.string.f_title))
            UTextField(title, { title = it.take(120) }, placeholder = stringResource(R.string.f_title_hint))
            SectionTitle(stringResource(R.string.f_author))
            UTextField(author, { author = it.take(80) }, placeholder = stringResource(R.string.f_author_hint))
            Spacer(Modifier.height(8.dp))
            app.familiar.ui.components.SwitchRow(stringResource(R.string.f_audio), stringResource(R.string.f_audio_sub), audio, { audio = it })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    SectionTitle(stringResource(if (audio) R.string.f_minutes else R.string.f_pages))
                    UTextField(pages, { pages = it.filter(Char::isDigit).take(5) }, number = true, placeholder = stringResource(R.string.f_pages_optional))
                }
                Column(Modifier.weight(1f)) {
                    SectionTitle(stringResource(if (audio) R.string.f_minute_now else R.string.f_page_now))
                    UTextField(page, { page = it.filter(Char::isDigit).take(5) }, number = true, placeholder = "0")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(2f)) {
                    SectionTitle(stringResource(R.string.f_series))
                    UTextField(series, { series = it.take(80) }, placeholder = stringResource(R.string.f_series_hint))
                }
                Column(Modifier.weight(1f)) {
                    SectionTitle(stringResource(R.string.f_volume))
                    UTextField(volume, { volume = it.filter(Char::isDigit).take(3) }, number = true, placeholder = "1")
                }
            }
            val known = remember(data.books) { app.familiar.domain.Series.all(data.books).map { it.name } }
            if (known.isNotEmpty() && series.isBlank()) FlowRow(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                known.take(6).forEach { n -> Chip(n.take(28), false, { series = n }) }
            }
            SectionTitle(stringResource(R.string.f_genre))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Genre.entries.forEach { g -> Chip(genreName(g), genre == g, { genre = g }, genreColor(g)) }
            }
            Text(stringResource(R.string.f_genre_note), style = UType.Small.copy(color = U.c.muted), modifier = Modifier.padding(top = 6.dp))
            SectionTitle(stringResource(R.string.f_shelves))
            ShelfChips(data.settings.shelves, tags) { tags = it }
            if (old == null) {
                SectionTitle(stringResource(R.string.f_shelf))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Shelf.READING, Shelf.WANT).forEach { s -> Chip(shelfName(s), shelf == s, { shelf = s }) }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        PrimaryButton(
            stringResource(R.string.save),
            {
                val now = System.currentTimeMillis()
                val p = (page.toIntOrNull() ?: 0).coerceIn(0, if (total > 0) total else 99_999)
                val b = (old ?: Book(title = "", pages = total, genre = genre.name, added = now)).copy(
                    title = title.trim(), author = author.trim(), pages = total, genre = genre.name, page = p, cover = cover, audio = audio, tags = tags.joinToString("\n"), series = series.trim(), volume = volume.toIntOrNull()?.takeIf { series.isNotBlank() },
                    shelf = if (old == null) shelf.name else old.shelf,
                    started = old?.started ?: if (shelf == Shelf.READING) now else null,
                )
                container.write { if (old == null) dao().insertBook(b) else dao().updateBook(b) }
                go.back()
            },
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp).navigationBarsPadding(),
            enabled = ok,
        )
    }
}
