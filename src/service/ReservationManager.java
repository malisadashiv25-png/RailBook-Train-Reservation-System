package service;

import model.ACTierTicket;
import model.GeneralTicket;
import model.SleeperTicket;
import model.Ticket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Practicum 10: ArrayList & Practicum 12: File Handling
 * -----------------------------------------------------
 * ReservationManager handles business logic and persistent storage for tickets:
 * - Practicum 10: Uses ArrayList<Ticket> for in-memory dynamic storage, search, and cancellation.
 * - Practicum 12: Uses BufferedReader and BufferedWriter with try-with-resources
 *   to persist reservations to 'reservations.txt' and generate printable receipts.
 */
public class ReservationManager {

    // Default persistence file name
    public static final String DEFAULT_FILE_PATH = "reservations.txt";
    public static final String DEFAULT_RECEIPT_FILE = "PNR_receipt.txt";

    // Practicum 10: Dynamic ArrayList to store Ticket objects in memory
    private final ArrayList<Ticket> reservations;

    // File path for persistence
    private final String persistenceFilePath;

    /**
     * Default constructor: uses the standard 'reservations.txt' file.
     * Loads existing reservations from disk on startup.
     */
    public ReservationManager() {
        this(DEFAULT_FILE_PATH);
    }

    /**
     * Parameterized constructor: allows specifying custom persistence file path.
     * Useful for testing and custom storage configurations.
     *
     * @param persistenceFilePath Path to the reservations text file
     */
    public ReservationManager(String persistenceFilePath) {
        this.persistenceFilePath = persistenceFilePath;
        // Practicum 10: Initializing the dynamic ArrayList
        this.reservations = new ArrayList<>();

        // Practicum 12: Load existing saved reservations at startup
        try {
            loadReservations();
        } catch (IOException e) {
            System.err.println("Notice: Could not load initial reservations: " + e.getMessage());
        }
    }

    // =========================================================================
    // Practicum 10: ArrayList Operations (Booking, Search, Listing, Cancel)
    // =========================================================================

    /**
     * Books a new ticket and appends it to the in-memory ArrayList.
     * Prevents duplicate PNRs and saves changes to persistent storage.
     *
     * @param ticket Ticket instance to book
     * @return true if successfully booked
     * @throws IllegalArgumentException if ticket is null or PNR already exists
     * @throws IOException              if file persistence fails
     */
    public synchronized boolean bookTicket(Ticket ticket) throws IOException {
        if (ticket == null) {
            throw new IllegalArgumentException("Cannot book a null ticket.");
        }

        // Prevent duplicate PNRs
        if (existsPNR(ticket.getPnr())) {
            throw new IllegalArgumentException("Duplicate PNR detected: " + ticket.getPnr() + " is already booked.");
        }

        // Practicum 10: ArrayList add operation
        boolean added = reservations.add(ticket);

        if (added) {
            // Practicum 12: Auto-save on successful booking
            saveReservations();
        }

        return added;
    }

