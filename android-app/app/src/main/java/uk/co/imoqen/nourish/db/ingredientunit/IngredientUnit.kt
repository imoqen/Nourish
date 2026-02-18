package uk.co.imoqen.nourish.db.ingredientunit

// also need to tell llm to use celsius / farenheit, not included in units.
// if hard to search for these from llm input, could potentially add ids to them and feed this into llm
// so llm can return more easily then we just search for the id.
// include: metric, uk imperial, us imperial.
enum class IngredientUnit(
    name: String,
    pluralName: String // where an s should be appended if the value is plural, or word changes e.g. loaf
) {
    // metric:
    ML("ml", "ml"),
    L("l", "l"),
    G("g", "g"),
    KG("kg", "kg"),

    // imperial:
    TSP("tsp", "tsp"),
    TBSP("Tbsp", "Tbsp"), // convention to put capital T on tbsp to differentiate from tsp
    FL_OZ("fl oz", "fl oz"),
    CUP("cup", "cups"),
    PINT("pint", "pints"),
    QUART("quart", "quarts"),
    GALLON("gallon", "gallons"),
    OZ("oz", "oz"),
    LB("lb", "lbs"),

    // other:
    CLOVE("clove of", "cloves of"),
    HEAD("head of", "heads of"),
    BUNCH("bunch of", "bunches of"),
    SPRIG("sprig of", "sprigs of"),
    BULB("bulb of", "bulbs of"),
    STALK("stalk of", "stalks of"),
    STICK("stick of", "sticks of"),
    PIECE("piece of", "pieces of"),
    LEAF("leaf of", "leaves of"),

    // small amounts of liquids
    DASH("dash of", "dashes of"),
    DRIZZLE("drizzle of", "drizzles of"),
    PINCH("pinch of", "pinches of"),
    SPLASH("splash of", "splashes of"),
    DROP("drop of", "drops of"),
    HANDFUL("handful of", "handfuls of"),

    // baking
    SLICE("slice of", "slices of"),
    LOAF("loaf of", "loaves of"),
    ROLL("roll of", "rolls of"),
    SHEET("sheet of", "sheets of"),
    BLOCK("block of", "blocks of"),
    BAR("bar of", "bars of"),
}