import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/** A simple predator-prey simulator, based on a rectangular field containing humans */
public class Simulator implements ActionListener
{
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 120;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 80;
    // The probability that a human will be created in any given position.
    private static final double HUMAN_CREATION_PROBABILITY = 0.055;
    // List of humans in the field.
    public final List<Human> allHumans;
    // The current state of the field.
    private final Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private final SimulatorView view;

    /** Construct a simulation field with default size */
    public Simulator() {
        allHumans = new ArrayList<>();
        field = new Field(DEFAULT_DEPTH, DEFAULT_WIDTH);
        // Create a view of the state of each location in the field.
        view = new SimulatorView(DEFAULT_DEPTH, DEFAULT_WIDTH);
        // Setup a valid starting point.
        reset();

        // the Action Listeners for the buttons
        view.button1.addActionListener(this);
        view.button2.addActionListener(this);
        view.button3.addActionListener(this);
    }

    /** Run the simulation from its current state for a reasonably long period (4000 steps) */
    public void runLongSimulation() {
        simulate(200);
    }

    /** Run the simulation for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for */
    public void simulate(int numSteps) {
        for(int step=1; step <= numSteps; step++) {
            simulateOneStep();
            //delay(700);   // run more slowly
        }
    }

    /** Run the simulation from its current state for a single step. Iterate
     * over the whole field updating the state of each fox and rabbit */
    public void simulateOneStep() {
        step++;
        // Provide space for newborn humans.
        List<Human> newBorn = new ArrayList<>();
        // Let all humans act.
        for(Iterator<Human> it = allHumans.iterator(); it.hasNext(); ) {
            Human person = it.next();
            person.move(newBorn);
            if(! person.isAlive()) {
                it.remove();
            }
        }
        // Add the newly born humans to the main list.
        allHumans.addAll(newBorn);
        view.showStatus(step, field);
    }

    /** Reset the simulation to a starting position. */
    public void reset() {
        step = 0;
        allHumans.clear();
        populate();
        // Show the starting state in the view.
        view.showStatus(step, field);
    }

    /** Randomly populate the field with humans */
    private void populate() {
        Random rand = Randomizer.getRandom();
        field.clear();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                if(rand.nextDouble() <= HUMAN_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Human human = new Human(true,true, field, location);
                    human.setHumanColor();
                    view.setColor(human);
                    allHumans.add(human);
                }
                // else leave the location empty.
            }
        }
    }

    /** Pause for a given time.
     * @param millisec  The time to pause for, in milliseconds */
    private void delay(int millisec) {
        try {
            Thread.sleep(millisec);
        }
        catch (InterruptedException ie) {
            // wake up
        }
    }

    /** Recognise the source of the Action Listener and run the appropriate code for each button
     * @param e the action event that indicates which button has been pressed */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == view.button1){
            simulateOneStep();
        }
        else if(e.getSource() == view.button2){
            runLongSimulation();
        }
        else if(e.getSource() == view.button3){
            reset();
        }
    }
}