package model;

import java.util.Random;

/**
 * Practicum 2: Inheritance Concepts - Abstract Base Class
 * --------------------------------------------------------
 * An abstract class cannot be instantiated on its own with the 'new' keyword.
 * It serves as a parent template that encapsulates common fields and methods
 * shared by all train ticket categories (Sleeper, AC Tier, and General).
 */
public abstract class Ticket {

    // Common ticket attributes (Encapsulated using private access modifier)
    private String pnr;
    private String passengerName;
    private String trainNumber;
    private double baseFare;

    /**
     * Primary Constructor: Automatically generates a unique PNR.
     * Used when booking a new ticket in the system.
     *
     * @param passengerName Name of passenger
     * @param trainNumber   Train identification number/code
     * @param baseFare      Standard base ticket fare (must be >= 0)
     */
    public Ticket(String passengerName, String trainNumber, double baseFare) {
        validatePassengerName(passengerName);
        validateTrainNumber(trainNumber);
        validateBaseFare(baseFare);

        this.pnr = generateUniquePNR();
        this.passengerName = passengerName.trim();
        this.trainNumber = trainNumber.trim();
        this.baseFare = baseFare;
    }

    /**
     * Overloaded Constructor: Accepts an existing PNR identifier.
     * Useful when reconstructing ticket objects from file storage or databases.
     *
     * @param pnr           Pre-existing PNR identifier
     * @param passengerName Name of passenger
     * @param trainNumber   Train identification number/code
     * @param baseFare      Standard base ticket fare (must be >= 0)
     */
    public Ticket(String pnr, String passengerName, String trainNumber, double baseFare) {
        validatePNR(pnr);
        validatePassengerName(passengerName);
        validateTrainNumber(trainNumber);
        validateBaseFare(baseFare);

        this.pnr = pnr.trim();
        this.passengerName = passengerName.trim();
        this.trainNumber = trainNumber.trim();
        this.baseFare = baseFare;
    }

    /**
     * Practicum 2: Abstract Method
     * -----------------------------
     * Declared without a method body. Every concrete subclass (SleeperTicket,
     * ACTierTicket, GeneralTicket) MUST override this method and implement
     * its own class-specific pricing algorithm.
     *
     * @return calculated total fare for this ticket
     */
    public abstract double calculateTotalFare();

    /**
     * Abstract method returning the display name / category of the ticket.
     *
     * @return String description of ticket class
     */
    public abstract String getTicketType();

    // ==========================================
    // Input Validation Methods
    // ==========================================

    private void validatePNR(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            throw new IllegalArgumentException("Validation Error: PNR cannot be empty.");
        }
    }

    private void validatePassengerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Validation Error: Passenger name cannot be empty.");
        }
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("Validation Error: Passenger name must have at least 2 characters.");
        }
    }

    private void validateTrainNumber(String trainNum) {
        if (trainNum == null || trainNum.trim().isEmpty()) {
            throw new IllegalArgumentException("Validation Error: Train number cannot be empty.");
        }
        if (!trainNum.trim().matches("^[A-Za-z0-9]{3,10}$")) {
            throw new IllegalArgumentException("Validation Error: Train number must be 3 to 10 alphanumeric characters (e.g., 12951 or EXP101).");
        }
    }

    private void validateBaseFare(double fare) {
        if (fare < 0.0) {
            throw new IllegalArgumentException("Validation Error: Base fare cannot be negative. Given: " + fare);
        }
    }

    /**
     * Generates a unique 10-character PNR identifier.
     * Example format: PNR + 7 digits (e.g., PNR4920193)
     *
     * @return unique PNR String
     */
    public static synchronized String generateUniquePNR() {
        Random random = new Random();
        int randomPart = 1000000 + random.nextInt(9000000); // 7-digit random number
        return "PNR" + randomPart;
    }

    // ==========================================
    // Getter & Setter Methods
    // ==========================================

    public String getPnr() {
        return pnr;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        validatePassengerName(passengerName);
        this.passengerName = passengerName.trim();
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        validateTrainNumber(trainNumber);
        this.trainNumber = trainNumber.trim();
    }

    public double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(double baseFare) {
        validateBaseFare(baseFare);
        this.baseFare = baseFare;
    }

    @Override
    public String toString() {
        return String.format("[%s] PNR: %s | Passenger: %s | Train: %s | Base Fare: Rs. %.2f | Total Fare: Rs. %.2f",
                getTicketType(), pnr, passengerName, trainNumber, baseFare, calculateTotalFare());
    }
}
