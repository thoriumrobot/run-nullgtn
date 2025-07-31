package de.zuellich.meal_planner.pinterest.services;

import de.zuellich.meal_planner.pinterest.datatypes.Board;
import de.zuellich.meal_planner.pinterest.datatypes.BoardListing;
import de.zuellich.meal_planner.pinterest.datatypes.Pin;
import java.util.List;
import javax.annotation.Nullable;

/**
 */
public interface IBoardService {

    /**
     * @return A list of the users boards or an empty list of none found.
     */
    List<Board> getBoards();

    List<Pin> getPins(@Nullable() String boardId);

    /**
     * Retrieve a listing of the boards basic properties and its pins.
     *
     * @param boardId The board to retrieve.
     * @return The listing.
     */
    BoardListing getBoardListing(@Nullable() String boardId);
}
