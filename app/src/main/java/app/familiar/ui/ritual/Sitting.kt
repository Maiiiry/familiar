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
import app.familiar.ui.pages
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType

class SittingInput(val toPage: Int, val item: String, val finish: Boolean)

/** Where you stopped and (for a slime) what it should swallow. */
@Composable
fun SittingForm(book: Book, fromPage: Int, startPage: Int, slime: Boolean, button: String, onSave: (SittingInput) -> Unit) {
    var page by remember { mutableIntStateOf(startPage.coerceIn(fromPage, book.pages)) }
    var typed by remember { mutableStateOf("") }
    var item by remember { mutableStateOf("") }
    var finish by remember { mutableStateOf(false) }
    Column {
        SectionTitle(stringResource(R.string.stopped_at))
        UCard {
            Stepper(stringResource(R.string.page_n, page), "+${page - fromPage}", { page = (page - 1).coerceAtLeast(fromPage) }, { page = (page + 1).coerceAtMost(book.pages) }, color = U.c.accent)
            UTextField(typed, {
                typed = it.filter(Char::isDigit).take(5)
                typed.toIntOrNull()?.let { v -> page = v.coerceIn(fromPage, book.pages) }
            }, number = true, placeholder = stringResource(R.string.page_type, book.pages))
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.read_now, pages(page - fromPage)), style = UType.Small.copy(color = U.c.muted))
            SwitchRow(stringResource(R.string.finished_book), null, finish || page >= book.pages, { finish = it; if (it) page = book.pages })
        }
        if (slime) {
            SectionTitle(stringResource(R.string.slime_item))
            UTextField(item, { item = it.take(30) }, placeholder = stringResource(R.string.slime_item_hint))
            Text(stringResource(R.string.slime_item_note), style = UType.Small.copy(color = U.c.muted), modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(Modifier.height(18.dp))
        PrimaryButton(button, { onSave(SittingInput(page, item, finish || page >= book.pages)) }, Modifier.fillMaxWidth())
    }
}
