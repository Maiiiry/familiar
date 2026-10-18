package app.familiar.data

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

/**
 * Finds books by title in open catalogues and keeps their covers on the phone. This is the only
 * thing in the app that uses the internet: Open Library first, Google Books when it finds nothing.
 */
object Covers {
    class Found(val title: String, val author: String, val pages: Int?, val year: Int?, val thumb: String, val cover: String)

    private fun get(url: String): ByteArray? = runCatching {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 8_000
        c.readTimeout = 12_000
        c.instanceFollowRedirects = true
        c.setRequestProperty("User-Agent", "Familiar/1.0 (Android reading app)")
        try {
            if (c.responseCode !in 200..299) null else c.inputStream.use { it.readBytes() }
        } finally {
            c.disconnect()
        }
    }.getOrNull()

    private fun enc(s: String) = URLEncoder.encode(s.trim(), "UTF-8")

    suspend fun search(query: String): List<Found>? = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val open = openLibrary(query)
        if (!open.isNullOrEmpty() && open.any { it.cover.isNotEmpty() }) return@withContext open
        val google = google(query)
        when {
            !google.isNullOrEmpty() -> (open.orEmpty() + google).distinctBy { it.title.lowercase() to it.author.lowercase() }
            else -> open
        }
    }

    private fun openLibrary(q: String): List<Found>? {
        val body = get("https://openlibrary.org/search.json?q=${enc(q)}&limit=12&fields=title,author_name,number_of_pages_median,cover_i,first_publish_year")
            ?: return null
        val docs = runCatching { JSONObject(body.decodeToString()).getJSONArray("docs") }.getOrNull() ?: return null
        return (0 until docs.length()).map { docs.getJSONObject(it) }.map { d ->
            val id = d.optLong("cover_i", 0)
            Found(
                title = d.optString("title"),
                author = d.optJSONArray("author_name")?.optString(0).orEmpty(),
                pages = d.optInt("number_of_pages_median", 0).takeIf { it > 0 },
                year = d.optInt("first_publish_year", 0).takeIf { it > 0 },
                thumb = if (id > 0) "https://covers.openlibrary.org/b/id/$id-M.jpg" else "",
                cover = if (id > 0) "https://covers.openlibrary.org/b/id/$id-L.jpg" else "",
            )
        }.filter { it.title.isNotBlank() }
    }

    private fun google(q: String): List<Found>? {
        val body = get("https://www.googleapis.com/books/v1/volumes?q=${enc(q)}&maxResults=12&printType=books") ?: return null
        val items = runCatching { JSONObject(body.decodeToString()).optJSONArray("items") }.getOrNull() ?: return emptyList()
        return (0 until items.length()).map { items.getJSONObject(it).getJSONObject("volumeInfo") }.map { v ->
            val img = v.optJSONObject("imageLinks")?.optString("thumbnail").orEmpty().replace("http://", "https://")
            Found(
                title = v.optString("title"),
                author = v.optJSONArray("authors")?.optString(0).orEmpty(),
                pages = v.optInt("pageCount", 0).takeIf { it > 0 },
                year = v.optString("publishedDate").take(4).toIntOrNull(),
                thumb = img,
                cover = img,
            )
        }.filter { it.title.isNotBlank() }
    }

    private fun file(context: Context, url: String): File {
        val h = MessageDigest.getInstance("SHA-1").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }
        return File(File(context.filesDir, "covers").apply { mkdirs() }, "$h.jpg")
    }

    /** Covers picked from the gallery are stored as files and referred to like this. */
    const val LOCAL = "file://"

    /** Copies a picture the user picked into the app, scaled down to a cover; returns its address. */
    suspend fun importPicked(context: Context, uri: android.net.Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 900) sample *= 2
            val bmp = context.contentResolver.openInputStream(uri)!!.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@runCatching null
            val dir = File(context.filesDir, "covers").apply { mkdirs() }
            val out = File(dir, "own-${System.currentTimeMillis()}.jpg")
            out.outputStream().use { bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, it) }
            LOCAL + out.path
        }.getOrNull()
    }

    private val memory = object : LinkedHashMap<String, ImageBitmap>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?) = size > 40
    }

    fun cached(url: String): ImageBitmap? = synchronized(memory) { memory[url] }

    /** The cover from the phone, downloading it once if needed; null if there's none. */
    suspend fun load(context: Context, url: String): ImageBitmap? = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext null
        cached(url)?.let { return@withContext it }
        val f = if (url.startsWith(LOCAL)) File(url.removePrefix(LOCAL)) else file(context, url)
        if (url.startsWith(LOCAL) && !f.exists()) return@withContext null
        if (!f.exists()) {
            val bytes = get(url) ?: return@withContext null
            // Open Library answers with a 1-pixel GIF when it has no picture.
            if (bytes.size < 1000) return@withContext null
            f.writeBytes(bytes)
        }
        val bmp = BitmapFactory.decodeFile(f.path)?.asImageBitmap() ?: return@withContext null
        synchronized(memory) { memory[url] = bmp }
        bmp
    }
}
