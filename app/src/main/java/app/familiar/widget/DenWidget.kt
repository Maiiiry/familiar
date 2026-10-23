package app.familiar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import app.familiar.MainActivity
import app.familiar.R
import app.familiar.container
import app.familiar.domain.Genre
import app.familiar.domain.Species
import app.familiar.domain.Stage
import app.familiar.domain.Stats
import app.familiar.localized
import app.familiar.ui.pix.Look
import app.familiar.ui.pix.Sprites
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.ZoneId

/** The familiar on the home screen: how it's doing and today's pages. A tap opens the den. */
class DenWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        scope.launch {
            try {
                refresh(context)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        suspend fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, DenWidget::class.java))
            if (ids.isEmpty()) return
            val c = context.container
            val s = c.settings.now()
            val dao = c.db.dao()
            val st = Stats(dao.books(), dao.readings(), dao.pets(), System.currentTimeMillis(), ZoneId.systemDefault(), s.dailyGoal, s.guardedDays)
            val l = context.localized()
            val views = RemoteViews(context.packageName, R.layout.widget_den)
            val pet = st.active
            if (pet != null) {
                val sp = Species.of(pet.species)
                val look = Look(
                    sp, Stage.of(pet.mana), stone = st.stone, mood = st.mood,
                    tails = Genre.tails.filter { it in st.genresFinished }.map { it.argb.toInt() },
                    hoard = st.finished.takeLast(16).map { Genre.of(it.genre).argb.toInt() },
                    patches = st.ownFinished.map { it.argb.toInt() },
                    stars = st.nights, items = st.items.take(7),
                    hatch = (pet.mana / Stage.BABY.from.toFloat()).coerceIn(0f, 1f),
                )
                val px = Sprites.frame(look, 0)
                val raw = Bitmap.createBitmap(px.c, px.w, px.h, Bitmap.Config.ARGB_8888)
                // Scaled up without smoothing so the pixels stay crisp.
                views.setImageViewBitmap(R.id.art, Bitmap.createScaledBitmap(raw, px.w * 3, px.h * 3, false))
                views.setTextViewText(R.id.name, pet.name)
                views.setTextViewText(
                    R.id.state,
                    l.getString(
                        when {
                            st.stage == Stage.EGG -> R.string.w_egg
                            st.stone > 0.3f -> if (sp == Species.PHOENIX) R.string.w_ash else R.string.w_stone
                            st.fed >= 1f -> R.string.w_fed
                            st.todayPages > 0 -> R.string.w_eating
                            else -> R.string.w_hungry
                        }
                    ),
                )
            }
            views.setTextViewText(R.id.pages, l.getString(R.string.fed_of, st.todayPages, s.dailyGoal))
            views.setProgressBar(R.id.bar, 1000, (st.fed * 1000).toInt(), false)
            views.setOnClickPendingIntent(
                R.id.root,
                PendingIntent.getActivity(
                    context, 5000,
                    Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            manager.updateAppWidget(ids, views)
        }
    }
}
