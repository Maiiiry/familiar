package app.familiar.domain

/** Where a piece stands in the den; one piece per place. */
enum class Place { WINDOW, WALL, TOP, FLOOR_LEFT, FLOOR_RIGHT, RUG, LIGHT }

/** Den furniture, bought with sparks. */
enum class Item(val place: Place, val price: Int) {
    WINDOW_MOON(Place.WINDOW, 300),
    WINDOW_STAINED(Place.WINDOW, 650),
    BOOKSHELF(Place.WALL, 450),
    PORTRAIT(Place.WALL, 350),
    BANNER(Place.TOP, 250),
    GARLAND(Place.TOP, 300),
    ARMCHAIR(Place.FLOOR_LEFT, 450),
    CAULDRON(Place.FLOOR_LEFT, 550),
    TELESCOPE(Place.FLOOR_LEFT, 750),
    MANDRAKE(Place.FLOOR_RIGHT, 250),
    TREASURE(Place.FLOOR_RIGHT, 600),
    GLOBE(Place.FLOOR_RIGHT, 500),
    RUG_RED(Place.RUG, 200),
    RUG_STARS(Place.RUG, 400),
    CANDLES(Place.LIGHT, 150),
    LANTERN(Place.LIGHT, 350);

    companion object {
        fun of(name: String): Item? = entries.firstOrNull { it.name == name }
    }
}

object Den {
    /** Sparks: every mana a familiar ever ate leaves one; furniture costs them, growth doesn't. */
    fun sparks(pets: List<app.familiar.data.Pet>, spent: Int): Int = (pets.sumOf { it.mana } - spent).coerceAtLeast(0)

    /** Placing [item] takes its place from whatever stood there. */
    fun place(placed: Set<String>, item: Item): Set<String> =
        placed.filter { Item.of(it)?.place != item.place }.toSet() + item.name
}
