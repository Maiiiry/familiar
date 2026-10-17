package app.familiar.ui.grimoire

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import app.familiar.data.Quote
import app.familiar.ui.FamiliarData
import app.familiar.ui.Go
import app.familiar.ui.components.Chip
import app.familiar.ui.components.ConfirmDialog
import app.familiar.ui.components.PrimaryButton
import app.familiar.ui.components.SectionTitle
import app.familiar.ui.components.TopBar
import app.familiar.ui.components.UTextField
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Writes a quote into the grimoire; text shared from another app arrives here prefilled. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuoteScreen(id: Long, bookId: Long, data: FamiliarData, shared: MutableStateFlow<String?>, go: Go) {
    val context = LocalContext.current
    val container = context.container
    val old = data.quotes.firstOrNull { it.id == id }
    var text by remember { mutableStateOf(old?.text ?: shared.value ?: "") }
    LaunchedEffect(Unit) { shared.value = null }
    var book by remember { mutableStateOf(old?.bookId ?: bookId.takeIf { it > 0 } ?: data.reading.firstOrNull()?.id) }
    var page by remember { mutableStateOf(old?.page?.toString() ?: "") }
    var deleting by remember { mutableStateOf(false) }
    val tasted = stringResource(R.string.quote_tasted)
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().imePadding()) {
        TopBar(stringResource(if (old == null) R.string.quote_add else R.string.quote_edit), { go.back() }) {
            if (old != null) {
                val b = old.bookId?.let { data.book(it) }
                IconButton(onClick = {
                    val body = "“${old.text}”" + (b?.let { "\n— ${it.title}" + if (it.author.isNotBlank()) ", ${it.author}" else "" } ?: "")
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, body), null))
                }) { Icon(Icons.Rounded.Share, stringResource(R.string.share), tint = U.c.muted) }
                IconButton(onClick = { deleting = true }) { Icon(Icons.Rounded.Delete, stringResource(R.string.delete), tint = U.c.muted) }
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            UTextField(text, { text = it.take(1500) }, placeholder = stringResource(R.string.quote_hint), minLines = 5)
            Text(stringResource(R.string.quote_share_hint), style = UType.Small.copy(color = U.c.muted), modifier = Modifier.padding(top = 6.dp))
            val candidates = data.books.filter { it.shelfOf != app.familiar.data.Shelf.WANT }.take(12)
            if (candidates.isNotEmpty()) {
                SectionTitle(stringResource(R.string.quote_book))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    candidates.forEach { b -> Chip(b.title.take(28), book == b.id, { book = if (book == b.id) null else b.id }) }
                }
            }
            SectionTitle(stringResource(R.string.quote_page))
            UTextField(page, { page = it.filter(Char::isDigit).take(5) }, number = true, placeholder = "—")
            Spacer(Modifier.height(16.dp))
        }
        PrimaryButton(
            stringResource(R.string.save),
            {
                val q = (old ?: Quote(text = "", created = System.currentTimeMillis())).copy(text = text.trim(), bookId = book, page = page.toIntOrNull())
                container.scope.launch {
                    val dao = container.db.dao()
                    if (old == null) {
                        val mana = Library.quote(dao, q)
                        withContext(Dispatchers.Main) { Toast.makeText(context, tasted.format(mana), Toast.LENGTH_SHORT).show() }
                    } else dao.updateQuote(q)
                    container.changed()
                }
                go.back()
            },
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp).navigationBarsPadding(),
            enabled = text.isNotBlank(),
        )
    }
    if (deleting && old != null) ConfirmDialog(
        title = stringResource(R.string.quote_delete_title),
        text = stringResource(R.string.quote_delete_text),
        confirm = stringResource(R.string.delete),
        danger = true,
        onConfirm = { container.write { dao().deleteQuote(old) }; go.back() },
        onDismiss = { deleting = false },
    )
}
