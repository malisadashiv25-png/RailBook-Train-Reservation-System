package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DemonstrationTrain Model
 * ------------------------
 * Represents simulated demonstration train schedules for academic practicum exploration.
 * Provides realistic train numbers, names, journey timings, durations, and simulated class availability.
 *
 * NOTE: All data represents simulated academic demonstration schedules.
 */
public class DemonstrationTrain {

    private final String trainNumber;
    private final String trainName;
    private final String origin;
    private final String destination;
    private final String departureTime;
    private final String arrivalTime;
    private final String duration;
    private final String daysOfRun;
    private final double baseFare;
    private final int availableGeneral;
    private final int availableSleeper;
    private final int available3A;
    private final int available2A;
    private final int available1A;

    public DemonstrationTrain(String trainNumber, String trainName, String origin, String destination,
                              String departureTime, String arrivalTime, String duration, String daysOfRun,
                              double baseFare, int availableGeneral, int availableSleeper,
                              int available3A, int available2A, int available1A) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.duration = duration;
        this.daysOfRun = daysOfRun;
        this.baseFare = baseFare;
        this.availableGeneral = availableGeneral;
        this.availableSleeper = availableSleeper;
        this.available3A = available3A;
        this.available2A = available2A;
        this.available1A = available1A;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public String getFullName() {
        return trainNumber + " - " + trainName;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getDuration() {
        return duration;
    }

    public String getDaysOfRun() {
        return daysOfRun;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public int getAvailableGeneral() {
        return availableGeneral;
    }

    public int getAvailableSleeper() {
        return availableSleeper;
    }

    public int getAvailable3A() {
        return available3A;
    }

    public int getAvailable2A() {
        return available2A;
    }

    public int getAvailable1A() {
        return available1A;
    }

    /**
     * Calculates simulated fare for a specific travel class based on base fare.
     */
    public double getFareForClass(String travelClass) {
        if (travelClass == null) return baseFare;
        String cl = travelClass.toLowerCase();
        if (cl.contains("1") || cl.contains("1a")) {
            return baseFare * 2.8 + 250.0;
        } else if (cl.contains("2") || cl.contains("2a")) {
            return baseFare * 2.0 + 150.0;
        } else if (cl.contains("3") || cl.contains("3a")) {
            return baseFare * 1.5 + 80.0;
        } else if (cl.contains("general") || cl.contains("gn")) {
            return Math.max(120.0, baseFare * 0.45);
        } else {
            // Sleeper default
            return baseFare;
        }
    }

    /**
     * Returns availability status string for a given class.
     */
    public String getStatusForClass(String travelClass) {
        if (travelClass == null) return "AVAILABLE";
        String cl = travelClass.toLowerCase();
        int count;
        if (cl.contains("1") || cl.contains("1a")) count = available1A;
        else if (cl.contains("2") || cl.contains("2a")) count = available2A;
        else if (cl.contains("3") || cl.contains("3a")) count = available3A;
        else if (cl.contains("general") || cl.contains("gn")) count = availableGeneral;
        else count = availableSleeper;

        if (count > 10) return "AVL " + count;
        if (count > 0) return "RAC " + (15 - count);
        return "WL " + Math.abs(count - 5);
    }

    /**
     * Generates realistic demonstration trains for any origin and destination pair.
     */
    public static List<DemonstrationTrain> getTrainsForRoute(String origin, String destination, LocalDate date) {
        List<DemonstrationTrain> list = new ArrayList<>();
        if (origin == null || destination == null) return list;

        String origCity = StationData.extractCode(origin);
        String destCity = StationData.extractCode(destination);

        // Train 1: Morning Superfast Express
        list.add(new DemonstrationTrain(
                "126" + Math.abs((origin + destination).hashCode() % 90 + 10),
                origCity + " — " + destCity + " Superfast Express",
                origin, destination,
                "06:15 AM", "01:45 PM", "07h 30m", "Daily",
                450.0, 140, 54, 22, 8, 3
        ));

        // Train 2: Premium Vande Bharat / Shatabdi Express
        list.add(new DemonstrationTrain(
                "206" + Math.abs((destination + origin).hashCode() % 90 + 10),
                origCity + " — " + destCity + " Vande Bharat Express",
                origin, destination,
                "02:30 PM", "08:15 PM", "05h 45m", "Except Wed",
                680.0, 95, 0, 48, 16, 6
        ));

        // Train 3: Overnight Mail / Express
        list.add(new DemonstrationTrain(
                "165" + Math.abs((origin.length() + destination.length()) % 90 + 10),
                origCity + " — " + destCity + " Night Express",
                origin, destination,
                "09:45 PM", "06:10 AM", "08h 25m", "Daily",
                420.0, 160, 68, 30, 12, 4
        ));

        // Train 4: Rajdhani / Duronto Special
        list.add(new DemonstrationTrain(
                "122" + Math.abs((origin.hashCode()) % 90 + 10),
                origCity + " — " + destCity + " AC Special",
                origin, destination,
                "05:00 PM", "11:20 PM", "06h 20m", "Tue, Thu, Sun",
                820.0, 0, 0, 36, 14, 5
        ));

        return list;
    }
}
