package model;

/**
 * Practicum 2: Inheritance Concepts - Subclass 1
 * -----------------------------------------------
 * SleeperTicket extends Ticket, demonstrating Single Inheritance ('is-a' relationship).
 * It inherits all attributes and behaviors from Ticket and introduces
 * sleeper-specific attributes such as berth allocation.
 */
public class SleeperTicket extends Ticket {

    // Subclass-specific attribute
    private String berthType;

    // Academic fare rule constant (Demonstration rule for college microproject; not actual Indian Railways fare)
    private static final double SLEEPER_SURCHARGE = 50.0;

    /**
     * Primary Constructor: Uses super(...) to chain to parent constructor.
     *
     * @param passengerName Name of passenger
     * @param trainNumber   Train identification number/code
     * @param baseFare      Standard base ticket fare
     * @param berthType     Preferred berth (e.g. Lower, Middle, Upper, Side Lower, Side Upper)
     */
    public SleeperTicket(String passengerName, String trainNumber, double baseFare, String berthType) {
        // Practicum 2: Calling parent constructor using super() to initialize inherited fields
        super(passengerName, trainNumber, baseFare);
        this.berthType = (berthType == null || berthType.trim().isEmpty()) ? "Lower" : berthType.trim();
    }

    /**
     * Overloaded Constructor: Used when restoring existing tickets from storage.
     *
     * @param pnr           Pre-existing PNR
     * @param passengerName Name of passenger
     * @param trainNumber   Train identification number/code
     * @param baseFare      Standard base ticket fare
     * @param berthType     Preferred berth
     */
    public SleeperTicket(String pnr, String passengerName, String trainNumber, double baseFare, String berthType) {
        // Practicum 2: Calling overloaded parent constructor using super()
        super(pnr, passengerName, trainNumber, baseFare);
        this.berthType = (berthType == null || berthType.trim().isEmpty()) ? "Lower" : berthType.trim();
    }

    /**
     * Practicum 2: Method Overriding
     * -------------------------------
     * Overrides the abstract calculateTotalFare() method defined in Ticket.
     *
     * Documented Fare Rule:
     * Total Fare = Base Fare + Sleeper Surcharge (Rs. 50.0)
     * (Note: This is a simplified academic rule and does not represent actual Indian Railways fares.)
     *
     * @return total fare for sleeper class ticket
     */
    @Override
    public double calculateTotalFare() {
        return getBaseFare() + SLEEPER_SURCHARGE;
    }

    @Override
    public String getTicketType() {
        return "Sleeper Class (SL)";
    }

    public String getBerthType() {
        return berthType;
    }

    public void setBerthType(String berthType) {
        this.berthType = (berthType == null || berthType.trim().isEmpty()) ? "Lower" : berthType.trim();
    }

    public double getSleeperSurcharge() {
        return SLEEPER_SURCHARGE;
    }
}
