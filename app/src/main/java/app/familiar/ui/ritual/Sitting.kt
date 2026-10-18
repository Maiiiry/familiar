package app.familiar.ui.ritual

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.familiar.R
import app.familiar.data.Book
import app.familiar.ui.components.PrimaryButton
import app.familiar.ui.components.SectionTitle
import app.familiar.ui.components.Stepper
import app.familiar.ui.components.SwitchRow
import app.familiar.ui.components.UCard
import app.familiar.ui.components.UTextField
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType

class SittingInput(val toPage: Int, val item: String, val finish: Boolean)

/**
 * Where you stopped and (for a slime) what it should swallow. After a [timed] ritual the page can
 * be left out: then the familiar eats the minutes by the candle.
 */
@Composable
fun SittingForm(book: Book, fromPage: Int, startPage: Int, slime: Boolean, button: String, timed: Boolean = false, listen: Boolean = false, onSave: (SittingInput) -> Unit) {
    var page by remember { mutableIntStateOf(startPage.coerceIn(fromPage, book.cap)) }
    var typed by remember { mutableStateOf("") }
    var item by remember { mutableStateOf("") }
    var finish by remember { mutableStateOf(false) }
    // After listening the page in the text is often unknown: skipping it is the default then.
    var noPage by remember { mutableStateOf(listen) }
    val known = book.pages > 0
    Column {
        SectionTitle(stringResource(R.string.stopped_at))
        UCard {
            if (timed && !book.audio) SwitchRow(stringResource(R.string.no_page), stringResource(if (listen) R.string.no_page_listen else R.string.no_page_sub), noPage, { noPage = it })
            if (!noPage) {
                Stepper(
                    if (book.audio) stringResource(R.string.minute_at, app.familiar.ui.hm(page)) else stringResource(R.string.page_n, page), "+${page - fromPage}",
                    { page = (page - 1).coerceAtLeast(fromPage) }, { page = (page + 1).coerceAtMost(book.cap) }, color = U.c.accent,
                )
                UTextField(typed, {
                    typed = it.filter(Char::isDigit).take(5)
                    typed.toIntOrNull()?.let { v -> page = v.coerceIn(fromPage, book.cap) }
                }, number = true, placeholder = when {
                    book.audio -> stringResource(R.string.minute_type, book.pages)
                    known -> stringResource(R.string.page_type, book.pages)
                    else -> stringResource(R.string.page_type_any)
                })
                Spacer(Modifier.height(6.dp))
                Text(stringResource(if (book.audio) R.string.listened_now else R.string.read_now, app.familiar.ui.amount(page - fromPage, book.audio)), style = UType.Small.copy(color = U.c.muted))
            }
            SwitchRow(stringResource(R.string.finished_book), null, finish || (known && page >= book.pages && !noPage), { finish = it; if (it && known && !noPage) page = book.pages })
        }
        if (slime) {
            SectionTitle(stringResource(R.string.slime_item))
            UTextField(item, { item = it.take(30) }, placeholder = stringResource(R.string.slime_item_hint))
            Text(stringResource(R.string.slime_item_note), style = UType.Small.copy(color = U.c.muted), modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(Modifier.height(18.dp))
        PrimaryButton(button, {
            val to = if (noPage) fromPage else page
            onSave(SittingInput(to, item, finish || (known && to >= book.pages)))
        }, Modifier.fillMaxWidth())
    }
}
