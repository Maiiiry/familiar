package app.familiar.ui.books

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.familiar.R
import app.familiar.data.Covers
import app.familiar.domain.Genre
import app.familiar.ui.components.BookCover
import app.familiar.ui.components.SoftButton
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType
import kotlinx.coroutines.launch

/** The cover as it will look, with a picture from the gallery or back to a drawn one. */
@Composable
fun CoverPicker(title: String, genre: Genre, cover: String, onCover: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch { Covers.importPicked(context, uri)?.let(onCover) }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        BookCover(title.ifBlank { "?" }, genre, Modifier.width(70.dp).height(102.dp), small = true, cover = cover)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(if (cover.isNotBlank()) R.string.cover_found else R.string.cover_drawn), style = UType.Muted)
            SoftButton(stringResource(R.string.cover_pick), {
                pick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Image)
            if (cover.isNotBlank()) SoftButton(stringResource(R.string.cover_remove), { onCover("") }, Modifier.fillMaxWidth(), color = U.c.muted, soft = U.c.surface2)
        }
    }
}
