package uk.co.imoqen.nourish.db.ingredientunit

/**
 * The enum holding all IngredientUnits which are sent to the LLM and assigned to an ingredient in a
 * recipe. Users can also assign IngredientUnits themselves when creating / editing a recipe.
 *
 * Since the app is aimed at a UK audience, UK-popular units have been prioritised and used rather
 * than cups, quarts, gallons, ounces and lbs. These were initially planned but not implemented,
 * so could be in the future thus left as a comment.
 *
 * @param singularName - The singular name for the ingredient, e.g. strawberry
 * @param pluralName - The plural name for the ingredient, e.g. strawberries
 * @param useSingularForOne - Used for something existing as a piece, not uncountable, e.g. a piece:
 *                            e.g. baguette is false: "piece of baguette" not "piece of baguettes",
 *                            but "tsp of baguettes" would never occur so is false
 * @param keepUnitInShoppingList - If the unit is quite small like tsp, no point in putting it in the
 *                                 shopping list - no one is going to buy just 1 tsp of something etc.
 *                                 So consider these just as "1 UNIT" (has no unit).
 */
enum class IngredientUnit(
    val singularName: String,
    val pluralName: String,
    val useSingularForOne: Boolean,
    val keepUnitInShoppingList: Boolean
) {
    // Primary units:
    ML("ml", "ml", false, true),
    G("g", "g", false, true),
    TSP("tsp", "tsp", false, false),
    // convention to put capital T on tbsp to differentiate from tsp:
    TBSP("Tbsp", "Tbsp", false, false),

    // L and kg are purely for output or user input, not send by the llm.
    // e.g. if something is 1000 ml, it will be displayed as L in the ui, but not updated in the db.
    L("L", "L", false, true),
    KG("kg", "kg", false, true),
    PINT("pint", "pints", false, true),

    // Metric, not currently supported as the app's primary unit is ml, g, etc.
    // But support could be added this in the future, so left here.
    /*FL_OZ("fl oz", "fl oz", false, true),
    CUP("cup", "cups", false),
    PINT("pint", "pints", false),
    QUART("quart", "quarts", false),
    GALLON("gallon", "gallons", false),
    OZ("oz", "oz", false),
    LB("lb", "lbs", false),*/

    // Vegetable-related:
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

    // Small amounts of liquids / herbs etc:
    DASH("dash of", "dashes of", false, false),
    DRIZZLE("drizzle of", "drizzles of", false, false),
    PINCH("pinch of", "pinches of", false, false),
    SPLASH("splash of", "splashes of", false, false),
    DROP("drop of", "drops of", false, false),
    HANDFUL("handful of", "handfuls of", false, false),
    SPRINKLE("sprinkle of", "sprinkles of", false, false),

    // Baking-related:
    SLICE("slice of", "slices of", true, false),
    LOAF("loaf of", "loaves of", true, false),
    ROLL("roll of", "rolls of", true, false),
    SHEET("sheet of", "sheets of", true, false),
    BLOCK("block of", "blocks of", true, false),
    BAR("bar of", "bars of", true, false),

    // For items without a prefix e.g. carrot:
    UNIT("", "", true, true),

    // Shopping List:
    // for use in the shopping list when a small amount of something must be bought - to say its there,
    // but has no strict unit, its just a single one of that item.
    // The difference to UNIT is that it is not something that is bought frequently e.g. oil, salt,
    // pepper, etc. you buy one, then use it for months.
    // so adding to the shopping list will never increment it. but, users will have the option to themselves if they wish.
    SINGLE("", "", true, false)
}