    /**
     * Cancels an existing ticket by its PNR.
     * Removes the ticket from the ArrayList and persists changes.
     *
     * @param pnr PNR of ticket to cancel
     * @return true if found and removed; false if not found
     * @throws IOException if file persistence fails
     */
    public synchronized boolean cancelTicket(String pnr) throws IOException {
        if (pnr == null || pnr.trim().isEmpty()) {
            return false;
        }

        Ticket ticketToRemove = findTicketByPNR(pnr.trim());
        if (ticketToRemove != null) {
            // Practicum 10: ArrayList remove operation
            boolean removed = reservations.remove(ticketToRemove);
            if (removed) {
                // Practicum 12: Auto-save after cancellation
                saveReservations();
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a ticket by its exact PNR (case-insensitive).
     *
     * @param pnr PNR identifier
     * @return Ticket object if found; null otherwise
     */
    public synchronized Ticket findTicketByPNR(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            return null;
        }

        String targetPnr = pnr.trim();
        // Practicum 10: Traversing the ArrayList using an enhanced for-loop
        for (Ticket ticket : reservations) {
            if (ticket.getPnr().equalsIgnoreCase(targetPnr)) {
                return ticket;
            }
        }
        return null;
    }

    /**
     * Checks whether a PNR already exists in memory.
     *
     * @param pnr PNR identifier
     * @return true if PNR exists, false otherwise
     */
    public synchronized boolean existsPNR(String pnr) {
        return findTicketByPNR(pnr) != null;
    }

    /**
     * Returns a copy of all current reservations.
     * Returning a new ArrayList preserves encapsulation so external callers
     * cannot corrupt internal manager state.
     *
     * @return ArrayList of active tickets
     */
    public synchronized ArrayList<Ticket> getAllTickets() {
        // Practicum 10: Creating a copy of the ArrayList
        return new ArrayList<>(reservations);
    }

    /**
     * Searches tickets by passenger name or train number (partial & case-insensitive match).
     *
     * @param query Search query string
     * @return ArrayList of matching tickets
     */
    public synchronized ArrayList<Ticket> searchTickets(String query) {
        ArrayList<Ticket> searchResults = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return searchResults;
        }

        String lowerQuery = query.trim().toLowerCase();
        // Practicum 10: Linear search and filtering with ArrayList
        for (Ticket ticket : reservations) {
            boolean matchesName = ticket.getPassengerName().toLowerCase().contains(lowerQuery);
            boolean matchesTrain = ticket.getTrainNumber().toLowerCase().contains(lowerQuery);
            boolean matchesPnr = ticket.getPnr().toLowerCase().contains(lowerQuery);

            if (matchesName || matchesTrain || matchesPnr) {
                searchResults.add(ticket);
            }
        }
        return searchResults;
    }

    /**
     * Returns current count of reservations stored in memory.
     *
     * @return total ticket count
     */
    public synchronized int getReservationCount() {
        return reservations.size();
    }

    // =========================================================================
    // Practicum 12: File Handling (Persistence & Receipts)
    // =========================================================================

    /**
     * Practicum 12: Loads reservations from the persistent text file into the ArrayList.
     * Uses BufferedReader with try-with-resources.
     * Handles missing file and malformed records safely without wiping existing data.
     *
     * @throws IOException if a serious I/O failure occurs
     */
    public synchronized void loadReservations() throws IOException {
        File file = new File(persistenceFilePath);
        if (!file.exists()) {
            // Fresh startup: persistent file does not exist yet; nothing to load
            return;
        }

        ArrayList<Ticket> loadedTickets = new ArrayList<>();

        // Practicum 12: Reading text file using BufferedReader inside try-with-resources
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                // Skip blank lines and comment lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    Ticket ticket = deserializeTicket(line);
                    if (ticket != null) {
                        // Avoid duplicates in memory
                        boolean duplicate = false;
                        for (Ticket existing : loadedTickets) {
                            if (existing.getPnr().equalsIgnoreCase(ticket.getPnr())) {
                                duplicate = true;
                                break;
                            }
                        }

                        if (!duplicate) {
                            loadedTickets.add(ticket);
                        } else {
                            System.err.println("Warning: Duplicate PNR " + ticket.getPnr() + " ignored on line " + lineNumber);
                        }
                    }
                } catch (Exception e) {
                    // Safe error handling: log and skip bad record without crashing or deleting file
                    System.err.println("Warning: Skipping malformed record on line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        // Populate in-memory ArrayList after reading
        reservations.clear();
        reservations.addAll(loadedTickets);
    }

    /**
     * Practicum 12: Saves all in-memory reservations to the text file.
     * Uses BufferedWriter with try-with-resources.
     * Employs a safe temporary-file write strategy so that existing data is never
     * corrupted or wiped if an I/O error occurs halfway through writing.
     *
     * @throws IOException if saving fails
     */
    public synchronized void saveReservations() throws IOException {
        File targetFile = new File(persistenceFilePath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Temporary file for atomic / safe replacement
        File tempFile = new File(targetFile.getAbsolutePath() + ".tmp");

        // Practicum 12: Writing to text file using BufferedWriter inside try-with-resources
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, StandardCharsets.UTF_8))) {
            // Header comments
            writer.write("# Train Reservation System Records");
            writer.newLine();
            writer.write("# Format: TYPE|PNR|PASSENGER_NAME|TRAIN_NUMBER|BASE_FARE|EXTRA_DATA");
            writer.newLine();

            for (Ticket ticket : reservations) {
                String serialized = serializeTicket(ticket);
                if (serialized != null) {
                    writer.write(serialized);
                    writer.newLine();
                }
            }
            writer.flush();
        } catch (IOException e) {
            // Clean up temporary file on failure
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw new IOException("Failed to save reservations to temporary file: " + e.getMessage(), e);
        }

        // Safely replace the target file with the validated temporary file
        try {
            Files.move(tempFile.toPath(), targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveException) {
            // Fallback for filesystems that do not support ATOMIC_MOVE
            Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Practicum 12: Generates a printable receipt text file for a ticket.
     * Writes to the standard 'PNR_receipt.txt' file.
     *
     * @param pnr PNR of the ticket
     * @return Generated File object
     * @throws IOException              if file creation or writing fails
     * @throws IllegalArgumentException if ticket is not found
     */
    public File generateReceipt(String pnr) throws IOException {
        return generateReceipt(pnr, DEFAULT_RECEIPT_FILE);
    }

    /**
     * Practicum 12: Generates a printable receipt text file for a specific ticket.
     *
     * @param pnr                 PNR of ticket
     * @param destinationFileName Name or path of receipt file
     * @return Generated File object
     * @throws IOException              if file writing fails
     * @throws IllegalArgumentException if ticket is not found
     */
    public File generateReceipt(String pnr, String destinationFileName) throws IOException {
        Ticket ticket = findTicketByPNR(pnr);
        if (ticket == null) {
            throw new IllegalArgumentException("Cannot generate receipt: Ticket with PNR '" + pnr + "' does not exist.");
        }
        return writeReceiptFile(ticket, destinationFileName);
    }

    /**
     * Internal helper method to format and write the printable ticket receipt.
     */
    private File writeReceiptFile(Ticket ticket, String destinationFileName) throws IOException {
        File receiptFile = new File(destinationFileName);
        File parentDir = receiptFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = LocalDateTime.now().format(dtf);

        // Practicum 12: Using BufferedWriter with try-with-resources to generate receipt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(receiptFile, StandardCharsets.UTF_8))) {
            writer.write("================================================================");
            writer.newLine();
            writer.write("                 COLLEGE RAILWAY RESERVATION SYSTEM             ");
            writer.newLine();
            writer.write("                     PASSENGER E-TICKET RECEIPT                 ");
            writer.newLine();
            writer.write("================================================================");
            writer.newLine();
            writer.write(String.format("PNR Number       : %s%n", ticket.getPnr()));
            writer.write(String.format("Passenger Name   : %s%n", ticket.getPassengerName()));
            writer.write(String.format("Train Number     : %s%n", ticket.getTrainNumber()));
            writer.write(String.format("Ticket Category  : %s%n", ticket.getTicketType()));

            // Class-specific details (Practicum 2 Inheritance & Polymorphism)
            if (ticket instanceof SleeperTicket) {
                SleeperTicket st = (SleeperTicket) ticket;
                writer.write(String.format("Allocated Berth  : %s%n", st.getBerthType()));
                writer.write(String.format("Sleeper Charge   : Rs. %.2f%n", st.getSleeperSurcharge()));
            } else if (ticket instanceof ACTierTicket) {
                ACTierTicket at = (ACTierTicket) ticket;
                writer.write(String.format("AC Class Tier    : Tier %d (%dA)%n", at.getAcTier(), at.getAcTier()));
                writer.write(String.format("Tier Surcharge   : Rs. %.2f%n", at.getTierSurcharge()));
                writer.write(String.format("GST Rate Applied : %.0f%%%n", at.getGstRate() * 100));
            } else if (ticket instanceof GeneralTicket) {
                GeneralTicket gt = (GeneralTicket) ticket;
                writer.write(String.format("Ticket Validity  : %d Hours%n", gt.getValidityHours()));
                writer.write(String.format("Service Fee      : Rs. %.2f%n", gt.getGeneralServiceCharge()));
            }

            writer.write("----------------------------------------------------------------");
            writer.newLine();
            writer.write(String.format("Base Fare        : Rs. %.2f%n", ticket.getBaseFare()));
            writer.write(String.format("TOTAL FARE       : Rs. %.2f%n", ticket.calculateTotalFare()));
            writer.write("----------------------------------------------------------------");
            writer.newLine();
            writer.write(String.format("Booking Status   : CONFIRMED%n"));
            writer.write(String.format("Issued On        : %s%n", formattedDateTime));
            writer.write("================================================================");
            writer.newLine();
            writer.write("        * ACADEMIC PROJECT DEMONSTRATION - NOT AN OFFICIAL TICKET *");
            writer.newLine();
            writer.write("                  WISH YOU A SAFE AND HAPPY JOURNEY!            ");
            writer.newLine();
            writer.write("================================================================");
            writer.newLine();
            writer.flush();
        }

        return receiptFile;
    }

    // =========================================================================
    // Delimiter-Safe Serialization & Deserialization
    // =========================================================================

    /**
     * Serializes a Ticket object into a single delimiter-safe line.
     * Escapes backslashes and pipe characters so that names with commas, quotes,
     * or pipes never break the record format.
     */
    private String serializeTicket(Ticket ticket) {
        if (ticket == null) return null;

        String type;
        String extra;

        if (ticket instanceof SleeperTicket) {
            type = "SLEEPER";
            extra = ((SleeperTicket) ticket).getBerthType();
        } else if (ticket instanceof ACTierTicket) {
            type = "AC";
            extra = String.valueOf(((ACTierTicket) ticket).getAcTier());
        } else if (ticket instanceof GeneralTicket) {
            type = "GENERAL";
            extra = String.valueOf(((GeneralTicket) ticket).getValidityHours());
        } else {
            return null;
        }

        return escape(type) + "|"
                + escape(ticket.getPnr()) + "|"
                + escape(ticket.getPassengerName()) + "|"
                + escape(ticket.getTrainNumber()) + "|"
                + escape(String.valueOf(ticket.getBaseFare())) + "|"
                + escape(extra);
    }

    /**
     * Deserializes a text line into the corresponding Ticket subclass instance.
     * Preserves subtype and fare calculation behavior.
     */
    private Ticket deserializeTicket(String line) {
        List<String> fields = splitFields(line);
        if (fields.size() < 6) {
            throw new IllegalArgumentException("Incomplete record: Expected 6 fields, found " + fields.size());
        }

        String type = fields.get(0).trim().toUpperCase();
        String pnr = fields.get(1).trim();
        String passengerName = fields.get(2).trim();
        String trainNumber = fields.get(3).trim();
        double baseFare = Double.parseDouble(fields.get(4).trim());
        String extra = fields.get(5).trim();

        // Practicum 2: Recreating concrete subclass based on stored type tag
        switch (type) {
            case "SLEEPER":
                return new SleeperTicket(pnr, passengerName, trainNumber, baseFare, extra);
            case "AC":
                int tier = Integer.parseInt(extra);
                return new ACTierTicket(pnr, passengerName, trainNumber, baseFare, tier);
            case "GENERAL":
                int validityHours = Integer.parseInt(extra);
                return new GeneralTicket(pnr, passengerName, trainNumber, baseFare, validityHours);
            default:
                throw new IllegalArgumentException("Unknown ticket subtype: " + type);
        }
    }

    /**
     * Escapes backslashes and pipes to preserve delimiters safely.
     */
    private static String escape(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("|", "\\|")
                    .replace("\r", "")
                    .replace("\n", " ");
    }

    /**
     * Splits a line by unescaped pipe '|' characters.
     */
    private static List<String> splitFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escaped) {
                current.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '|') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    public String getPersistenceFilePath() {
        return persistenceFilePath;
    }
}
