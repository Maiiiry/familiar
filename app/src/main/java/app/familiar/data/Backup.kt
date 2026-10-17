package app.familiar.data

import org.json.JSONArray
import org.json.JSONObject

/** Plain JSON backup: everything stays on the device unless the user exports it. */
object Backup {
    private const val VERSION = 1

    fun encode(s: Snapshot): String = JSONObject()
        .put("app", "familiar")
        .put("version", VERSION)
        .put("books", JSONArray(s.books.map {
            JSONObject().put("id", it.id).put("title", it.title).put("author", it.author).put("pages", it.pages).put("genre", it.genre)
                .put("shelf", it.shelf).put("page", it.page).put("added", it.added).put("started", it.started ?: JSONObject.NULL)
                .put("finished", it.finished ?: JSONObject.NULL).put("rating", it.rating ?: JSONObject.NULL).put("cover", it.cover).put("audio", it.audio)
        }))
        .put("readings", JSONArray(s.readings.map {
            JSONObject().put("id", it.id).put("bookId", it.bookId).put("petId", it.petId).put("start", it.start).put("end", it.end)
                .put("fromPage", it.fromPage).put("toPage", it.toPage).put("mana", it.mana).put("timed", it.timed)
                .put("stillSec", it.stillSec).put("item", it.item).put("finishedBook", it.finishedBook)
        }))
        .put("pets", JSONArray(s.pets.map {
            JSONObject().put("id", it.id).put("species", it.species).put("name", it.name).put("joined", it.joined).put("mana", it.mana).put("active", it.active).put("shiny", it.shiny)
        }))
        .toString(2)

    /** Throws [IllegalArgumentException] when the text is not a Familiar backup. */
    fun decode(text: String): Snapshot {
        val root = runCatching { JSONObject(text) }.getOrNull()
        require(root != null && root.optString("app") == "familiar") { "Not a Familiar backup" }
        return Snapshot(
            books = root.list("books") {
                Book(
                    id = getLong("id"), title = getString("title"), author = optString("author"), pages = getInt("pages"), genre = getString("genre"),
                    shelf = optString("shelf", Shelf.READING.name), page = optInt("page"), added = getLong("added"),
                    started = optL("started"), finished = optL("finished"), rating = optI("rating"), cover = optString("cover"), audio = optBoolean("audio"),
                )
            },
            readings = root.list("readings") {
                Reading(
                    id = getLong("id"), bookId = getLong("bookId"), petId = getLong("petId"), start = getLong("start"), end = getLong("end"),
                    fromPage = getInt("fromPage"), toPage = getInt("toPage"), mana = getInt("mana"), timed = optBoolean("timed"),
                    stillSec = optInt("stillSec"), item = optString("item"), finishedBook = optBoolean("finishedBook"),
                )
            },
            pets = root.list("pets") { Pet(getLong("id"), getString("species"), getString("name"), getLong("joined"), getInt("mana"), optBoolean("active"), optBoolean("shiny")) },
        )
    }

    private fun JSONObject.optL(key: String): Long? = if (!has(key) || isNull(key)) null else getLong(key)
    private fun JSONObject.optI(key: String): Int? = if (!has(key) || isNull(key)) null else getInt(key)

    private fun <T> JSONObject.list(key: String, read: JSONObject.() -> T): List<T> {
        val arr = optJSONArray(key) ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).read() }
    }
}
