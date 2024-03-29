import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.util.LinkedHashMap;
import java.util.Map;

/** A graphical view of the simulation grid.
 * The view displays a colored rectangle for each location
 * representing its contents. It uses a default background color.
 * Colors for each type of species can be defined using the
 * setColor method */
public class SimulatorView extends JFrame
{
    // Colors used for empty locations.
    private static final Color EMPTY_COLOR = Color.white;

    // Color used for objects that have no defined color.
    private static final Color UNKNOWN_COLOR = Color.gray;

    private final String STEP = "Step: ";
    private final String POPULATION = "Population: ";
    private final String INFECTED = "Infected: ";
    private final String DEAD = "dead: ";
    private final String VACCINATED = "vaccinated: ";
    private final JLabel stepLabel, population, infected, dead, vaccinated;
    public JButton button1, button2, button3;
    private final FieldView fieldView;

    // A map for storing colors for participants in the simulation
    private Map<Human, Color> colors;
    // A statistics object computing and storing simulation information
    private FieldStats stats;

    /** Create a view of the given width and height.
     * @param height The simulation's height.
     * @param width  The simulation's width */
    public SimulatorView(int height, int width) {
        stats = new FieldStats();
        colors = new LinkedHashMap<>();

        setTitle("Virus Simulation");
        stepLabel = new JLabel(STEP, JLabel.CENTER);
        population = new JLabel(POPULATION, JLabel.CENTER);
        infected = new JLabel(INFECTED, JLabel.CENTER);
        dead = new JLabel(DEAD,JLabel.CENTER);
        vaccinated = new JLabel(VACCINATED,JLabel.CENTER);

        setLocation(100, 50);

        fieldView = new FieldView(height, width);

        button1 = new JButton();
        button1.add(stepLabel);
        button2 = new JButton("200 steps");
        button3 = new JButton("reset");

        // the panel with the step buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new LineBorder(Color.BLACK));
        bottomPanel.setBackground(Color.GRAY);
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(button1);
        bottomPanel.add(button2);
        bottomPanel.add(button3);

        // the status of the people in the field
        // it is contained in the side panel
        JPanel status = new JPanel();
        status.setLayout(new GridLayout(5,1,10,10));
        status.setBorder(new LineBorder(Color.LIGHT_GRAY,3));
        status.setBackground(Color.LIGHT_GRAY);
        status.add(population);
        status.add(infected);
        status.add(vaccinated);
        status.add(dead);


        // They indicate the colors that separates every category of human in the field
        JLabel l1 = new JLabel("NORMAL",JLabel.CENTER);
        l1.setForeground(Color.BLUE);

        JLabel l2 = new JLabel("INFECTED",JLabel.CENTER);
        l2.setForeground(Color.RED);

        JLabel l3 = new JLabel("VACCINATED",JLabel.CENTER);
        l3.setForeground(Color.GREEN);

        // contains the labels that point the color
        // it is contained in the side panel
        JPanel colors = new JPanel();
        colors.setLayout(new GridLayout(3,1,10,10));
        colors.setBorder(new LineBorder(Color.ORANGE,3));
        colors.setBackground(Color.WHITE);
        colors.add(l1);
        colors.add(l2);
        colors.add(l3);

        //the panel that contains the status and the colors
        JPanel sidePanel = new JPanel();
        sidePanel.setBorder(new LineBorder(Color.BLACK,1));
        sidePanel.setBackground(Color.LIGHT_GRAY);
        sidePanel.setLayout(new GridLayout(3,1,10,10));
        sidePanel.add(status);
        sidePanel.add(colors);

        Container contents = getContentPane();
        contents.add(bottomPanel,BorderLayout.SOUTH);
        contents.add(sidePanel,BorderLayout.WEST);
        contents.add(fieldView, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /** Define a color to be used for a given class.
     * @param human Human's object*/
    public void setColor(Human human) {
        colors.put(human,human.getHumanColor());
    }

    /** @return The color to be used for a given class */
   private Color getColor(Human human) {
        Color col = human.getHumanColor();
        if(col == null) {
            // no color defined for this class
            return UNKNOWN_COLOR;
        }
        else {
            return col;
        }
    }

    /** Show the current status of the field.
     * @param step Which iteration step it is.
     * @param field The field whose status is to be displayed */
    public void showStatus(int step, Field field) {
        if(!isVisible()) {
            setVisible(true);
        }

        stepLabel.setText(STEP + step);
        stats.reset();

        fieldView.preparePaint();

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Human animal = field.getObjectAt(row, col);
                if(animal != null) {
                    stats.incrementCount(animal.getClass());
                    fieldView.drawMark(col, row, getColor(animal));
                }
                else {
                    fieldView.drawMark(col, row, EMPTY_COLOR);
                }
            }
        }
        stats.countFinished();
        population.setText(POPULATION + stats.getPopulationDetails(field));
        infected.setText(INFECTED + stats.infectedCount(field));
        vaccinated.setText(VACCINATED + stats.vaccinatedCount(field));
        dead.setText(DEAD + stats.deadCount(field));
        fieldView.repaint();
    }

    /** Determine whether the simulation should continue to run.
     * @return true If there is more than one species alive */
    public boolean isViable(Field field) {
        return stats.isViable(field);
    }

    /** Provide a graphical view of a rectangular field. This is a nested class (a class defined inside a class)
     * which defines a custom component for the user interface. This component displays the field */
    private class FieldView extends JPanel
    {
        private final int GRID_VIEW_SCALING_FACTOR = 6;

        private final int gridWidth, gridHeight;
        private int xScale, yScale;
        Dimension size;
        private Graphics g;
        private Image fieldImage;

        /** Create a new FieldView component */
        public FieldView(int height, int width) {
            gridHeight = height;
            gridWidth = width - 16;
            size = new Dimension(0, 0);
            setResizable(true);
            setBorder(new LineBorder(Color.BLACK,1));
        }

        /** Tell the GUI manager how big we would like to be */
        public Dimension getPreferredSize() {
            return new Dimension(gridWidth * GRID_VIEW_SCALING_FACTOR,
                    gridHeight * GRID_VIEW_SCALING_FACTOR);
        }

        /** Prepare for a new round of painting. Since the component
         * may be resized, compute the scaling factor again */
        public void preparePaint() {
            if(! size.equals(getSize())) {  // if the size has changed...
                size = getSize();
                fieldImage = fieldView.createImage(size.width, size.height);
                g = fieldImage.getGraphics();

                xScale = size.width / gridWidth;
                if(xScale < 1) {
                    xScale = GRID_VIEW_SCALING_FACTOR;
                }
                yScale = size.height / gridHeight;
                if(yScale < 1) {
                    yScale = GRID_VIEW_SCALING_FACTOR;
                }
            }
        }

        /** Paint on grid location on this field in a given color */
        public void drawMark(int x, int y, Color color) {
            g.setColor(color);
            g.fillRect(x * xScale, y * yScale, xScale-1, yScale-1);
        }

        /** The field view component needs to be redisplayed.
         * Copy the internal image to screen */
        public void paintComponent(Graphics g) {
            if(fieldImage != null) {
                Dimension currentSize = getSize();
                if(size.equals(currentSize)) {
                    g.drawImage(fieldImage, 0, 0, null);
                }
                else {
                    // Rescale the previous image.
                    g.drawImage(fieldImage, 0, 0, currentSize.width, currentSize.height, null);
                }
            }
        }
    }
}
