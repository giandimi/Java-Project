import java.awt.*;
import java.util.List;
import java.util.Random;

/** A simple model of a human.
 * humans age, move, breed, get infected, get vaccinated, be in quarantine and die */
public class Human
{
    // Characteristics shared by all humans (class variables).

    // The age at which a human can start to breed.
    private static final int BREEDING_AGE = 27;
    // The age at which a human stops breeding.
    private static final int MAX_BREEDING_AGE = 40;
    // The age to which a human can live.
    private static final int MAX_AGE = 80;
    // The likelihood of a human breeding.
    private static final double BREEDING_PROBABILITY = 0.23;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 1;
    // The probability of a human's infection.
    private static final double INFECTING_PROBABILITY = 1;
    // The probability of a human's start simulation infected.
    private static final double INFECTED_PROBABILITY = 0.7;
    // The probability of someone getting vaccinated.
    private static final double VACCINATING_PROBABILITY = 0.05;
    // The probability an infected person get in quarantine.
    private static final double QUARANTINE_PROBABILITY = 0.2;
    // The probability of someone vaccinated getting infected.
    private static final double UNSAFE_PROBABILITY = 0.1;
    // The likelihood of an infected human get deceased;
    private static final double DEATH_PROBABILITY = 0.065;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();

    // Individual characteristics (instance fields).

    //the human's gender. false for female and true for female.
    private final boolean sex;
    // The human's age.
    private int age;
    // Whether the human is alive or not.
    private boolean alive;
    //Whether the human is infected by the virus
    private boolean infected;
    // the number of days someone is infected.
    private int infectionDays;
    // If the human is in quarantine or not.
    private boolean quarantine;
    // The color human appears on the map;
    private Color humanColor;
    //Whether the human is vaccinated;
    private boolean vaccinated;
    // The human's position.
    private Location location;
    // The field occupied.
    private Field field;

    /** Create a new human.
     * @param randomAge If true, the human will have a random age.
     * @param randInfected if true, the human will randomly be infected or not
     * @param field The field currently occupied.
     * @param location The location within the field */
    public Human(boolean randomAge,boolean randInfected, Field field, Location location) {
        humanColor = Color.BLUE;
        alive = true;
        age = 12;
        infected = false;
        vaccinated = false;
        quarantine = false;
        infectionDays = 0;
        sex = rand.nextInt(2) != 0;
        this.field = field;
        setLocation(location);
        if(randomAge) {
            do {
                age = rand.nextInt(MAX_AGE);
            }while(age <= 12);
        }
        if(randInfected){
            infected = rand.nextDouble() <= INFECTED_PROBABILITY;
            if(infected){
                setHumanColor();
                infectionDays = rand.nextInt(14) + 1;
            }
        }
    }

    /** Represents a person's step, in which a person can move to another position,
     * can get older, can breed, can die, can be in quarantine and can get infected by other people.
     * @param newBorn A list to return newly born humans */
    public void move(List<Human> newBorn) {
        incrementAge();
        if(alive) {
            infectionDaysIncrement();
            if(isInfected()){
                quarantine = rand.nextDouble() <= QUARANTINE_PROBABILITY;
                if(rand.nextDouble() <= DEATH_PROBABILITY){
                    setDead();
                    return;
                }
            }
            else if(!isVaccinated()){
                vaccinated = rand.nextDouble() <= VACCINATING_PROBABILITY;
                setHumanColor();
            }
            giveBirth(newBorn);
            // Try to move into a free location.
            Location newLocation = field.freeAdjacentLocation(location);
            if(newLocation != null) {
                setLocation(newLocation);
                if(field.infection(newLocation) && !infected){
                    if(isVaccinated()) {
                        infected = rand.nextDouble() <= UNSAFE_PROBABILITY;
                    }
                    else{
                        infected = rand.nextDouble() <= INFECTING_PROBABILITY;
                    }
                    setHumanColor();
                }
            }
            else {
                // Overcrowding.
                setDead();
            }
        }
    }

    /** Check whether the human is alive or not.
     * @return true if the human is still alive */
    public boolean isAlive() {
        return alive;
    }

    /** Check whether the human is infected by the virus
     * @return true if he is infected */
    public boolean isInfected(){
        return infected;
    }

    /** @return the age of the human */
    public int getAge(){
        return age;
    }

    /** Check whether the human is infected
     * @return true if he is infected */
    public boolean isVaccinated(){
        return vaccinated;
    }

    /** Check if the infected human is in quarantine
     * @return true if the human is in quarantine */
    public boolean isQuarantine(){
        return quarantine;
    }

    /** If a human is infected, Increases the number of days of his infection */
    public void infectionDaysIncrement(){
        if(isInfected()){
            if(infectionDays <= 13)
                infectionDays ++;
            else {
                infectionDays = 0;
                infected = false;
                //quarantine = false;
            }
        }
    }

    /** @return a person's view color */
    public Color getHumanColor(){
        return humanColor;
    }

    /** Set a person's view color, depending on the person's infection status */
    public void setHumanColor(){
        if(isInfected())
            humanColor = Color.RED;
        else if(isVaccinated() && !isInfected())
            humanColor = Color.GREEN;
        else
            humanColor = Color.BLUE;
    }

    /** @return the number of the days the human is infected */
    public int getInfectionDays(){
        return infectionDays;
    }

    /** Indicate that the human is no longer alive. It is removed from the field */
    public void setDead() {
        alive = false;
        if(location != null) {
            field.clear(location);
            location = null;
            field = null;
        }
    }

    /** @return The human's location */
    public Location getLocation() {
        return location;
    }

    /** Place the human at the new location in the given field.
     * @param newLocation The human's new location */
    private void setLocation(Location newLocation) {
        if(location != null) {
            field.clear(location);
        }
        location = newLocation;
        field.place(this, newLocation);
    }

    /** Increase the age. This could result in the human's death */
    private void incrementAge() {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    /** Check whether or not this human is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newBorn A list to return newly born rabbits */
    private void giveBirth(List<Human> newBorn) {
        // New human is born into adjacent locations.
        // Get a list of adjacent free locations.
        List<Location> free = field.getFreeAdjacentLocations(location);
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Human young = new Human(false,false,field, loc);
            newBorn.add(young);
        }
    }

    /** Generate a number representing the number of births, if it can breed.
     * @return The number of births (may be zero) */
    private int breed() {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /** A human can breed if it has reached the breeding age.
     * @return true if the human can breed, false otherwise */
    private boolean canBreed() {
        return (!sex && age >= BREEDING_AGE && age <= MAX_BREEDING_AGE);
    }
}
