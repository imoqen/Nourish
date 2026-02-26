package uk.co.imoqen.nourish.db.ingredientunit

// if hard to search for these from llm input, could potentially add ids to them and feed this into llm
// so llm can return more easily then we just search for the id.
// include: metric, uk imperial, us imperial.
enum class IngredientUnit(
    val singularName: String,
    val pluralName: String, // where an s should be appended if the value is plural, or word changes e.g. loaf
    val useSingularForOne: Boolean // used for something existing as a piece, not uncountable, e.g. a piece: "piece of baguette" not "piece of baguettes", but "tsp of baguettes" would never occur so is false
) {
    // metric:
    ML("ml", "ml", false),
    L("l", "l", false),
    G("g", "g", false),
    KG("kg", "kg", false),

    // imperial:
    TSP("tsp", "tsp", false),
    TBSP("Tbsp", "Tbsp", false), // convention to put capital T on tbsp to differentiate from tsp
    FL_OZ("fl oz", "fl oz", false),
    CUP("cup", "cups", false),
    PINT("pint", "pints", false),
    QUART("quart", "quarts", false),
    GALLON("gallon", "gallons", false),
    OZ("oz", "oz", false),
    LB("lb", "lbs", false),

    // other:
    CLOVE("clove of", "cloves of", true),
    HEAD("head of", "heads of", true),
    BUNCH("bunch of", "bunches of", true),
    SPRIG("sprig of", "sprigs of", true),
    BULB("bulb of", "bulbs of", true),
    STALK("stalk of", "stalks of", true),
    STICK("stick of", "sticks of", true),
    PIECE("piece of", "pieces of", true),
    LEAF("leaf of", "leaves of", true),

    // small amounts of liquids / herbs etc
    DASH("dash of", "dashes of", false),
    DRIZZLE("drizzle of", "drizzles of", false),
    PINCH("pinch of", "pinches of", false),
    SPLASH("splash of", "splashes of", false),
    DROP("drop of", "drops of", false),
    HANDFUL("handful of", "handfuls of", false),
    SPRINKLE("sprinkle of", "sprinkles of", false),

    // baking
    SLICE("slice of", "slices of", true),
    LOAF("loaf of", "loaves of", true),
    ROLL("roll of", "rolls of", true),
    SHEET("sheet of", "sheets of", true),
    BLOCK("block of", "blocks of", true),
    BAR("bar of", "bars of", true),

    // for item without a prefix e.g. carrot
    UNIT("", "", true)
}