package de.zuellich.meal_planner.algorithms;

import de.zuellich.meal_planner.datatypes.Recipe;
import de.zuellich.meal_planner.datatypes.RecipeFormat;
import javax.annotation.Nullable;

/**
 */
public interface RecipeParser {

    /**
     * @param source The source format to parse.
     * @return The parsed recipe instance.
     */
    @Nullable()
    Recipe parse(@Nullable() String source);

    @Nullable()
    RecipeFormat getFormat();
}
