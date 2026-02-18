package uk.co.imoqen.nourish.db.ingredientunit

// also need to tell llm to use celsius / farenheit, not included in units.
// if hard to search for these from llm input, could potentially add ids to them and feed this into llm
// so llm can return more easily then we just search for the id.
// include: metric, uk imperial, us imperial.
enum class IngredientUnit(
    name: String,
    pluralName: String // whether an s should be appended if the value is plural
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
    CLOVE("clove", "cloves of"),
    HEAD("head", "heads of"),
    BUNCH("bunch", "bunches of"),
    SPRIG("sprig", "sprigs of"),
    BULB("bulb", "bulbs of"),
    STALK("stalk", "stalks of"),
    STICK("stick", "sticks of"),
    PIECE("piece", "pieces of"),

    // small amounts of liquids
    DASH("dash", "dashes of"),
    DRIZZLE("drizzle", "drizzles of"),
    PINCH("pinch", "pinches of"),
    SPLASH("splash", "splashes of"),
    DROP("drop", "drops of"),
    HANDFUL("handful", "handful of"),

    // baking
    SLICE("slice", "slices of"),
    LOAF("loaf", "loaves of"),
    ROLL("roll", "rolls of"),
    SHEET("sheet", "sheets of"),
    BLOCK("block", "blocks of"),
    BAR("bar", "bars of"),
}