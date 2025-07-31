package de.zuellich.meal_planner.algorithms;

import de.zuellich.meal_planner.datatypes.Recipe;
import de.zuellich.meal_planner.datatypes.RecipeFormat;
import org.springframework.stereotype.Service;
import javax.annotation.Nullable;

/**
 */
@Service
public class NullParser implements RecipeParser {

    @Override
    public Recipe parse(final String source) {
        return new NullRecipe();
    }

    @Override
    public RecipeFormat getFormat() {
        return RecipeFormat.UNKNOWN;
    }
}
