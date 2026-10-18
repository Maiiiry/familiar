package app.familiar.ui.den

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.ui.platform.LocalContext
import app.familiar.container
import app.familiar.domain.Den
import app.familiar.domain.Item
import app.familiar.ui.components.DenScene
import app.familiar.ui.components.rememberNow
import app.familiar.ui.pix.Room
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.familiar.R
import app.familiar.content.Lines
import app.familiar.domain.Genre
import app.familiar.domain.Species
import app.familiar.domain.Stage
import app.familiar.ui.FamiliarData
import app.familiar.ui.Go
import app.familiar.ui.Tab
import app.familiar.ui.components.Bar
import app.familiar.ui.components.BookCover
import app.familiar.ui.components.FamiliarArt
import app.familiar.ui.components.PrimaryButton
import app.familiar.ui.components.SoftButton
import app.familiar.ui.components.UCard
import app.familiar.ui.days
import app.familiar.ui.locale
import app.familiar.ui.speciesName
import app.familiar.ui.stageName
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType
import java.time.LocalTime

@Composable
fun DenTab(data: FamiliarData, go: Go, onTab: (Tab) -> Unit) {
    val s = data.stats
    val pet = data.pet
    val sp = data.species
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 24.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                val h = LocalTime.now().hour
                val hello = stringResource(
                    when (h) { in 5..11 -> R.string.hello_morning; in 12..17 -> R.string.hello_day; in 18..22 -> R.string.hello_evening; else -> R.string.hello_night }
                )
                Text((if (data.settings.name.isBlank()) hello else "$hello, ${data.settings.name}").uppercase(), style = UType.Eyebrow)
                Text(pet?.name ?: stringResource(R.string.app_name), style = UType.Display)
            }
            IconButton(onClick = { go.settings() }) { Icon(Icons.Rounded.Settings, stringResource(R.string.settings), tint = U.c.muted) }
        }
        Spacer(Modifier.height(10.dp))
        val container = LocalContext.current.container
        val now = rememberNow(1000)
        // Moments that pass: purring after a stroke, a sleepy eye after a tap at night.
        var purrUntil by remember { mutableLongStateOf(0L) }
        var wokenUntil by remember { mutableLongStateOf(0L) }
        val hour = LocalTime.now().hour
        val night = hour < 6 || hour >= 20
        val asleep = hour < 6 && now > wokenUntil && data.settings.ritual == null
        val purring = now < purrUntil
        val look = data.look(sleeping = asleep)?.let { if (purring && !asleep) it.copy(happy = true) else it }
        val placed = data.settings.placed.mapNotNull { Item.of(it) }.toSet()
        val books = s.finished.map { Genre.of(it.genre).argb.toInt() }.takeLast(24)
        Box(Modifier.fillMaxWidth().aspectRatio(Room.W / Room.H.toFloat())) {
            if (look != null) DenScene(
                look, placed, books, night, Modifier.fillMaxSize(),
                onTap = { if (asleep || now < wokenUntil) wokenUntil = System.currentTimeMillis() + 8_000 },
                onStroke = { purrUntil = System.currentTimeMillis() + 2_500 },
            )
            if (pet != null && sp != null) Row(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Tag("${if (pet.shiny) "✦ " else ""}${speciesName(sp)} · ${stageName(s.stage)}")
            }
        }
        if (pet != null && sp != null) {
            val seed = remember(data.now / 600_000) { (data.now / 600_000).toInt() }
            val l = locale()
            val line = when {
                purring -> Lines.line(l, sp, Lines.State.PURR, pet.name, seed)
                asleep -> Lines.line(l, sp, Lines.State.SLEEP, pet.name, seed)
                now < wokenUntil && hour < 6 -> Lines.line(l, sp, Lines.State.WOKEN, pet.name, seed)
                else -> speech(data, sp, pet.name, seed)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                line,
                style = UType.Body.copy(fontSize = 14.sp, textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth().clip(U.RMd).background(U.c.surface).border(1.dp, U.c.line, U.RMd).padding(horizontal = 14.dp, vertical = 10.dp),
            )
            Spacer(Modifier.height(10.dp))
            SoftButton(
                stringResource(R.string.furnish_short, Den.sparks(data.pets, data.settings.spent)), { go.furnish() },
                Modifier.fillMaxWidth(), color = U.c.gold, soft = U.c.goldSoft, icon = Icons.Rounded.Chair,
            )
        }
        if (pet != null) {
            Spacer(Modifier.height(12.dp))
            UCard {
                val next = s.stage.next
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = U.c.mana, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.mana_title), style = UType.Strong, modifier = Modifier.weight(1f))
                    Text(if (next == null) stringResource(R.string.mana_n, pet.mana) else stringResource(R.string.mana_of, pet.mana, next.from), style = UType.Muted)
                }
                Spacer(Modifier.height(8.dp))
                Bar(Stage.progress(pet.mana), color = U.c.mana, track = U.c.manaSoft)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (next == null) stringResource(R.string.mana_max) else if (s.stage == Stage.EGG) stringResource(R.string.mana_to_hatch, next.from - pet.mana) else stringResource(R.string.mana_to_next, stageName(next), next.from - pet.mana),
                    style = UType.Small.copy(color = U.c.muted),
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.fed_title), style = UType.Strong, modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.fed_of, s.todayPages, data.settings.dailyGoal), style = UType.Muted)
                }
                Spacer(Modifier.height(8.dp))
                Bar(s.fed, color = U.c.ember, track = U.c.emberSoft)
                if (s.streak > 0) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalFireDepartment, null, tint = U.c.ember, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.streak_line, days(s.streak)), style = UType.Small.copy(color = U.c.muted))
                    }
                }
            }
            if (s.stone > 0f) {
                Spacer(Modifier.height(12.dp))
                UCard {
                    Text(stringResource(if (sp == Species.PHOENIX) R.string.ash_title else R.string.stone_title), style = UType.Strong.copy(color = U.c.accent))
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(if (sp == Species.PHOENIX) R.string.ash_text else R.string.stone_text, pet.name), style = UType.Muted)
                }
            }
        }
        val book = data.reading.firstOrNull()
        Spacer(Modifier.height(12.dp))
        if (book != null) {
            UCard(onClick = { go.book(book.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BookCover(book.title, Genre.of(book.genre), Modifier.width(54.dp).height(78.dp), small = true, cover = book.cover)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.now_reading).uppercase(), style = UType.Eyebrow)
                        Text(book.title, style = UType.Strong, maxLines = 2)
                        if (book.author.isNotBlank()) Text(book.author, style = UType.Muted, maxLines = 1)
                        Spacer(Modifier.height(6.dp))
                        if (book.pages > 0) Bar(book.fraction, color = U.c.accent, track = U.c.accentSoft)
                        Spacer(Modifier.height(4.dp))
                        Text(app.familiar.ui.progressLabel(book), style = UType.Small)
                    }
                }
                Spacer(Modifier.height(14.dp))
                PrimaryButton(stringResource(R.string.ritual_start), { go.ritual(book.id) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.LocalFireDepartment)
                Spacer(Modifier.height(8.dp))
                SoftButton(stringResource(R.string.log_pages), { go.log(book.id) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Edit)
            }
        } else {
            UCard {
                Text(stringResource(R.string.no_book_title), style = UType.Strong)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.no_book_text), style = UType.Muted)
                Spacer(Modifier.height(12.dp))
                PrimaryButton(stringResource(R.string.book_add), { go.editBook() }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Add)
                if (data.books.any { it.shelfOf == app.familiar.data.Shelf.WANT }) {
                    Spacer(Modifier.height(8.dp))
                    SoftButton(stringResource(R.string.from_shelf), { onTab(Tab.SHELF) }, Modifier.fillMaxWidth())
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        UCard(onClick = { go.bestiary() }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.bestiary), style = UType.Strong)
                    val eggs = s.unlocked.count { u -> data.pets.none { it.species == u.name } }
                    Text(
                        if (eggs > 0) stringResource(R.string.bestiary_eggs, eggs) else stringResource(R.string.bestiary_count, data.pets.size, Species.entries.size),
                        style = UType.Muted.copy(color = if (eggs > 0) U.c.gold else U.c.muted),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
                    data.pets.take(4).forEach { p ->
                        FamiliarArt(app.familiar.ui.lookOf(Species.of(p.species), p.mana, s, p.active, shiny = p.shiny), Modifier.size(46.dp), fps = 3f)
                    }
                }
            }
        }
    }
}

@Composable
private fun Tag(text: String) {
    Text(text, style = UType.Small.copy(color = U.c.ink), modifier = Modifier.clip(U.RSm).background(U.c.surface.copy(alpha = 0.85f)).padding(horizontal = 10.dp, vertical = 5.dp))
}

/** What the familiar is doing, in a line. */
@Composable
private fun speech(data: FamiliarData, sp: Species, name: String, seed: Int): String {
    val l = locale()
    val s = data.stats
    val state = when {
        s.stage == Stage.EGG -> Lines.State.EGG
        s.stone > 0.3f && sp == Species.PHOENIX -> Lines.State.ASH
        s.stone > 0.3f -> Lines.State.STONE
        s.fed >= 1f -> Lines.State.FED
        else -> Lines.State.HUNGRY
    }
    if (state == Lines.State.FED || state == Lines.State.HUNGRY) {
        // Now and then it reacts to the last book.
        if (seed % 2 == 0) Lines.mood(l, s.mood, name)?.let { return it }
    }
    return Lines.line(l, sp, state, name, seed)
}
