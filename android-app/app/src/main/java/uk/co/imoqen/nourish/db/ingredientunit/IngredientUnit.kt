package uk.co.imoqen.nourish.db.ingredientunit

// if hard to search for these from llm input, could potentially add ids to them and feed this into llm
// so llm can return more easily then we just search for the id.
// include: metric, uk imperial, us imperial.
enum class IngredientUnit(
    val singularName: String,
    val pluralName: String, // where an s should be appended if the value is plural, or word changes e.g. loaf
    val useSingularForOne: Boolean, // used for something existing as a piece, not uncountable, e.g. a piece: "piece of baguette" not "piece of baguettes", but "tsp of baguettes" would never occur so is false
    val keepUnitInShoppingList: Boolean // if the unit is quite small like tsp, no point in putting it in the shopping list - no one is going to buy 1tsp of something etc. so consider these just as "1 UNIT" (has no unit).
) {
    ML("ml", "ml", false, true), // L is not supported, it is just 1000ml and will be handled accordingly.
    G("g", "g", false, true), // same for kg as above.
    TSP("tsp", "tsp", false, false),
    TBSP("Tbsp", "Tbsp", false, false), // convention to put capital T on tbsp to differentiate from tsp

    // extra for user input IN SHOPPING LIST, not supported in the LLM
    // l and kg are purely for output or user input. not send by the llm
    // and not stored in the db as this as it makes ingredient scaling harder.
    L("L", "L", false, true),
    KG("kg", "kg", false, true),
    PINT("pint", "pints", false, true),

    // metric, not currently supported as the app's primary unit is ml, g. but support could be added this in the future.
    /*FL_OZ("fl oz", "fl oz", false, true),
    CUP("cup", "cups", false),
    PINT("pint", "pints", false),
    QUART("quart", "quarts", false),
    GALLON("gallon", "gallons", false),
    OZ("oz", "oz", false),
    LB("lb", "lbs", false),*/

    // other:
    CLOVE("clove of", "cloves of", true, false),
    HEAD("head of", "heads of", true, false),
    BUNCH("bunch of", "bunches of", true, false),
    SPRIG("sprig of", "sprigs of", true, false),
    BULB("bulb of", "bulbs of", true, false),
    STALK("stalk of", "stalks of", true, false),
    STICK("stick of", "sticks of", true, false),
    PIECE("piece of", "pieces of", true, false),
    LEAF("leaf of", "leaves of", true, false),
    CM("cm of", "cms of", true, false),

    // small amounts of liquids / herbs etc
    DASH("dash of", "dashes of", false, false),
    DRIZZLE("drizzle of", "drizzles of", false, false),
    PINCH("pinch of", "pinches of", false, false),
    SPLASH("splash of", "splashes of", false, false),
    DROP("drop of", "drops of", false, false),
    HANDFUL("handful of", "handfuls of", false, false),
    SPRINKLE("sprinkle of", "sprinkles of", false, false),

    // baking
    SLICE("slice of", "slices of", true, false),
    LOAF("loaf of", "loaves of", true, false),
    ROLL("roll of", "rolls of", true, false),
    SHEET("sheet of", "sheets of", true, false),
    BLOCK("block of", "blocks of", true, false),
    BAR("bar of", "bars of", true, false),

    // for item without a prefix e.g. carrot
    UNIT("", "", true, true),

    // for use in the shopping list when a small amount of something must be bought, its just there
    // to say its there but it has no strict unit, its just a single one of that item.
    SINGLE("", "", true, false) // difference to UNIT is that it is not something that is bought frequently e.g. oil, salt, pepper, etc. you buy 1 then use it for months
    // so the add to shopping list will never increment it. but, users will have the option to themselves if they wish.
}