package model;

/**
 * Practicum 2: Inheritance Concepts - Subclass 3
 * -----------------------------------------------
 * GeneralTicket extends Ticket, representing unreserved / general second class travel.
 * Demonstrates inheritance and polymorphism with its own specific fare calculation rule.
 */
public class GeneralTicket extends Ticket {

    // Subclass-specific attribute: Validity period of the unreserved ticket in hours
    private int validityHours;

    // Academic fare rule constant (Demonstration rule for college microproject; not actual Indian Railways fare)
    private static final double GENERAL_SERVICE_CHARGE = 15.0;

    /**
     * Primary Constructor: Uses super(...) to chain to parent constructor.
     *
     * @param passengerName Name of passenger
     * @param trainNumber   Train identification number/code
     * @param baseFare      Standard base ticket fare
     * @param validityHours Number of hours this ticket remains valid (defaults to 24 if <= 0)
     */
    public GeneralTicket(String passengerName, String trainNumber, double baseFare, int validityHours) {
        // Practicum 2: Constructor chaining using super()
        super(passengerName, trainNumber, baseFare);
        this.validityHours = (validityHours <= 0) ? 24 : validityHours;
    }

    /**
     * Overloaded Constructor: Used when restoring existing tickets from storage.
     *
     * @param pnr           Pre-existing PNR
     * @param passengerName Name of passenger
     * @param trainNumber   Train identification number/code
     * @param baseFare      Standard base ticket fare
     * @param validityHours Number of hours this ticket remains valid
     */
    public GeneralTicket(String pnr, String passengerName, String trainNumber, double baseFare, int validityHours) {
        // Practicum 2: Calling overloaded parent constructor using super()
        super(pnr, passengerName, trainNumber, baseFare);
        this.validityHours = (validityHours <= 0) ? 24 : validityHours;
    }

    /**
     * Practicum 2: Method Overriding
     * -------------------------------
     * Overrides the abstract calculateTotalFare() method defined in Ticket.
     *
     * Documented Fare Rule:
     * Total Fare = Base Fare + General Service Charge (Rs. 15.0)
     * (Note: This is a simplified academic rule and does not represent actual Indian Railways fares.)
     *
     * @return total fare for general unreserved ticket
     */
    @Override
    public double calculateTotalFare() {
        return getBaseFare() + GENERAL_SERVICE_CHARGE;
    }

    @Override
    public String getTicketType() {
        return "General Unreserved (GEN)";
    }

    public int getValidityHours() {
        return validityHours;
    }

    public void setValidityHours(int validityHours) {
        this.validityHours = (validityHours <= 0) ? 24 : validityHours;
    }

    public double getGeneralServiceCharge() {
        return GENERAL_SERVICE_CHARGE;
    }
}
