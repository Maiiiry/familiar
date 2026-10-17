package app.familiar.ui.tree

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.familiar.R
import app.familiar.domain.Year
import app.familiar.ui.FamiliarData
import app.familiar.ui.Go
import app.familiar.ui.components.Den
import app.familiar.ui.components.FamiliarArt
import app.familiar.ui.components.PrimaryButton
import app.familiar.ui.components.Stat
import app.familiar.ui.components.TopBar
import app.familiar.ui.components.UCard
import app.familiar.ui.genreName
import app.familiar.ui.speciesName
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType

/** The year of reading told as a legend, ready to share. */
@Composable
fun SagaScreen(data: FamiliarData, go: Go) {
    val context = LocalContext.current
    val s = data.stats
    val y = Year(s, s.today.year)
    val pet = data.pet
    val sp = data.species
    val reader = data.settings.name.ifBlank { stringResource(R.string.saga_reader) }
    val who = if (pet != null && sp != null) stringResource(R.string.saga_who, reader, speciesName(sp).lowercase(), pet.name) else reader
    val parts = buildList {
        add(
            if (y.books.isEmpty()) stringResource(R.string.saga_open, y.year, who, pluralStringResource(R.plurals.pages, y.pages, y.pages))
            else stringResource(R.string.saga_open_books, y.year, who, pluralStringResource(R.plurals.books, y.books.size, y.books.size), pluralStringResource(R.plurals.pages, y.pages, y.pages))
        )
        y.genre?.let { add(stringResource(R.string.saga_genre, genreName(it).lowercase())) }
        y.longest?.let { add(stringResource(R.string.saga_longest, it.title, pluralStringResource(R.plurals.pages, it.pages, it.pages))) }
        if (y.nights > 0) add(pluralStringResource(R.plurals.saga_nights, y.nights, y.nights))
        if (y.bestStreak > 1) add(pluralStringResource(R.plurals.saga_streak, y.bestStreak, y.bestStreak))
        if (y.quotes > 0) add(pluralStringResource(R.plurals.saga_quotes, y.quotes, y.quotes))
        add(stringResource(R.string.saga_end))
    }
    val text = parts.joinToString(" ")
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        TopBar(stringResource(R.string.saga_title, y.year), { go.back() })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Den(Modifier.fillMaxWidth().height(220.dp)) {
                data.look()?.let { FamiliarArt(it, Modifier.fillMaxSize().padding(16.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.saga_heading, y.year), style = UType.Title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(text, style = UType.Quote)
            Spacer(Modifier.height(18.dp))
            UCard {
                Row {
                    Stat(y.books.size.toString(), stringResource(R.string.stat_books), Modifier.weight(1f))
                    Stat(y.pages.toString(), stringResource(R.string.stat_pages), Modifier.weight(1f))
                    Stat(y.nights.toString(), stringResource(R.string.stat_nights), Modifier.weight(1f))
                    Stat(y.quotes.toString(), stringResource(R.string.stat_quotes), Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        PrimaryButton(
            stringResource(R.string.share), {
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null))
            },
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp).navigationBarsPadding(), icon = Icons.Rounded.Share,
        )
    }
}
