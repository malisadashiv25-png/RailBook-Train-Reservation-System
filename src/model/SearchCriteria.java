package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * SearchCriteria Model
 * --------------------
 * Encapsulates the user's train search query:
 * - Origin (From station)
 * - Destination (To station)
 * - Departure date
 * - Passenger count (1 to 6)
 * - Travel class (General, Sleeper, 3A, 2A, 1A)
 */
public class SearchCriteria {

    private String fromStation;
    private String toStation;
    private LocalDate departureDate;
    private int passengerCount;
    private String travelClass;

    public SearchCriteria(String fromStation, String toStation, LocalDate departureDate, int passengerCount, String travelClass) {
        validate(fromStation, toStation, departureDate, passengerCount);
        this.fromStation = fromStation.trim();
        this.toStation = toStation.trim();
        this.departureDate = departureDate;
        this.passengerCount = passengerCount;
        this.travelClass = (travelClass == null || travelClass.trim().isEmpty()) ? "All Classes" : travelClass.trim();
    }

    /**
     * Validates search inputs according to business rules.
     */
    public static void validate(String from, String to, LocalDate date, int passengers) {
        if (from == null || from.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select an origin (From) station.");
        }
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select a destination (To) station.");
        }
        if (from.trim().equalsIgnoreCase(to.trim())) {
            throw new IllegalArgumentException("Origin and Destination stations cannot be identical.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Please select a valid departure date.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Departure date cannot be in the past (" + date + ").");
        }
        if (passengers < 1 || passengers > 6) {
            throw new IllegalArgumentException("Passenger count must be between 1 and 6.");
        }
    }

    public String getFromStation() {
        return fromStation;
    }

    public void setFromStation(String fromStation) {
        this.fromStation = fromStation;
    }

    public String getToStation() {
        return toStation;
    }

    public void setToStation(String toStation) {
        this.toStation = toStation;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }

    public String getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(String travelClass) {
        this.travelClass = travelClass;
    }

    public String getFormattedDate() {
        return departureDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy, EEEE"));
    }

    @Override
    public String toString() {
        return String.format("%s -> %s | %s | %d Passenger(s) | %s",
                fromStation, toStation, getFormattedDate(), passengerCount, travelClass);
    }
}
