package model;

/**
 * Practicum 2: Inheritance Concepts - Subclass 2
 * -----------------------------------------------
 * ACTierTicket extends Ticket, representing air-conditioned railway accommodation.
 * Demonstrates hierarchical inheritance by adding AC-tier specific properties
 * and computing fare with tier surcharges and GST.
 */
public class ACTierTicket extends Ticket {

    // Subclass-specific attribute: 1 for 1st AC, 2 for 2nd AC, 3 for 3rd AC
    private int acTier;

    // Academic fare rule constants (Demonstration rule for college microproject; not actual Indian Railways fare)
    private static final double GST_RATE = 0.05; // 5% GST on AC travel
    private static final double TIER_1_SURCHARGE = 500.0;
    private static final double TIER_2_SURCHARGE = 300.0;
    private static final double TIER_3_SURCHARGE = 150.0;

    /**
     * Primary Constructor: Uses super(...) to chain to parent constructor.
     *
     * @param passengerName Name of passenger
     * @param trainNumber   Train identification number/code
     * @param baseFare      Standard base ticket fare
     * @param acTier        AC Tier level (1 = 1A, 2 = 2A, 3 = 3A)
     */
    public ACTierTicket(String passengerName, String trainNumber, double baseFare, int acTier) {
        // Practicum 2: Constructor chaining using super()
        super(passengerName, trainNumber, baseFare);
        validateTier(acTier);
        this.acTier = acTier;
    }

    /**
     * Overloaded Constructor: Used when restoring existing tickets from storage.
     *
     * @param pnr           Pre-existing PNR
     * @param passengerName Name of passenger
     * @param trainNumber   Train identification number/code
     * @param baseFare      Standard base ticket fare
     * @param acTier        AC Tier level (1 = 1A, 2 = 2A, 3 = 3A)
     */
    public ACTierTicket(String pnr, String passengerName, String trainNumber, double baseFare, int acTier) {
        // Practicum 2: Calling overloaded parent constructor using super()
        super(pnr, passengerName, trainNumber, baseFare);
        validateTier(acTier);
        this.acTier = acTier;
    }

    private void validateTier(int tier) {
        if (tier < 1 || tier > 3) {
            throw new IllegalArgumentException("Validation Error: Invalid AC Tier (" + tier + "). Allowed tiers are 1 (1A), 2 (2A), or 3 (3A).");
        }
    }

    /**
     * Practicum 2: Method Overriding
     * -------------------------------
     * Overrides the abstract calculateTotalFare() method defined in Ticket.
     *
     * Documented Fare Rule:
     * 1. Surcharge: 1A = Rs. 500, 2A = Rs. 300, 3A = Rs. 150
     * 2. Subtotal = Base Fare + Surcharge
     * 3. GST = Subtotal * 5%
     * 4. Total Fare = Subtotal + GST
     * (Note: This is a simplified academic rule and does not represent actual Indian Railways fares.)
     *
     * @return total fare including tier surcharge and GST
     */
    @Override
    public double calculateTotalFare() {
        double tierSurcharge;
        switch (acTier) {
            case 1:
                tierSurcharge = TIER_1_SURCHARGE;
                break;
            case 2:
                tierSurcharge = TIER_2_SURCHARGE;
                break;
            case 3:
            default:
                tierSurcharge = TIER_3_SURCHARGE;
                break;
        }

        double subtotal = getBaseFare() + tierSurcharge;
        double gst = subtotal * GST_RATE;
        return subtotal + gst;
    }

    @Override
    public String getTicketType() {
        return "AC " + acTier + "-Tier (" + acTier + "A)";
    }

    public int getAcTier() {
        return acTier;
    }

    public void setAcTier(int acTier) {
        validateTier(acTier);
        this.acTier = acTier;
    }

    public double getTierSurcharge() {
        switch (acTier) {
            case 1: return TIER_1_SURCHARGE;
            case 2: return TIER_2_SURCHARGE;
            default: return TIER_3_SURCHARGE;
        }
    }

    public double getGstRate() {
        return GST_RATE;
    }
}
