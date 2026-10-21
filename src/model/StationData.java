package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Predefined list of demonstration railway stations.
 * Extensible for Indian Railways route simulations.
 */
public class StationData {

    private static final List<String> STATIONS = new ArrayList<>();

    static {
        STATIONS.add("Bengaluru (SBC)");
        STATIONS.add("Mysuru (MYS)");
        STATIONS.add("Chennai (MAS)");
        STATIONS.add("Mumbai Central (MMCT)");
        STATIONS.add("New Delhi (NDLS)");
        STATIONS.add("Hyderabad (HYB)");
        STATIONS.add("Pune Junction (PUNE)");
        STATIONS.add("Hubballi (UBL)");
        STATIONS.add("Kolkata Howrah (HWH)");
        STATIONS.add("Ahmedabad (ADI)");
    }

    /**
     * Returns an unmodifiable list of available demonstration stations.
     */
    public static List<String> getStations() {
        return Collections.unmodifiableList(STATIONS);
    }

    /**
     * Adds a new demonstration station to the system.
     */
    public static void addStation(String stationName) {
        if (stationName != null && !stationName.trim().isEmpty()) {
            String clean = stationName.trim();
            if (!STATIONS.contains(clean)) {
                STATIONS.add(clean);
            }
        }
    }

    /**
     * Extracts station code if formatted as "City (CODE)".
     */
    public static String extractCode(String stationLabel) {
        if (stationLabel != null && stationLabel.contains("(") && stationLabel.contains(")")) {
            int start = stationLabel.indexOf('(') + 1;
            int end = stationLabel.indexOf(')');
            return stationLabel.substring(start, end).trim();
        }
        return stationLabel != null ? stationLabel.trim() : "";
    }
}
