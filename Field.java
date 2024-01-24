import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/** Represent a rectangular grid of field positions.
 * Each position is able to store a single person */
public class Field
{
    // A random number generator for providing random locations.
    private static final Random rand = Randomizer.getRandom();
    // The depth and width of the field.
    private final int depth, width;
    // Storage for the humans.
    private final Human[][] field;

    /** Represent a field of the given dimensions.
     * @param depth The depth of the field.
     * @param width The width of the field */
    public Field(int depth, int width) {
        this.depth = depth;
        this.width = width;
        field = new Human[depth][width];
    }

    /** Empty the field */
    public void clear() {
        for(int row = 0; row < depth; row++) {
            for(int col = 0; col < width; col++) {
                field[row][col] = null;
            }
        }
    }

    /** Clear the given location.
     * @param location The location to clear */
    public void clear(Location location) {
        field[location.getRow()][location.getCol()] = null;
    }

    /** Place a person at the given location.
     * If there is already a person at the location it will be lost.
     * @param person The human to be placed.
     * @param row Row coordinate of the location.
     * @param col Column coordinate of the location */
    public void place(Human person, int row, int col) {
        place(person, new Location(row, col));
    }

    /** Place a person at the given location.
     * If there is already an animal at the location it will be lost.
     * @param person The human to be placed.
     * @param location Where to place the animal.
     */
    public void place(Human person, Location location) {
        field[location.getRow()][location.getCol()] = person;
    }

    /** Return the human at the given location, if any.
     * @param location Where in the field.
     * @return The human at the given location, or null if there is none */
    public Human getObjectAt(Location location) {
        return getObjectAt(location.getRow(), location.getCol());
    }

    /** Return the human at the given location, if any.
     * @param row The desired row.
     * @param col The desired column.
     * @return The human at the given location, or null if there is none */
    public Human getObjectAt(int row, int col) {
        return field[row][col];
    }

    /** Generate a random location that is adjacent to the given location, or is the same location.
     * The returned location will be within the valid bounds of the field.
     * @param location The location from which to generate an adjacency.
     * @return A valid location within the grid area */
    public Location randomAdjacentLocation(Location location) {
        List<Location> adjacent = adjacentLocations(location);
        return adjacent.get(0);
    }

    /** Check if there is an infected person at one of the four adjacent locations.
     * Only North, South, East and West (not diagonal, because it's not adjacent).
     * @param loc the location of the current human
     * @return true if there is infected human at one of the four adjacent locations */
    public boolean infection(Location loc){
        int r = loc.getRow();
        int c = loc.getCol();
        boolean right = false;
        boolean left = false;
        boolean  up = false;
        boolean down = false;

        if(c+1 < width) //check on the right
            right = getObjectAt(r, c + 1) != null && getObjectAt(r, c + 1).isInfected() && !getObjectAt(r, c + 1).isQuarantine();

        if(c-1 >= 0) //check on the left
            left = getObjectAt(r, c - 1) != null && getObjectAt(r, c - 1).isInfected() && !getObjectAt(r, c - 1).isQuarantine() ;

        if(r-1 >= 0) //check above
            up = getObjectAt(r - 1, c) != null && getObjectAt(r - 1, c).isInfected() && !getObjectAt(r - 1, c).isQuarantine();

        if(r+1 < depth)  //check below
            down = getObjectAt(r + 1, c) != null && getObjectAt(r + 1, c).isInfected() && !getObjectAt(r + 1, c).isQuarantine();

        return right || left || up || down;
    }

    /** Get a shuffled list of the free adjacent locations.
     * @param location Get locations adjacent to this.
     * @return A list of free adjacent locations */
    public List<Location> getFreeAdjacentLocations(Location location) {
        List<Location> free = new LinkedList<>();
        List<Location> adjacent = adjacentLocations(location);
        for(Location next : adjacent) {
            if(getObjectAt(next) == null) {
                free.add(next);
            }
        }
        return free;
    }

    /** Try to find a free location that is adjacent to the given location. If there is none, return null.
     * The returned location will be within the valid bounds of the field.
     * @param location The location from which to generate an adjacency.
     * @return A valid location within the grid area */
    public Location freeAdjacentLocation(Location location) {
        // The available free ones.
        List<Location> free = getFreeAdjacentLocations(location);
        if(free.size() > 0) {
            return free.get(0);
        }
        else {
            return null;
        }
    }

    /** Return a shuffled list of locations adjacent to the given one.
     * The list will not include the location itself. All locations will lie within the grid.
     * @param location The location from which to generate adjacencies.
     * @return A list of locations adjacent to that given */
    public List<Location> adjacentLocations(Location location) {
        assert location != null : "Null location passed to adjacentLocations";
        // The list of locations to be returned.
        List<Location> locations = new LinkedList<>();
        if(location != null) {
            int row = location.getRow();
            int col = location.getCol();
            for(int roffset = -1; roffset <= 1; roffset++) {
                int nextRow = row + roffset;
                if(nextRow >= 0 && nextRow < depth) {
                    for(int coffset = -1; coffset <= 1; coffset++) {
                        int nextCol = col + coffset;
                        // Exclude invalid locations and the original location.
                        if(nextCol >= 0 && nextCol < width && (roffset != 0 || coffset != 0)) {
                            locations.add(new Location(nextRow, nextCol));
                        }
                    }
                }
            }
            // Shuffle the list. Several other methods rely on the list
            // being in a random order.
            Collections.shuffle(locations, rand);
        }
        return locations;
    }

    /** @return The depth of the field */
    public int getDepth() {
        return depth;
    }

    /** @return The width of the field */
    public int getWidth() {
        return width;
    }
}

