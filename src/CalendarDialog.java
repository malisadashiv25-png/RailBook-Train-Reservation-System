import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * CalendarDialog
 * --------------
 * Modern, interactive Java Swing modal dialog for selecting train travel dates.
 * Built using standard Swing components and Java Time API (LocalDate, YearMonth).
 *
 * Features:
 * - Month & Year navigation with Previous and Next controls.
 * - 7 Weekday columns (Sun to Sat).
 * - Exact day placement and automatic leap-year handling (e.g. Feb 2028 = 29 days).
 * - Disables and visually grays out all past dates (date < LocalDate.now()).
 * - Distinct highlight for today's date and the selected departure date.
 * - Quick "Today" shortcut button and "Cancel" action.
 * - Manual date input with validation preventing past/malformed dates.
 * - Thread-safe and compatible with Java 21.
 */
public class CalendarDialog extends JDialog {

    // Color Palette matching RailBook Design
    private static final Color COLOR_NAVY_HEADER = new Color(26, 43, 73);
    private static final Color COLOR_PRIMARY_BLUE = new Color(0, 102, 204);
    private static final Color COLOR_BLUE_HOVER = new Color(0, 82, 163);
    private static final Color COLOR_CYAN_ACCENT = new Color(0, 180, 216);
    private static final Color COLOR_SUCCESS_GREEN = new Color(16, 185, 129);
    private static final Color COLOR_DISABLED_TEXT = new Color(160, 174, 192);
    private static final Color COLOR_DISABLED_BG = new Color(241, 245, 249);
    private static final Color COLOR_GRID_BG = Color.WHITE;
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);

    private LocalDate selectedDate;
    private YearMonth currentMonth;
    private boolean confirmed = false;

    // UI Components
    private JLabel lblMonthYear;
    private JPanel daysGridPanel;
    private JButton btnPrevMonth;
    private JButton btnNextMonth;
    private JLabel lblSelectedPreview;
    private JTextField txtManualInput;
    private JLabel lblValidationError;

    public CalendarDialog(Window owner, LocalDate initialDate) {
        super(owner, "Select Departure Date — RailBook", ModalityType.APPLICATION_MODAL);
        LocalDate today = LocalDate.now();
        this.selectedDate = (initialDate != null && !initialDate.isBefore(today)) ? initialDate : today;
        this.currentMonth = YearMonth.from(this.selectedDate);

        initComponents();
        updateCalendarGrid();
        updateSelectionPreview();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        if (owner != null) {
            setLocationRelativeTo(owner);
        } else {
            setLocationRelativeTo(null);
        }
    }

    public CalendarDialog(Frame owner, LocalDate initialDate) {
        this((Window) owner, initialDate);
    }

    /**
     * Static helper to prompt the user for a travel date.
     * Returns the selected LocalDate if confirmed, or null if cancelled.
     */
    public static LocalDate showDatePicker(Window owner, LocalDate initialDate) {
        CalendarDialog dialog = new CalendarDialog(owner, initialDate);
        dialog.setVisible(true);
        return dialog.isConfirmed() ? dialog.getSelectedDate() : null;
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 12));
        contentPane.setBackground(new Color(248, 250, 252));
        contentPane.setBorder(new EmptyBorder(16, 20, 16, 20));

        // --- Top Header: Month & Year with Navigation ---
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(COLOR_NAVY_HEADER);
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        btnPrevMonth = createNavButton("◀", "Previous Month");
        btnPrevMonth.addActionListener(e -> prevMonth());

        btnNextMonth = createNavButton("▶", "Next Month");
        btnNextMonth.addActionListener(e -> nextMonth());

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblMonthYear.setForeground(Color.WHITE);

        headerPanel.add(btnPrevMonth, BorderLayout.WEST);
        headerPanel.add(lblMonthYear, BorderLayout.CENTER);
        headerPanel.add(btnNextMonth, BorderLayout.EAST);

        contentPane.add(headerPanel, BorderLayout.NORTH);

        // --- Center: Weekday Headers + Days Grid ---
        JPanel calendarBody = new JPanel(new BorderLayout(0, 6));
        calendarBody.setOpaque(false);

        // Weekday Columns (Sun to Sat)
        JPanel weekdayPanel = new JPanel(new GridLayout(1, 7, 4, 4));
        weekdayPanel.setOpaque(false);
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel lblDay = new JLabel(day, SwingConstants.CENTER);
            lblDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if ("Sun".equals(day)) {
                lblDay.setForeground(new Color(239, 68, 68)); // Red accent for Sunday
            } else {
                lblDay.setForeground(new Color(71, 85, 105));
            }
            lblDay.setPreferredSize(new Dimension(46, 26));
            weekdayPanel.add(lblDay);
        }
        calendarBody.add(weekdayPanel, BorderLayout.NORTH);

        // 6 Rows x 7 Columns Day Grid
        daysGridPanel = new JPanel(new GridLayout(6, 7, 4, 4));
        daysGridPanel.setBackground(COLOR_GRID_BG);
        daysGridPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 6, 6, 6)
        ));
        calendarBody.add(daysGridPanel, BorderLayout.CENTER);

        contentPane.add(calendarBody, BorderLayout.CENTER);

        // --- Bottom Area: Preview, Manual Date Input & Action Buttons ---
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setOpaque(false);

        // Selected Date Preview Card
        JPanel previewCard = new JPanel(new BorderLayout(8, 0));
        previewCard.setOpaque(false);
        previewCard.setBorder(new EmptyBorder(6, 4, 6, 4));

        lblSelectedPreview = new JLabel("Selected: --");
        lblSelectedPreview.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSelectedPreview.setForeground(COLOR_PRIMARY_BLUE);
        previewCard.add(lblSelectedPreview, BorderLayout.CENTER);

        bottomContainer.add(previewCard);
        bottomContainer.add(Box.createVerticalStrut(4));

        // Manual Entry Box with Validation
        JPanel manualPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        manualPanel.setOpaque(false);

        JLabel lblManual = new JLabel("Manual (YYYY-MM-DD):");
        lblManual.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblManual.setForeground(COLOR_TEXT_DARK);

        txtManualInput = new JTextField(10);
        txtManualInput.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtManualInput.setText(selectedDate.toString());

        JButton btnApplyManual = new JButton("Apply");
        btnApplyManual.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnApplyManual.setBackground(new Color(241, 245, 249));
        btnApplyManual.setForeground(COLOR_TEXT_DARK);
        btnApplyManual.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApplyManual.addActionListener(e -> applyManualDate(txtManualInput.getText().trim()));
        txtManualInput.addActionListener(e -> applyManualDate(txtManualInput.getText().trim()));

        manualPanel.add(lblManual);
        manualPanel.add(txtManualInput);
        manualPanel.add(btnApplyManual);

        bottomContainer.add(manualPanel);

        // Validation Error Label
        lblValidationError = new JLabel(" ");
        lblValidationError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblValidationError.setForeground(new Color(220, 38, 38));
        lblValidationError.setBorder(new EmptyBorder(2, 6, 4, 6));
        bottomContainer.add(lblValidationError);

        // Action Buttons Row (Today, Cancel, Confirm)
        JPanel actionRow = new JPanel(new BorderLayout(10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton btnToday = new JButton("Today");
        btnToday.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToday.setForeground(COLOR_PRIMARY_BLUE);
        btnToday.setBackground(Color.WHITE);
        btnToday.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToday.setFocusPainted(false);
        btnToday.addActionListener(e -> jumpToToday());

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setForeground(new Color(100, 116, 139));
        btnCancel.setBackground(new Color(241, 245, 249));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        JButton btnConfirm = new JButton("Select Date");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setBackground(COLOR_PRIMARY_BLUE);
        btnConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirm.setFocusPainted(false);
        btnConfirm.addActionListener(e -> confirmAndClose());

        rightActions.add(btnCancel);
        rightActions.add(btnConfirm);

        actionRow.add(btnToday, BorderLayout.WEST);
        actionRow.add(rightActions, BorderLayout.EAST);

        bottomContainer.add(actionRow);

        contentPane.add(bottomContainer, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private JButton createNavButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(40, 65, 105));
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(4, 12, 4, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Re-renders the 6x7 day grid for the active year and month.
     */
    public void updateCalendarGrid() {
        daysGridPanel.removeAll();

        lblMonthYear.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        YearMonth thisMonth = YearMonth.now();
        btnPrevMonth.setEnabled(!currentMonth.isBefore(thisMonth));

        // Start column for day 1 (Sunday = 0, Monday = 1, ..., Saturday = 6)
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int startCol = firstOfMonth.getDayOfWeek().getValue() % 7;
        int totalDays = currentMonth.lengthOfMonth();

        LocalDate today = LocalDate.now();

        // Leading empty slots
        for (int i = 0; i < startCol; i++) {
            JLabel blank = new JLabel("");
            blank.setPreferredSize(new Dimension(46, 36));
            daysGridPanel.add(blank);
        }

        // Days in active month
        for (int day = 1; day <= totalDays; day++) {
            LocalDate date = currentMonth.atDay(day);
            JButton btnDay = new JButton(String.valueOf(day));
            btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnDay.setMargin(new Insets(2, 2, 2, 2));
            btnDay.setPreferredSize(new Dimension(46, 36));
            btnDay.setFocusPainted(false);

            boolean isPast = date.isBefore(today);
            boolean isSelected = date.equals(selectedDate);
            boolean isToday = date.equals(today);

            if (isPast) {
                btnDay.setEnabled(false);
                btnDay.setForeground(COLOR_DISABLED_TEXT);
                btnDay.setBackground(COLOR_DISABLED_BG);
                btnDay.setBorder(new LineBorder(new Color(241, 245, 249), 1));
            } else {
                btnDay.setEnabled(true);
                btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));

                if (isSelected) {
                    btnDay.setBackground(COLOR_PRIMARY_BLUE);
                    btnDay.setForeground(Color.WHITE);
                    btnDay.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    btnDay.setBorder(new LineBorder(COLOR_BLUE_HOVER, 2, true));
                } else if (isToday) {
                    btnDay.setBackground(new Color(230, 247, 255));
                    btnDay.setForeground(COLOR_PRIMARY_BLUE);
                    btnDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    btnDay.setBorder(new LineBorder(COLOR_CYAN_ACCENT, 2, true));
                    btnDay.setToolTipText("Today");
                } else {
                    btnDay.setBackground(Color.WHITE);
                    btnDay.setForeground(COLOR_TEXT_DARK);
                    btnDay.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
                }

                // Click to select, double-click to confirm
                btnDay.addActionListener(e -> {
                    selectDate(date);
                });

                btnDay.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2 && btnDay.isEnabled()) {
                            selectDate(date);
                            confirmAndClose();
                        }
                    }
                });
            }

            daysGridPanel.add(btnDay);
        }

        // Trailing empty slots to fill 42 cells (6 rows x 7 cols)
        int occupied = startCol + totalDays;
        int remaining = 42 - occupied;
        for (int i = 0; i < remaining; i++) {
            JLabel blank = new JLabel("");
            blank.setPreferredSize(new Dimension(46, 36));
            daysGridPanel.add(blank);
        }

        daysGridPanel.revalidate();
        daysGridPanel.repaint();
    }

    public void prevMonth() {
        YearMonth prev = currentMonth.minusMonths(1);
        if (!prev.isBefore(YearMonth.now())) {
            currentMonth = prev;
            updateCalendarGrid();
        }
    }

    public void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        updateCalendarGrid();
    }

    public void jumpToToday() {
        LocalDate today = LocalDate.now();
        this.selectedDate = today;
        this.currentMonth = YearMonth.from(today);
        clearError();
        updateCalendarGrid();
        updateSelectionPreview();
        txtManualInput.setText(today.toString());
    }

    public boolean selectDate(LocalDate date) {
        if (date == null) return false;
        if (date.isBefore(LocalDate.now())) {
            showError("Date cannot be in the past (" + date + ").");
            return false;
        }
        this.selectedDate = date;
        this.currentMonth = YearMonth.from(date);
        clearError();
        updateCalendarGrid();
        updateSelectionPreview();
        txtManualInput.setText(date.toString());
        return true;
    }

    /**
     * Validates and applies manually typed date strings.
     * Prevents past dates and malformed syntax.
     */
    public boolean applyManualDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            showError("Please enter a valid date.");
            return false;
        }

        String clean = text.trim();
        LocalDate parsed = null;

        // Try YYYY-MM-DD
        try {
            parsed = LocalDate.parse(clean, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
            // Try DD-MM-YYYY
            try {
                parsed = LocalDate.parse(clean, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (DateTimeParseException ignored2) {
                // Try DD/MM/YYYY
                try {
                    parsed = LocalDate.parse(clean, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (DateTimeParseException ignored3) {
                    showError("Invalid format. Use YYYY-MM-DD (e.g. 2026-10-25).");
                    return false;
                }
            }
        }

        if (parsed.isBefore(LocalDate.now())) {
            showError("Departure date cannot be in the past (" + parsed + ").");
            return false;
        }

        selectDate(parsed);
        return true;
    }

    private void confirmAndClose() {
        if (selectedDate == null || selectedDate.isBefore(LocalDate.now())) {
            showError("Please select a valid upcoming travel date.");
            return;
        }
        this.confirmed = true;
        dispose();
    }

    private void updateSelectionPreview() {
        if (selectedDate != null) {
            String formatted = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy (EEEE)"));
            lblSelectedPreview.setText("Selected Date: " + formatted);
        }
    }

    private void showError(String msg) {
        lblValidationError.setText(msg);
    }

    private void clearError() {
        lblValidationError.setText(" ");
    }

    // --- Getters & Public Helpers ---

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public YearMonth getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(YearMonth ym) {
        if (ym != null) {
            this.currentMonth = ym;
            updateCalendarGrid();
        }
    }

    public static boolean isLeapYear(int year) {
        return Year.isLeap(year);
    }

    public static int getDaysInMonth(int year, int month) {
        return YearMonth.of(year, month).lengthOfMonth();
    }
}
