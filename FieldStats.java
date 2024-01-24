import java.util.HashMap;

/** This class collects and provides some statistical data on the state
 * of a field. It is flexible: it will create and maintain a counter
 * for any class of object that is found within the field */
public class FieldStats
{
    // Counters for each type of entity (human etc.) in the simulation.
    private HashMap<Class, Counter> counters;
    // Whether the counters are currently up to date.
    private boolean countsValid;
    //counts the dead people.
    public int count;

    /** Construct a FieldStats object */
    public FieldStats() {
        // Set up a collection for counters
        counters = new HashMap<>();
        countsValid = true;
        count = 0;
    }

    /** Get details of what is in the field.
     * @return A string describing what is in the field */
    public String getPopulationDetails(Field field) {
        StringBuffer buffer = new StringBuffer();
        if(!countsValid) {
            generateCounts(field);
        }
        for(Class key : counters.keySet()) {
            Counter info = counters.get(key);
            buffer.append(info.getCount());
            buffer.append(' ');
        }
        return buffer.toString();
    }

    /** Invalidate the current set of statistics; reset all counts to zero */
    public void reset() {
        countsValid = false;
        for(Class key : counters.keySet()) {
            Counter count = counters.get(key);
            count.reset();
        }
    }

    /** Increment the count for class of human.
     * @param human The class to increment */
    public void incrementCount(Class human) {
        Counter count = counters.get(human);
        if(count == null) {
            // We do not have a counter for this species yet.
            // Create one.
            count = new Counter(human.getName());
            counters.put(human, count);
        }

        count.increment();
    }

    /** Indicate that a human count has been completed */
    public void countFinished() {
        countsValid = true;
    }

    /** Determine whether the simulation is still viable. I.e., should it continue to run.
     * @return true If there is more than one species alive */
    public boolean isViable(Field field) {
        // How many counts are non-zero.
        int nonZero = 0;
        if(!countsValid) {
            generateCounts(field);
        }
        for(Class key : counters.keySet()) {
            Counter info = counters.get(key);
            if(info.getCount() > 0) {
                nonZero++;
            }
        }
        return nonZero > 1;
    }

    /** Generate counts of the number of human. These are not kept up to date as human
     * are placed in the field, but only when a request is made for the information.
     * @param field the field to generate the stats for */
    private void generateCounts(Field field) {
        reset();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Object human = field.getObjectAt(row, col);
                if(human != null) {
                    incrementCount(human.getClass());
                }
            }
        }
        countsValid = true;
    }

    /** Count the number of infected people shown in the field
     * @param field the field to generate the stats for
     * @return the number of the infected in the field */
    public int infectedCount(Field field){
        int count = 0;
        reset();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Human human = field.getObjectAt(row, col);
                if(human != null) {
                    if(human.isInfected()){
                        count++;
                    }
                }
            }
        }
        countsValid = true;
        return count;
    }

    /** Count the number of vaccinated people shown in the field
     * @param field the field to generate the stats for
     * @return the number of the infected in the field */
    public int vaccinatedCount(Field field){
        int count = 0;
        reset();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Human human = field.getObjectAt(row, col);
                if(human != null) {
                    if(human.isVaccinated()){
                        count++;
                    }
                }
            }
        }
        countsValid = true;
        return count;
    }

    /** Count the number of dead people until this moment of the simulation
     * @param field the field to generate the stats for
     * @return the number of the dead until this moment */
    public int deadCount(Field field){
        reset();
        if(vaccinatedCount(field) == 0){
            return 0;
        }
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Human human = field.getObjectAt(row, col);
                if(human != null) {
                    if(human.getAge() == 80){
                        count++;
                    }
                }
            }
        }
        countsValid = true;
        return count;
    }

}

