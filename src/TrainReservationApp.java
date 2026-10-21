import model.ACTierTicket;
import model.GeneralTicket;
import model.SleeperTicket;
import model.Ticket;
import model.User;
import service.ReservationManager;
import service.UserManager;
import service.UserSession;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * RailBook — Train Reservation System
 * =====================================
 * Premium Modern Desktop GUI Application built using standard Java Swing, AWT, & Java2D.
 * Compiled with Java 21 compatibility (--release 21).
 *
 * Demonstrates 3 College Practicum Experiments:
 * - Practicum 2: Inheritance Concepts (Ticket, SleeperTicket, ACTierTicket, GeneralTicket)
 * - Practicum 10: ArrayList (Dynamic in-memory storage, linear search, and cancellation)
 * - Practicum 12: File Handling (Persistent storage in reservations.txt and users.txt)
 *
 * Features (Stage 1 & 2):
 * - User Authentication: Salted SHA-256 password hashing, user registration, login, and guest mode.
 * - ConfirmTkt-inspired modern palette (Navy, Royal Blue, Cyan, White).
 * - Non-blocking javax.swing.Timer animations (smooth panel transitions, card slide-in,
 *   animated checkmark, welcome fade-in, and button hover states).
 */
public class TrainReservationApp extends JFrame {

    // --- Color Palette (ConfirmTkt inspired) ---
    public static final Color COLOR_NAVY_DARK = new Color(15, 23, 42);      // Slate 900
    public static final Color COLOR_NAVY_SIDEBAR = new Color(26, 43, 73);   // Midnight Navy
    public static final Color COLOR_SIDEBAR_HOVER = new Color(38, 60, 98);
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(0, 102, 204); // Royal Blue
    public static final Color COLOR_CANVAS = new Color(245, 247, 250);      // Light Grey Canvas
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_CARD_BORDER = new Color(226, 232, 240);
    public static final Color COLOR_PRIMARY_BLUE = new Color(0, 102, 204);
    public static final Color COLOR_PRIMARY_HOVER = new Color(0, 82, 163);
    public static final Color COLOR_CYAN_ACCENT = new Color(0, 180, 216);
    public static final Color COLOR_SUCCESS_GREEN = new Color(16, 185, 129);
    public static final Color COLOR_DANGER_RED = new Color(239, 68, 68);
    public static final Color COLOR_TEXT_MAIN = new Color(30, 41, 59);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    // Business Logic & Storage Services
    private final ReservationManager manager;
    private final UserManager userManager;

    // GUI Layout Components
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private SidebarButton activeButton;

    // Smooth Panel Transition Animation (non-blocking Timer)
    private float transitionAlpha = 1.0f;
    private Timer transitionTimer;

    // Sidebar Profile Labels
    private JLabel lblSidebarUserName;
    private JLabel lblSidebarUserRole;

    // View Panels
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private NewBookingPanel newBookingPanel;
    private MyBookingsPanel myBookingsPanel;
    private ManageTicketsPanel manageTicketsPanel;
    private TicketReceiptPanel ticketReceiptPanel;

    // Sidebar navigation buttons
    private SidebarButton btnNavLogin;
    private SidebarButton btnNavDashboard;
    private SidebarButton btnNavNewBooking;
    private SidebarButton btnNavMyBookings;
    private SidebarButton btnNavManage;
    private SidebarButton btnNavReceipt;

    public TrainReservationApp() {
        super("RailBook — Train Reservation System");

        // Initialize business logic layer (Practicum 10 ArrayList & Practicum 12 File Handling)
        this.manager = new ReservationManager();
        this.userManager = new UserManager();

        // Initialize guest session by default for immediate exploration
        UserSession.getInstance().loginAsGuest();

        initComponents();
        setupWindow();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Left Navigation Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Center Content Area with CardLayout & smooth alpha transition
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout) {
            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transitionAlpha));
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        mainContentPanel.setBackground(COLOR_CANVAS);

        // Instantiate view sections
        loginPanel = new LoginPanel(this);
        dashboardPanel = new DashboardPanel(this);
        newBookingPanel = new NewBookingPanel(this);
        myBookingsPanel = new MyBookingsPanel(this);
        manageTicketsPanel = new ManageTicketsPanel(this);
        ticketReceiptPanel = new TicketReceiptPanel(this);

        // Add sections to CardLayout
        mainContentPanel.add(loginPanel, "LOGIN");
        mainContentPanel.add(dashboardPanel, "DASHBOARD");
        mainContentPanel.add(newBookingPanel, "NEW_BOOKING");
        mainContentPanel.add(myBookingsPanel, "MY_BOOKINGS");
        mainContentPanel.add(manageTicketsPanel, "MANAGE_TICKETS");
        mainContentPanel.add(ticketReceiptPanel, "TICKET_RECEIPT");

        add(mainContentPanel, BorderLayout.CENTER);

        // Start on Login screen or Dashboard
        navigateTo("LOGIN", btnNavLogin);
    }

    private void setupWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null); // Center on screen
    }

    /**
     * Creates modern deep-navy navigation sidebar.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 760));
        sidebar.setBackground(COLOR_NAVY_SIDEBAR);

        // --- Top Header / Branding & User Profile ---
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(20, 20, 15, 20));

        JLabel lblLogo = new JLabel("RailBook");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Train Reservation System");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(COLOR_CYAN_ACCENT);

        JLabel lblBadge = new JLabel("College Practicum Project");
        lblBadge.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblBadge.setForeground(new Color(148, 163, 184));

        brandPanel.add(lblLogo);
        brandPanel.add(Box.createVerticalStrut(3));
        brandPanel.add(lblSub);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(lblBadge);

        // Profile Card in Sidebar
        JPanel profileCard = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 23, 42, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        profileCard.setOpaque(false);
        profileCard.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel profileText = new JPanel(new GridLayout(2, 1, 0, 2));
        profileText.setOpaque(false);

        lblSidebarUserName = new JLabel("Guest Passenger");
        lblSidebarUserName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSidebarUserName.setForeground(Color.WHITE);

        lblSidebarUserRole = new JLabel("Guest Mode");
        lblSidebarUserRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSidebarUserRole.setForeground(COLOR_CYAN_ACCENT);

        profileText.add(lblSidebarUserName);
        profileText.add(lblSidebarUserRole);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnLogout.setForeground(new Color(239, 68, 68));
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());

        profileCard.add(profileText, BorderLayout.CENTER);
        profileCard.add(btnLogout, BorderLayout.EAST);

        brandPanel.add(Box.createVerticalStrut(12));
        brandPanel.add(profileCard);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // --- Navigation Buttons List ---
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(10, 12, 10, 12));

        btnNavLogin = new SidebarButton(" Account / Login", "LOGIN");
        btnNavDashboard = new SidebarButton(" Dashboard", "DASHBOARD");
        btnNavNewBooking = new SidebarButton(" New Reservation", "NEW_BOOKING");
        btnNavMyBookings = new SidebarButton(" My Bookings", "MY_BOOKINGS");
        btnNavManage = new SidebarButton(" Manage Tickets", "MANAGE_TICKETS");
        btnNavReceipt = new SidebarButton(" Ticket Receipt", "TICKET_RECEIPT");

        btnNavLogin.addActionListener(e -> navigateTo("LOGIN", btnNavLogin));
        btnNavDashboard.addActionListener(e -> navigateTo("DASHBOARD", btnNavDashboard));
        btnNavNewBooking.addActionListener(e -> navigateTo("NEW_BOOKING", btnNavNewBooking));
        btnNavMyBookings.addActionListener(e -> navigateTo("MY_BOOKINGS", btnNavMyBookings));
        btnNavManage.addActionListener(e -> navigateTo("MANAGE_TICKETS", btnNavManage));
        btnNavReceipt.addActionListener(e -> navigateTo("TICKET_RECEIPT", btnNavReceipt));

        navList.add(btnNavLogin);
        navList.add(Box.createVerticalStrut(8));
        navList.add(btnNavDashboard);
        navList.add(Box.createVerticalStrut(8));
        navList.add(btnNavNewBooking);
        navList.add(Box.createVerticalStrut(8));
        navList.add(btnNavMyBookings);
        navList.add(Box.createVerticalStrut(8));
        navList.add(btnNavManage);
        navList.add(Box.createVerticalStrut(8));
        navList.add(btnNavReceipt);

        sidebar.add(navList, BorderLayout.CENTER);

        // --- Bottom Practicum Info Card ---
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBackground(new Color(15, 23, 42));
        footerPanel.setBorder(new EmptyBorder(15, 16, 15, 16));

        JLabel lblExp1 = new JLabel("• Practicum 2: Inheritance");
        JLabel lblExp2 = new JLabel("• Practicum 10: ArrayList");
        JLabel lblExp3 = new JLabel("• Practicum 12: File I/O");

        Font fontExp = new Font("Segoe UI", Font.PLAIN, 11);
        Color colExp = new Color(148, 163, 184);

        lblExp1.setFont(fontExp);
        lblExp1.setForeground(colExp);
        lblExp2.setFont(fontExp);
        lblExp2.setForeground(colExp);
        lblExp3.setFont(fontExp);
        lblExp3.setForeground(colExp);

        JLabel lblPersist = new JLabel("Files: reservations.txt, users.txt");
        lblPersist.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPersist.setForeground(COLOR_CYAN_ACCENT);

        footerPanel.add(lblExp1);
        footerPanel.add(Box.createVerticalStrut(3));
        footerPanel.add(lblExp2);
        footerPanel.add(Box.createVerticalStrut(3));
        footerPanel.add(lblExp3);
        footerPanel.add(Box.createVerticalStrut(8));
        footerPanel.add(lblPersist);

        sidebar.add(footerPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    /**
     * Switches card view with smooth non-blocking alpha transition.
     */
    public void navigateTo(String cardName, SidebarButton button) {
        if (activeButton != null) {
            activeButton.setActive(false);
        }
        if (button != null) {
            button.setActive(true);
            activeButton = button;
        }

        cardLayout.show(mainContentPanel, cardName);

        // Refresh views when navigating
        if ("DASHBOARD".equals(cardName)) {
            dashboardPanel.refreshData();
        } else if ("MY_BOOKINGS".equals(cardName)) {
            myBookingsPanel.refreshTable();
        } else if ("MANAGE_TICKETS".equals(cardName)) {
            manageTicketsPanel.refreshTable();
        } else if ("TICKET_RECEIPT".equals(cardName)) {
            ticketReceiptPanel.populatePnrSelector();
        } else if ("NEW_BOOKING".equals(cardName)) {
            newBookingPanel.onNavigatedTo();
        }

        // Trigger smooth panel fade transition (15ms timer, 6-8 steps)
        if (transitionTimer != null && transitionTimer.isRunning()) {
            transitionTimer.stop();
        }
        transitionAlpha = 0.2f;
        transitionTimer = new Timer(15, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transitionAlpha += 0.16f;
                if (transitionAlpha >= 1.0f) {
                    transitionAlpha = 1.0f;
                    transitionTimer.stop();
                }
                mainContentPanel.repaint();
            }
        });
        transitionTimer.start();
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of RailBook?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            UserSession.getInstance().logout();
            updateUserProfileBadge();
            navigateTo("LOGIN", btnNavLogin);
        }
    }

    public void onUserLoggedIn() {
        updateUserProfileBadge();
    }

    public void updateUserProfileBadge() {
        if (UserSession.getInstance().isAuthenticated()) {
            lblSidebarUserName.setText(UserSession.getInstance().getDisplayName());
            lblSidebarUserRole.setText(UserSession.getInstance().isGuest() ? "Guest Mode" : "Verified User");
        } else {
            lblSidebarUserName.setText("Not Signed In");
            lblSidebarUserRole.setText("Sign In to Book");
        }
    }

    public ReservationManager getManager() {
        return manager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void showReceiptForPNR(String pnr) {
        ticketReceiptPanel.loadTicketByPNR(pnr);
        navigateTo("TICKET_RECEIPT", btnNavReceipt);
    }

    public static JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_MAIN);
        return lbl;
    }

    // =========================================================================
    // SECTION 0: LOGIN & REGISTRATION PANEL (Feature 1)
    // =========================================================================

    public static class LoginPanel extends JPanel {
        private final TrainReservationApp app;
        private final CardLayout authCardLayout;
        private final JPanel authCardContainer;

        // Sign In fields
        private final JTextField txtSignInUsername;
        private final JPasswordField txtSignInPassword;
        private final JToggleButton btnToggleSignInPass;

        // Register fields
        private final JTextField txtRegFullName;
        private final JTextField txtRegUsername;
        private final JTextField txtRegEmail;
        private final JPasswordField txtRegPassword;
        private final JPasswordField txtRegConfirmPassword;
        private final JToggleButton btnToggleRegPass;

        // Tab selection buttons
        private final JButton btnTabSignIn;
        private final JButton btnTabRegister;

        public LoginPanel(TrainReservationApp app) {
            this.app = app;
            setLayout(new GridBagLayout());
            setBackground(COLOR_CANVAS);

            CardPanel mainCard = new CardPanel();
            mainCard.setLayout(new BorderLayout(0, 16));
            mainCard.setPreferredSize(new Dimension(480, 560));
            mainCard.setBorder(new EmptyBorder(25, 30, 25, 30));

            // Card Header
            JPanel headerPanel = new JPanel(new GridLayout(3, 1, 0, 4));
            headerPanel.setOpaque(false);

            JLabel lblBrand = new JLabel("RailBook Portal", SwingConstants.CENTER);
            lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblBrand.setForeground(COLOR_PRIMARY_BLUE);

            JLabel lblTitle = new JLabel("Passenger Sign In & Registration", SwingConstants.CENTER);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblTitle.setForeground(COLOR_TEXT_MAIN);

            JLabel lblSubtitle = new JLabel("Feature 1: Secure Salted SHA-256 Authentication & Guest Mode", SwingConstants.CENTER);
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblSubtitle.setForeground(COLOR_TEXT_MUTED);

            headerPanel.add(lblBrand);
            headerPanel.add(lblTitle);
            headerPanel.add(lblSubtitle);
            mainCard.add(headerPanel, BorderLayout.NORTH);

            // Tab Buttons: [Sign In] | [Create Account]
            JPanel tabSelectorPanel = new JPanel(new GridLayout(1, 2, 8, 0));
            tabSelectorPanel.setOpaque(false);

            btnTabSignIn = new JButton("Sign In");
            btnTabRegister = new JButton("Create Account");

            styleAuthTabButton(btnTabSignIn, true);
            styleAuthTabButton(btnTabRegister, false);

            authCardLayout = new CardLayout();
            authCardContainer = new JPanel(authCardLayout);
            authCardContainer.setOpaque(false);

            btnTabSignIn.addActionListener(e -> {
                authCardLayout.show(authCardContainer, "SIGN_IN");
                styleAuthTabButton(btnTabSignIn, true);
                styleAuthTabButton(btnTabRegister, false);
            });

            btnTabRegister.addActionListener(e -> {
                authCardLayout.show(authCardContainer, "REGISTER");
                styleAuthTabButton(btnTabSignIn, false);
                styleAuthTabButton(btnTabRegister, true);
            });

            tabSelectorPanel.add(btnTabSignIn);
            tabSelectorPanel.add(btnTabRegister);

            // --- 1. SIGN IN VIEW ---
            JPanel signInPanel = new JPanel(new GridBagLayout());
            signInPanel.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 4, 6, 4);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            int r = 0;
            gbc.gridx = 0; gbc.gridy = r++;
            signInPanel.add(createFieldLabel("Username or Email"), gbc);

            gbc.gridy = r++;
            txtSignInUsername = new JTextField("demo");
            txtSignInUsername.setPreferredSize(new Dimension(200, 36));
            txtSignInUsername.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(2, 8, 2, 8)));
            signInPanel.add(txtSignInUsername, gbc);

            gbc.gridy = r++;
            signInPanel.add(createFieldLabel("Password"), gbc);

            gbc.gridy = r++;
            JPanel passRow = new JPanel(new BorderLayout(6, 0));
            passRow.setOpaque(false);
            txtSignInPassword = new JPasswordField("demo123");
            txtSignInPassword.setPreferredSize(new Dimension(200, 36));
            txtSignInPassword.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(2, 8, 2, 8)));

            btnToggleSignInPass = new JToggleButton("Show");
            btnToggleSignInPass.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnToggleSignInPass.setPreferredSize(new Dimension(65, 36));
            btnToggleSignInPass.setFocusPainted(false);
            btnToggleSignInPass.addActionListener(e -> {
                if (btnToggleSignInPass.isSelected()) {
                    txtSignInPassword.setEchoChar((char) 0);
                    btnToggleSignInPass.setText("Hide");
                } else {
                    txtSignInPassword.setEchoChar('•');
                    btnToggleSignInPass.setText("Show");
                }
            });
            passRow.add(txtSignInPassword, BorderLayout.CENTER);
            passRow.add(btnToggleSignInPass, BorderLayout.EAST);
            signInPanel.add(passRow, gbc);

            gbc.gridy = r++;
            gbc.insets = new Insets(16, 4, 6, 4);
            ModernButton btnDoSignIn = new ModernButton("Sign In to RailBook", COLOR_PRIMARY_BLUE);
            btnDoSignIn.setPreferredSize(new Dimension(200, 38));
            btnDoSignIn.addActionListener(e -> handleSignIn());
            signInPanel.add(btnDoSignIn, gbc);

            gbc.gridy = r++;
            gbc.insets = new Insets(6, 4, 6, 4);
            ModernButton btnGuest = new ModernButton("Continue as Guest (Demo Mode)", new Color(71, 85, 105));
            btnGuest.setPreferredSize(new Dimension(200, 36));
            btnGuest.addActionListener(e -> handleGuestMode());
            signInPanel.add(btnGuest, gbc);

            gbc.gridy = r++;
            JLabel lblDemoTip = new JLabel("<html><center>Default Demo Account: <b>demo</b> / <b>demo123</b></center></html>", SwingConstants.CENTER);
            lblDemoTip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblDemoTip.setForeground(COLOR_TEXT_MUTED);
            signInPanel.add(lblDemoTip, gbc);

            // --- 2. REGISTER VIEW ---
            JPanel regPanel = new JPanel(new GridBagLayout());
            regPanel.setOpaque(false);
            GridBagConstraints gbcR = new GridBagConstraints();
            gbcR.insets = new Insets(4, 4, 4, 4);
            gbcR.fill = GridBagConstraints.HORIZONTAL;
            gbcR.weightx = 1.0;

            int regR = 0;
            gbcR.gridx = 0; gbcR.gridy = regR++;
            regPanel.add(createFieldLabel("Full Legal Name *"), gbcR);

            gbcR.gridy = regR++;
            txtRegFullName = new JTextField();
            txtRegFullName.setPreferredSize(new Dimension(200, 32));
            txtRegFullName.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(2, 8, 2, 8)));
            regPanel.add(txtRegFullName, gbcR);

            gbcR.gridy = regR++;
            regPanel.add(createFieldLabel("Username (Min 3 chars) *"), gbcR);

            gbcR.gridy = regR++;
            txtRegUsername = new JTextField();
            txtRegUsername.setPreferredSize(new Dimension(200, 32));
            txtRegUsername.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(2, 8, 2, 8)));
            regPanel.add(txtRegUsername, gbcR);

            gbcR.gridy = regR++;
            regPanel.add(createFieldLabel("Email Address *"), gbcR);

            gbcR.gridy = regR++;
            txtRegEmail = new JTextField();
            txtRegEmail.setPreferredSize(new Dimension(200, 32));
            txtRegEmail.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(2, 8, 2, 8)));
            regPanel.add(txtRegEmail, gbcR);

            gbcR.gridy = regR++;
            regPanel.add(createFieldLabel("Password (Min 6 chars) *"), gbcR);

            gbcR.gridy = regR++;
            JPanel regPassRow = new JPanel(new BorderLayout(6, 0));
            regPassRow.setOpaque(false);
            txtRegPassword = new JPasswordField();
            txtRegPassword.setPreferredSize(new Dimension(200, 32));
            txtRegPassword.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(2, 8, 2, 8)));

            txtRegConfirmPassword = new JPasswordField();
            txtRegConfirmPassword.setPreferredSize(new Dimension(200, 32));
            txtRegConfirmPassword.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(2, 8, 2, 8)));

            btnToggleRegPass = new JToggleButton("Show");
            btnToggleRegPass.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnToggleRegPass.setPreferredSize(new Dimension(65, 32));
            btnToggleRegPass.setFocusPainted(false);
            btnToggleRegPass.addActionListener(e -> {
                if (btnToggleRegPass.isSelected()) {
                    txtRegPassword.setEchoChar((char) 0);
                    txtRegConfirmPassword.setEchoChar((char) 0);
                    btnToggleRegPass.setText("Hide");
                } else {
                    txtRegPassword.setEchoChar('•');
                    txtRegConfirmPassword.setEchoChar('•');
                    btnToggleRegPass.setText("Show");
                }
            });
            regPassRow.add(txtRegPassword, BorderLayout.CENTER);
            regPassRow.add(btnToggleRegPass, BorderLayout.EAST);
            regPanel.add(regPassRow, gbcR);

            gbcR.gridy = regR++;
            regPanel.add(createFieldLabel("Confirm Password *"), gbcR);

            gbcR.gridy = regR++;
            regPanel.add(txtRegConfirmPassword, gbcR);

            gbcR.gridy = regR++;
            gbcR.insets = new Insets(10, 4, 4, 4);
            ModernButton btnDoRegister = new ModernButton("Create Account & Sign In", COLOR_SUCCESS_GREEN);
            btnDoRegister.setPreferredSize(new Dimension(200, 36));
            btnDoRegister.addActionListener(e -> handleRegister());
            regPanel.add(btnDoRegister, gbcR);

            authCardContainer.add(signInPanel, "SIGN_IN");
            authCardContainer.add(regPanel, "REGISTER");

            JPanel centerContainer = new JPanel(new BorderLayout(0, 10));
            centerContainer.setOpaque(false);
            centerContainer.add(tabSelectorPanel, BorderLayout.NORTH);
            centerContainer.add(authCardContainer, BorderLayout.CENTER);

            mainCard.add(centerContainer, BorderLayout.CENTER);

            // Bottom Disclaimer
            JLabel lblDisclaimer = new JLabel("<html><center>* Academic demonstration authentication. Passwords hashed with salted SHA-256. *</center></html>", SwingConstants.CENTER);
            lblDisclaimer.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            lblDisclaimer.setForeground(COLOR_TEXT_MUTED);
            mainCard.add(lblDisclaimer, BorderLayout.SOUTH);

            add(mainCard);
        }

        private void styleAuthTabButton(JButton btn, boolean active) {
            btn.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
            btn.setBackground(active ? COLOR_PRIMARY_BLUE : Color.WHITE);
            btn.setForeground(active ? Color.WHITE : COLOR_TEXT_MAIN);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(6, 12, 6, 12)));
        }

        private void handleSignIn() {
            String usernameOrEmail = txtSignInUsername.getText().trim();
            String password = new String(txtSignInPassword.getPassword());

            if (usernameOrEmail.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your username or registered email address.", "Input Required", JOptionPane.WARNING_MESSAGE);
                txtSignInUsername.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your password.", "Input Required", JOptionPane.WARNING_MESSAGE);
                txtSignInPassword.requestFocus();
                return;
            }

            User user = app.getUserManager().authenticate(usernameOrEmail, password);
            if (user != null) {
                UserSession.getInstance().login(user);
                app.onUserLoggedIn();
                JOptionPane.showMessageDialog(this, "Welcome back, " + user.getFullName() + "!", "Authentication Successful", JOptionPane.INFORMATION_MESSAGE);
                app.navigateTo("DASHBOARD", app.btnNavDashboard);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username/email or password.\n\nTip: You can use the default demo account:\nUsername: demo\nPassword: demo123",
                        "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void handleGuestMode() {
            UserSession.getInstance().loginAsGuest();
            app.onUserLoggedIn();
            JOptionPane.showMessageDialog(this, "Continuing in Demonstration Guest Mode.\nYou have full access to search and view tickets.", "Guest Mode Active", JOptionPane.INFORMATION_MESSAGE);
            app.navigateTo("DASHBOARD", app.btnNavDashboard);
        }

        private void handleRegister() {
            String fullName = txtRegFullName.getText().trim();
            String username = txtRegUsername.getText().trim();
            String email = txtRegEmail.getText().trim();
            String pass = new String(txtRegPassword.getPassword());
            String confirmPass = new String(txtRegConfirmPassword.getPassword());

            if (fullName.length() < 2) {
                JOptionPane.showMessageDialog(this, "Please enter your full legal name (at least 2 characters).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtRegFullName.requestFocus();
                return;
            }
            if (username.length() < 3) {
                JOptionPane.showMessageDialog(this, "Username must contain at least 3 alphanumeric characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtRegUsername.requestFocus();
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtRegEmail.requestFocus();
                return;
            }
            if (pass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtRegPassword.requestFocus();
                return;
            }
            if (!pass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match. Please verify your password entry.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtRegConfirmPassword.requestFocus();
                return;
            }

            try {
                User newUser = app.getUserManager().registerUser(username, fullName, email, pass);
                UserSession.getInstance().login(newUser);
                app.onUserLoggedIn();
                JOptionPane.showMessageDialog(this,
                        "Account registered successfully!\nWelcome to RailBook, " + newUser.getFullName() + ".",
                        "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
                app.navigateTo("DASHBOARD", app.btnNavDashboard);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.WARNING_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving user to users.txt: " + ex.getMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // SECTION 1: DASHBOARD PANEL
    // =========================================================================

    public static class DashboardPanel extends JPanel {
        private final TrainReservationApp app;
        private final StatCard cardTotal;
        private final StatCard cardSleeper;
        private final StatCard cardAC;
        private final StatCard cardGeneral;
        private final StatCard cardRevenue;
        private final DefaultTableModel recentTableModel;
        private float welcomeAlpha = 0.0f;
        private Timer welcomeTimer;

        public DashboardPanel(TrainReservationApp app) {
            this.app = app;
            setLayout(new BorderLayout(20, 20));
            setBackground(COLOR_CANVAS);
            setBorder(new EmptyBorder(25, 30, 25, 30));

            // --- Top Welcome Card with Fade-In Animation ---
            JPanel welcomeCard = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, welcomeAlpha));
                    g2.setColor(COLOR_PRIMARY_BLUE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            welcomeCard.setOpaque(false);
            welcomeCard.setBorder(new EmptyBorder(20, 25, 20, 25));

            JLabel lblWelcomeTitle = new JLabel("Welcome to RailBook Portal");
            lblWelcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblWelcomeTitle.setForeground(Color.WHITE);

            JLabel lblWelcomeDesc = new JLabel("College Microproject demonstrating Polymorphism, Dynamic ArrayList Storage, and Safe File Persistence.");
            lblWelcomeDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblWelcomeDesc.setForeground(new Color(224, 242, 254));

            JPanel textHolder = new JPanel(new GridLayout(2, 1, 0, 4));
            textHolder.setOpaque(false);
            textHolder.add(lblWelcomeTitle);
            textHolder.add(lblWelcomeDesc);
            welcomeCard.add(textHolder, BorderLayout.CENTER);

            // Trigger non-blocking entrance animation
            startWelcomeAnimation(welcomeCard);

            // --- Stats Grid Panel (5 Cards) ---
            JPanel statsGrid = new JPanel(new GridLayout(1, 5, 14, 0));
            statsGrid.setOpaque(false);

            cardTotal = new StatCard("Total Bookings", "0", COLOR_PRIMARY_BLUE);
            cardSleeper = new StatCard("Sleeper Class", "0", new Color(14, 165, 233));
            cardAC = new StatCard("AC Tiers", "0", new Color(139, 92, 246));
            cardGeneral = new StatCard("General Class", "0", new Color(245, 158, 11));
            cardRevenue = new StatCard("Total Revenue", "Rs. 0", COLOR_SUCCESS_GREEN);

            statsGrid.add(cardTotal);
            statsGrid.add(cardSleeper);
            statsGrid.add(cardAC);
            statsGrid.add(cardGeneral);
            statsGrid.add(cardRevenue);

            JPanel topContainer = new JPanel(new BorderLayout(0, 16));
            topContainer.setOpaque(false);
            topContainer.add(welcomeCard, BorderLayout.NORTH);
            topContainer.add(statsGrid, BorderLayout.CENTER);

            add(topContainer, BorderLayout.NORTH);

            // --- Center: Quick Booking Shortcut & Recent Bookings Table ---
            JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
            centerPanel.setOpaque(false);

            // Quick Actions Bar
            CardPanel actionsCard = new CardPanel();
            actionsCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 12));

            JLabel lblQuick = new JLabel("Quick Actions: ");
            lblQuick.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblQuick.setForeground(COLOR_TEXT_MAIN);

            ModernButton btnQuickBook = new ModernButton("Book New Ticket", COLOR_PRIMARY_BLUE);
            btnQuickBook.addActionListener(e -> app.navigateTo("NEW_BOOKING", app.btnNavNewBooking));

            ModernButton btnQuickView = new ModernButton("View All Bookings", new Color(71, 85, 105));
            btnQuickView.addActionListener(e -> app.navigateTo("MY_BOOKINGS", app.btnNavMyBookings));

            ModernButton btnQuickReceipt = new ModernButton("Open Receipt Viewer", new Color(14, 165, 233));
            btnQuickReceipt.addActionListener(e -> app.navigateTo("TICKET_RECEIPT", app.btnNavReceipt));

            actionsCard.add(lblQuick);
            actionsCard.add(btnQuickBook);
            actionsCard.add(btnQuickView);
            actionsCard.add(btnQuickReceipt);

            centerPanel.add(actionsCard, BorderLayout.NORTH);

            // Recent Bookings Card
            CardPanel tableCard = new CardPanel();
            tableCard.setLayout(new BorderLayout(0, 10));
            tableCard.setBorder(new EmptyBorder(15, 18, 15, 18));

            JLabel lblTableTitle = new JLabel("Recent Reservations");
            lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblTableTitle.setForeground(COLOR_TEXT_MAIN);

            String[] columns = {"PNR", "Passenger Name", "Train No.", "Class", "Fare (Rs.)", "Status"};
            recentTableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };

            JTable recentTable = createStyledTable(recentTableModel);
            JScrollPane scrollPane = new JScrollPane(recentTable);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());

            tableCard.add(lblTableTitle, BorderLayout.NORTH);
            tableCard.add(scrollPane, BorderLayout.CENTER);

            centerPanel.add(tableCard, BorderLayout.CENTER);
            add(centerPanel, BorderLayout.CENTER);

            refreshData();
        }

        private void startWelcomeAnimation(JComponent target) {
            welcomeAlpha = 0.0f;
            welcomeTimer = new Timer(20, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    welcomeAlpha += 0.06f;
                    if (welcomeAlpha >= 1.0f) {
                        welcomeAlpha = 1.0f;
                        welcomeTimer.stop();
                    }
                    target.repaint();
                }
            });
            welcomeTimer.start();
        }

        public void refreshData() {
            ArrayList<Ticket> all = app.getManager().getAllTickets();

            int total = all.size();
            int sleeper = 0;
            int ac = 0;
            int general = 0;
            double revenue = 0.0;

            for (Ticket t : all) {
                revenue += t.calculateTotalFare();
                if (t instanceof SleeperTicket) sleeper++;
                else if (t instanceof ACTierTicket) ac++;
                else if (t instanceof GeneralTicket) general++;
            }

            cardTotal.setValue(String.valueOf(total));
            cardSleeper.setValue(String.valueOf(sleeper));
            cardAC.setValue(String.valueOf(ac));
            cardGeneral.setValue(String.valueOf(general));
            cardRevenue.setValue(String.format("Rs. %.0f", revenue));

            // Refresh recent table (latest 5)
            recentTableModel.setRowCount(0);
            int start = Math.max(0, all.size() - 5);
            for (int i = all.size() - 1; i >= start; i--) {
                Ticket t = all.get(i);
                recentTableModel.addRow(new Object[]{
                        t.getPnr(),
                        t.getPassengerName(),
                        t.getTrainNumber(),
                        t.getTicketType(),
                        String.format("%.2f", t.calculateTotalFare()),
                        "CONFIRMED"
                });
            }
        }
    }

    // =========================================================================
    // SECTION 2: NEW RESERVATION PANEL (Form, Dynamic Inputs, Live Fare Preview)
    // =========================================================================

    public static class NewBookingPanel extends JPanel {
        private final TrainReservationApp app;

        // Form Fields
        private final JTextField txtPassengerName;
        private final JSpinner spnAge;
        private final JComboBox<String> cmbTrain;
        private final JTextField txtBaseFare;
        private final JComboBox<String> cmbCategory;

        // Dynamic Subclass Inputs
        private final JPanel dynamicSubclassCard;
        private final CardLayout dynamicLayout;
        private final JComboBox<String> cmbBerth;
        private final JComboBox<String> cmbAcTier;
        private final JSpinner spnValidity;

        // Live Fare Preview Labels
        private final JLabel lblPreviewBase;
        private final JLabel lblPreviewSurcharge;
        private final JLabel lblPreviewGST;
        private final JLabel lblPreviewTotal;

        // Animated Slide-in Booking Confirmation Result Card
        private final AnimatedSlideCard resultSlidePanel;
        private final JLabel lblResultPnr;
        private final JLabel lblResultDetails;
        private final AnimatedCheckmark checkmarkComponent;
        private String lastBookedPnr = null;

        public NewBookingPanel(TrainReservationApp app) {
            this.app = app;
            setLayout(new BorderLayout(25, 25));
            setBackground(COLOR_CANVAS);
            setBorder(new EmptyBorder(25, 30, 25, 30));

            // --- Header ---
            JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            headerPanel.setOpaque(false);
            JLabel lblTitle = new JLabel("Book Train Reservation");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblTitle.setForeground(COLOR_TEXT_MAIN);

            JLabel lblSubtitle = new JLabel("Enter journey details. Fare is calculated polymorphically via Practicum 2 inheritance.");
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblSubtitle.setForeground(COLOR_TEXT_MUTED);

            headerPanel.add(lblTitle);
            headerPanel.add(lblSubtitle);
            add(headerPanel, BorderLayout.NORTH);

            // --- Main Body: Left (Form) & Right (Live Preview + Animated Result) ---
            JPanel bodyPanel = new JPanel(new GridLayout(1, 2, 25, 0));
            bodyPanel.setOpaque(false);

            // 1. Left Form Card
            CardPanel formCard = new CardPanel();
            formCard.setLayout(new GridBagLayout());
            formCard.setBorder(new EmptyBorder(20, 25, 20, 25));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            int row = 0;

            // Passenger Name
            gbc.gridx = 0; gbc.gridy = row++;
            formCard.add(createFieldLabel("Passenger Full Name *"), gbc);
            gbc.gridy = row++;
            txtPassengerName = new JTextField();
            txtPassengerName.setPreferredSize(new Dimension(200, 36));
            txtPassengerName.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(2, 8, 2, 8)));
            formCard.add(txtPassengerName, gbc);

            // Age & Train in split row
            JPanel splitRow = new JPanel(new GridLayout(1, 2, 12, 0));
            splitRow.setOpaque(false);

            JPanel agePanel = new JPanel(new BorderLayout(0, 4));
            agePanel.setOpaque(false);
            agePanel.add(createFieldLabel("Age"), BorderLayout.NORTH);
            spnAge = new JSpinner(new SpinnerNumberModel(28, 1, 120, 1));
            spnAge.setPreferredSize(new Dimension(80, 36));
            agePanel.add(spnAge, BorderLayout.CENTER);

            JPanel trainPanel = new JPanel(new BorderLayout(0, 4));
            trainPanel.setOpaque(false);
            trainPanel.add(createFieldLabel("Train Number / Name *"), BorderLayout.NORTH);
            cmbTrain = new JComboBox<>(new String[]{
                    "12951 - Mumbai Central Rajdhani Express",
                    "12002 - New Delhi Shatabdi Express",
                    "22436 - Varanasi Vande Bharat Express",
                    "12259 - Sealdah AC Duronto Express",
                    "12626 - Kerala Superfast Express"
            });
            cmbTrain.setEditable(true);
            cmbTrain.setPreferredSize(new Dimension(160, 36));
            trainPanel.add(cmbTrain, BorderLayout.CENTER);

            splitRow.add(agePanel);
            splitRow.add(trainPanel);

            gbc.gridy = row++;
            formCard.add(splitRow, gbc);

            // Base Fare
            gbc.gridy = row++;
            formCard.add(createFieldLabel("Base Fare (Rs.) *"), gbc);
            gbc.gridy = row++;
            txtBaseFare = new JTextField("450.00");
            txtBaseFare.setPreferredSize(new Dimension(200, 36));
            txtBaseFare.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(2, 8, 2, 8)));
            formCard.add(txtBaseFare, gbc);

            // Ticket Category Selector
            gbc.gridy = row++;
            formCard.add(createFieldLabel("Travel Category (Practicum 2 Subclass) *"), gbc);
            gbc.gridy = row++;
            cmbCategory = new JComboBox<>(new String[]{
                    "Sleeper Class (SL) — Subclass 1",
                    "AC Tier Class (1A/2A/3A) — Subclass 2",
                    "General Unreserved (GEN) — Subclass 3"
            });
            cmbCategory.setPreferredSize(new Dimension(200, 36));
            formCard.add(cmbCategory, gbc);

            // Dynamic Subclass Inputs Container
            gbc.gridy = row++;
            dynamicLayout = new CardLayout();
            dynamicSubclassCard = new JPanel(dynamicLayout);
            dynamicSubclassCard.setOpaque(false);

            // Sleeper Subclass Config: Berth selector
            JPanel sleeperPanel = new JPanel(new BorderLayout(0, 4));
            sleeperPanel.setOpaque(false);
            sleeperPanel.add(createFieldLabel("Berth Preference (Sleeper Attribute)"), BorderLayout.NORTH);
            cmbBerth = new JComboBox<>(new String[]{"Lower Berth", "Middle Berth", "Upper Berth", "Side Lower", "Side Upper"});
            cmbBerth.setPreferredSize(new Dimension(200, 36));
            sleeperPanel.add(cmbBerth, BorderLayout.CENTER);

            // AC Tier Subclass Config: Tier level
            JPanel acPanel = new JPanel(new BorderLayout(0, 4));
            acPanel.setOpaque(false);
            acPanel.add(createFieldLabel("AC Class Tier (AC Tier Attribute)"), BorderLayout.NORTH);
            cmbAcTier = new JComboBox<>(new String[]{
                    "Tier 1 (1A) — Rs. 500 Surcharge + 5% GST",
                    "Tier 2 (2A) — Rs. 300 Surcharge + 5% GST",
                    "Tier 3 (3A) — Rs. 150 Surcharge + 5% GST"
            });
            cmbAcTier.setSelectedIndex(1); // Default 2A
            cmbAcTier.setPreferredSize(new Dimension(200, 36));
            acPanel.add(cmbAcTier, BorderLayout.CENTER);

            // General Subclass Config: Validity hours
            JPanel genPanel = new JPanel(new BorderLayout(0, 4));
            genPanel.setOpaque(false);
            genPanel.add(createFieldLabel("Validity Period (General Attribute)"), BorderLayout.NORTH);
            spnValidity = new JSpinner(new SpinnerNumberModel(24, 6, 72, 6));
            spnValidity.setPreferredSize(new Dimension(200, 36));
            genPanel.add(spnValidity, BorderLayout.CENTER);

            dynamicSubclassCard.add(sleeperPanel, "SLEEPER");
            dynamicSubclassCard.add(acPanel, "AC");
            dynamicSubclassCard.add(genPanel, "GENERAL");

            formCard.add(dynamicSubclassCard, gbc);

            // Action Buttons
            gbc.gridy = row++;
            gbc.insets = new Insets(16, 6, 6, 6);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            btnPanel.setOpaque(false);

            ModernButton btnBook = new ModernButton("Confirm & Book Ticket", COLOR_PRIMARY_BLUE);
            btnBook.setPreferredSize(new Dimension(190, 40));
            btnBook.addActionListener(e -> handleBookingProcess());

            ModernButton btnReset = new ModernButton("Reset Form", new Color(148, 163, 184));
            btnReset.setPreferredSize(new Dimension(110, 40));
            btnReset.addActionListener(e -> resetForm());

            btnPanel.add(btnBook);
            btnPanel.add(btnReset);
            formCard.add(btnPanel, gbc);

            bodyPanel.add(formCard);

            // 2. Right Side: Live Dynamic Fare Card & Animated Result Card
            JPanel rightContainer = new JPanel(new BorderLayout(0, 20));
            rightContainer.setOpaque(false);

            // Live Dynamic Fare Breakdown Card
            CardPanel fareCard = new CardPanel();
            fareCard.setLayout(new BorderLayout());
            fareCard.setBorder(new EmptyBorder(20, 22, 20, 22));

            JLabel lblFareTitle = new JLabel("Live Fare Calculator (Polymorphic Preview)");
            lblFareTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblFareTitle.setForeground(COLOR_TEXT_MAIN);

            JPanel fareBreakdown = new JPanel(new GridLayout(5, 1, 0, 8));
            fareBreakdown.setOpaque(false);
            fareBreakdown.setBorder(new EmptyBorder(12, 0, 12, 0));

            lblPreviewBase = createFareRowLabel("Base Fare:", "Rs. 450.00");
            lblPreviewSurcharge = createFareRowLabel("Class Surcharge:", "Rs. 50.00");
            lblPreviewGST = createFareRowLabel("Applicable GST:", "Rs. 0.00");
            lblPreviewTotal = new JLabel("Total Fare: Rs. 500.00");
            lblPreviewTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblPreviewTotal.setForeground(COLOR_PRIMARY_BLUE);

            JLabel lblAcademicRule = new JLabel("* Simplified Academic Fare Rule for College Practicum");
            lblAcademicRule.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblAcademicRule.setForeground(COLOR_TEXT_MUTED);

            fareBreakdown.add(lblPreviewBase);
            fareBreakdown.add(lblPreviewSurcharge);
            fareBreakdown.add(lblPreviewGST);
            fareBreakdown.add(lblPreviewTotal);
            fareBreakdown.add(lblAcademicRule);

            fareCard.add(lblFareTitle, BorderLayout.NORTH);
            fareCard.add(fareBreakdown, BorderLayout.CENTER);

            // Animated Slide-in Result Card (Smooth entrance on booking)
            resultSlidePanel = new AnimatedSlideCard();
            resultSlidePanel.setLayout(new BorderLayout(10, 10));
            resultSlidePanel.setBorder(new EmptyBorder(16, 20, 16, 20));
            resultSlidePanel.setVisible(false);

            checkmarkComponent = new AnimatedCheckmark();
            checkmarkComponent.setPreferredSize(new Dimension(50, 50));

            JPanel resultTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            resultTop.setOpaque(false);
            resultTop.add(checkmarkComponent);

            JPanel resultHeader = new JPanel(new GridLayout(2, 1));
            resultHeader.setOpaque(false);
            JLabel lblSuccess = new JLabel("Booking Confirmed!");
            lblSuccess.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblSuccess.setForeground(COLOR_SUCCESS_GREEN);
            lblResultPnr = new JLabel("PNR: —");
            lblResultPnr.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblResultPnr.setForeground(COLOR_NAVY_DARK);
            resultHeader.add(lblSuccess);
            resultHeader.add(lblResultPnr);
            resultTop.add(resultHeader);

            lblResultDetails = new JLabel("<html>Ticket saved to <b>reservations.txt</b>.</html>");
            lblResultDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblResultDetails.setForeground(COLOR_TEXT_MAIN);

            JPanel resultActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            resultActions.setOpaque(false);

            ModernButton btnViewReceipt = new ModernButton("View E-Receipt", COLOR_PRIMARY_BLUE);
            btnViewReceipt.addActionListener(e -> {
                if (lastBookedPnr != null) {
                    app.showReceiptForPNR(lastBookedPnr);
                }
            });

            ModernButton btnGenerateReceipt = new ModernButton("Generate PNR_receipt.txt", COLOR_SUCCESS_GREEN);
            btnGenerateReceipt.addActionListener(e -> {
                if (lastBookedPnr != null) {
                    try {
                        File receipt = app.getManager().generateReceipt(lastBookedPnr);
                        JOptionPane.showMessageDialog(this,
                                "Receipt generated successfully:\n" + receipt.getAbsolutePath(),
                                "Receipt Exported", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Receipt export error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            resultActions.add(btnViewReceipt);
            resultActions.add(btnGenerateReceipt);

            resultSlidePanel.add(resultTop, BorderLayout.NORTH);
            resultSlidePanel.add(lblResultDetails, BorderLayout.CENTER);
            resultSlidePanel.add(resultActions, BorderLayout.SOUTH);

            rightContainer.add(fareCard, BorderLayout.NORTH);
            rightContainer.add(resultSlidePanel, BorderLayout.CENTER);

            bodyPanel.add(rightContainer);
            add(bodyPanel, BorderLayout.CENTER);

            // Wire Dynamic Listeners for Live Calculations
            setupListeners();
            updateLiveFareCalculation();
        }

        public void onNavigatedTo() {
            // Auto-fill passenger name from authenticated user if empty or guest
            if (txtPassengerName.getText().trim().isEmpty() && !UserSession.getInstance().isGuest()) {
                txtPassengerName.setText(UserSession.getInstance().getDisplayName());
            }
        }

        private void setupListeners() {
            cmbCategory.addActionListener(e -> {
                int idx = cmbCategory.getSelectedIndex();
                if (idx == 0) dynamicLayout.show(dynamicSubclassCard, "SLEEPER");
                else if (idx == 1) dynamicLayout.show(dynamicSubclassCard, "AC");
                else dynamicLayout.show(dynamicSubclassCard, "GENERAL");
                updateLiveFareCalculation();
            });

            cmbAcTier.addActionListener(e -> updateLiveFareCalculation());
            txtBaseFare.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    updateLiveFareCalculation();
                }
            });
        }

        private void updateLiveFareCalculation() {
            try {
                double base = Double.parseDouble(txtBaseFare.getText().trim());
                if (base < 0) base = 0;

                int cat = cmbCategory.getSelectedIndex();
                if (cat == 0) {
                    // Sleeper: Surcharge 50
                    lblPreviewBase.setText(String.format("Base Fare: Rs. %.2f", base));
                    lblPreviewSurcharge.setText("Sleeper Surcharge: Rs. 50.00");
                    lblPreviewGST.setText("Applicable GST: Rs. 0.00 (N/A)");
                    lblPreviewTotal.setText(String.format("Total Fare: Rs. %.2f", base + 50.0));
                } else if (cat == 1) {
                    // AC Tier
                    int tierIdx = cmbAcTier.getSelectedIndex() + 1; // 1, 2, or 3
                    double tierCharge = (tierIdx == 1) ? 500.0 : (tierIdx == 2) ? 300.0 : 150.0;
                    double subtotal = base + tierCharge;
                    double gst = subtotal * 0.05;
                    lblPreviewBase.setText(String.format("Base Fare: Rs. %.2f", base));
                    lblPreviewSurcharge.setText(String.format("AC Tier %dA Surcharge: Rs. %.2f", tierIdx, tierCharge));
                    lblPreviewGST.setText(String.format("GST (5%%): Rs. %.2f", gst));
                    lblPreviewTotal.setText(String.format("Total Fare: Rs. %.2f", subtotal + gst));
                } else {
                    // General
                    lblPreviewBase.setText(String.format("Base Fare: Rs. %.2f", base));
                    lblPreviewSurcharge.setText("General Service Fee: Rs. 15.00");
                    lblPreviewGST.setText("Applicable GST: Rs. 0.00 (N/A)");
                    lblPreviewTotal.setText(String.format("Total Fare: Rs. %.2f", base + 15.0));
                }
            } catch (Exception ignored) {
                lblPreviewTotal.setText("Total Fare: --");
            }
        }

        private void handleBookingProcess() {
            String name = txtPassengerName.getText().trim();
            if (name.length() < 2) {
                JOptionPane.showMessageDialog(this, "Please enter a valid passenger name (minimum 2 characters).",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtPassengerName.requestFocus();
                return;
            }

            double baseFare;
            try {
                baseFare = Double.parseDouble(txtBaseFare.getText().trim());
                if (baseFare < 0) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive base fare.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtBaseFare.requestFocus();
                return;
            }

            // Extract train number
            String trainRaw = (String) cmbTrain.getSelectedItem();
            String trainNumber = (trainRaw != null && trainRaw.contains(" - "))
                    ? trainRaw.split(" - ")[0].trim()
                    : (trainRaw != null ? trainRaw.trim() : "12951");

            int cat = cmbCategory.getSelectedIndex();
            Ticket ticketToBook;

            // Instantiate concrete Ticket subclass (Practicum 2)
            try {
                if (cat == 0) {
                    String berth = (String) cmbBerth.getSelectedItem();
                    ticketToBook = new SleeperTicket(name, trainNumber, baseFare, berth);
                } else if (cat == 1) {
                    int acTier = cmbAcTier.getSelectedIndex() + 1; // 1, 2, or 3
                    ticketToBook = new ACTierTicket(name, trainNumber, baseFare, acTier);
                } else {
                    int hours = (Integer) spnValidity.getValue();
                    ticketToBook = new GeneralTicket(name, trainNumber, baseFare, hours);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Confirmation Dialog
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Confirm Reservation Details:\n\nPassenger: %s\nTrain: %s\nCategory: %s\nTotal Fare: Rs. %.2f\n\nProceed to book?",
                            ticketToBook.getPassengerName(), ticketToBook.getTrainNumber(), ticketToBook.getTicketType(), ticketToBook.calculateTotalFare()),
                    "Confirm Booking", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Non-blocking simulated booking progress indicator using Timer
            JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Processing Booking", true);
            JPanel progressPanel = new JPanel(new BorderLayout(10, 15));
            progressPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
            JLabel lblProgress = new JLabel("Securing reservation and generating PNR...");
            lblProgress.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JProgressBar pBar = new JProgressBar();
            pBar.setIndeterminate(true);
            progressPanel.add(lblProgress, BorderLayout.NORTH);
            progressPanel.add(pBar, BorderLayout.CENTER);
            progressDialog.getContentPane().add(progressPanel);
            progressDialog.pack();
            progressDialog.setLocationRelativeTo(this);

            Timer timer = new Timer(500, e -> {
                progressDialog.dispose();
                try {
                    // Practicum 10 (ArrayList add) & Practicum 12 (File Persistence)
                    boolean success = app.getManager().bookTicket(ticketToBook);
                    if (success) {
                        lastBookedPnr = ticketToBook.getPnr();
                        displayBookingSuccess(ticketToBook);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "File persistence error while saving booking: " + ex.getMessage(),
                            "I/O Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            timer.setRepeats(false);
            timer.start();
            progressDialog.setVisible(true);
        }

        private void displayBookingSuccess(Ticket ticket) {
            lblResultPnr.setText("PNR: " + ticket.getPnr());
            lblResultDetails.setText(String.format(
                    "<html>Passenger: <b>%s</b> | Train: <b>%s</b><br/>Class: <b>%s</b> | Total Fare: <b>Rs. %.2f</b><br/>Saved safely to <i>reservations.txt</i>.</html>",
                    ticket.getPassengerName(), ticket.getTrainNumber(), ticket.getTicketType(), ticket.calculateTotalFare()
            ));

            // Smooth entrance animation for ticket confirmation card followed by checkmark
            resultSlidePanel.startSlideIn(() -> {
                checkmarkComponent.startAnimation();
            });
        }

        private void resetForm() {
            txtPassengerName.setText("");
            txtBaseFare.setText("450.00");
            spnAge.setValue(28);
            cmbCategory.setSelectedIndex(0);
            resultSlidePanel.setVisible(false);
            updateLiveFareCalculation();
        }

        private static JLabel createFareRowLabel(String label, String value) {
            JLabel lbl = new JLabel(label + " " + value);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(COLOR_TEXT_MAIN);
            return lbl;
        }
    }

    // =========================================================================
    // SECTION 3: MY BOOKINGS PANEL (JTable, Live Filtering, Printing & Cancellation)
    // =========================================================================

    public static class MyBookingsPanel extends JPanel {
        private final TrainReservationApp app;
        private final DefaultTableModel tableModel;
        private final JTable table;
        private final JTextField txtSearch;
        private final JLabel lblCount;

        public MyBookingsPanel(TrainReservationApp app) {
            this.app = app;
            setLayout(new BorderLayout(15, 15));
            setBackground(COLOR_CANVAS);
            setBorder(new EmptyBorder(25, 30, 25, 30));

            // Header & Toolbar
            JPanel topPanel = new JPanel(new BorderLayout(15, 10));
            topPanel.setOpaque(false);

            JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
            titlePanel.setOpaque(false);
            JLabel lblTitle = new JLabel("My Bookings Registry");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblTitle.setForeground(COLOR_TEXT_MAIN);

            JLabel lblSubtitle = new JLabel("Live reservations managed via Practicum 10 (ArrayList<Ticket>) and persisted in reservations.txt.");
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblSubtitle.setForeground(COLOR_TEXT_MUTED);

            titlePanel.add(lblTitle);
            titlePanel.add(lblSubtitle);
            topPanel.add(titlePanel, BorderLayout.NORTH);

            // Filter & Action Toolbar
            CardPanel toolbar = new CardPanel();
            toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));

            JLabel lblFilter = new JLabel("Search Bookings:");
            lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));

            txtSearch = new JTextField(18);
            txtSearch.setPreferredSize(new Dimension(180, 34));
            txtSearch.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(2, 8, 2, 8)));
            txtSearch.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    filterTable(txtSearch.getText().trim());
                }
            });

            ModernButton btnRefresh = new ModernButton("Refresh", new Color(71, 85, 105));
            btnRefresh.addActionListener(e -> refreshTable());

            ModernButton btnPrintReceipt = new ModernButton("Print Receipt (PNR_receipt.txt)", COLOR_SUCCESS_GREEN);
            btnPrintReceipt.addActionListener(e -> printSelectedReceipt());

            ModernButton btnCancel = new ModernButton("Cancel Ticket", COLOR_DANGER_RED);
            btnCancel.addActionListener(e -> cancelSelectedTicket());

            lblCount = new JLabel("Total Bookings: 0");
            lblCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblCount.setForeground(COLOR_TEXT_MUTED);

            toolbar.add(lblFilter);
            toolbar.add(txtSearch);
            toolbar.add(btnRefresh);
            toolbar.add(btnPrintReceipt);
            toolbar.add(btnCancel);
            toolbar.add(Box.createHorizontalStrut(15));
            toolbar.add(lblCount);

            topPanel.add(toolbar, BorderLayout.CENTER);
            add(topPanel, BorderLayout.NORTH);

            // Center Table
            CardPanel tableCard = new CardPanel();
            tableCard.setLayout(new BorderLayout());
            tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));

            String[] columns = {"PNR", "Passenger Name", "Train No.", "Category", "Specific Info", "Base Fare", "Total Fare", "Status"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };

            table = createStyledTable(tableModel);
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        printSelectedReceipt();
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            tableCard.add(scrollPane, BorderLayout.CENTER);
            add(tableCard, BorderLayout.CENTER);

            refreshTable();
        }

        public void refreshTable() {
            txtSearch.setText("");
            ArrayList<Ticket> all = app.getManager().getAllTickets();
            populateTable(all);
        }

        private void filterTable(String query) {
            if (query.isEmpty()) {
                populateTable(app.getManager().getAllTickets());
            } else {
                // Practicum 10: Searching using ArrayList linear traversal
                populateTable(app.getManager().searchTickets(query));
            }
        }

        private void populateTable(ArrayList<Ticket> tickets) {
            tableModel.setRowCount(0);
            for (Ticket t : tickets) {
                String specific = "N/A";
                if (t instanceof SleeperTicket) {
                    specific = "Berth: " + ((SleeperTicket) t).getBerthType();
                } else if (t instanceof ACTierTicket) {
                    specific = "AC Tier: " + ((ACTierTicket) t).getAcTier() + "A";
                } else if (t instanceof GeneralTicket) {
                    specific = "Valid: " + ((GeneralTicket) t).getValidityHours() + "h";
                }

                tableModel.addRow(new Object[]{
                        t.getPnr(),
                        t.getPassengerName(),
                        t.getTrainNumber(),
                        t.getTicketType(),
                        specific,
                        String.format("Rs. %.2f", t.getBaseFare()),
                        String.format("Rs. %.2f", t.calculateTotalFare()),
                        "CONFIRMED"
                });
            }
            lblCount.setText("Total Bookings: " + tickets.size());
        }

        private void printSelectedReceipt() {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a booking from the table first.",
                        "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String pnr = (String) tableModel.getValueAt(row, 0);
            app.showReceiptForPNR(pnr);
        }

        private void cancelSelectedTicket() {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a ticket from the table to cancel.",
                        "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String pnr = (String) tableModel.getValueAt(row, 0);
            String name = (String) tableModel.getValueAt(row, 1);

            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel reservation " + pnr + " for passenger '" + name + "'?",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    // Practicum 10 (ArrayList remove) & Practicum 12 (File Persistence)
                    boolean removed = app.getManager().cancelTicket(pnr);
                    if (removed) {
                        JOptionPane.showMessageDialog(this, "Ticket " + pnr + " successfully cancelled.",
                                "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        refreshTable();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to cancel: Ticket not found.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error persisting cancellation: " + ex.getMessage(),
                            "I/O Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // =========================================================================
    // SECTION 4: MANAGE TICKETS PANEL (Search, Inspection, Cancellation)
    // =========================================================================

    public static class ManageTicketsPanel extends JPanel {
        private final TrainReservationApp app;
        private final JTextField txtSearchQuery;
        private final DefaultTableModel searchTableModel;
        private final JTable searchTable;
        private final JPanel detailCard;
        private final JLabel lblDetailPNR;
        private final JLabel lblDetailName;
        private final JLabel lblDetailTrain;
        private final JLabel lblDetailCategory;
        private final JLabel lblDetailFare;
        private Ticket currentSelectedTicket = null;

        public ManageTicketsPanel(TrainReservationApp app) {
            this.app = app;
            setLayout(new BorderLayout(15, 15));
            setBackground(COLOR_CANVAS);
            setBorder(new EmptyBorder(25, 30, 25, 30));

            // Header
            JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            headerPanel.setOpaque(false);
            JLabel lblTitle = new JLabel("Manage & Search Tickets");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblTitle.setForeground(COLOR_TEXT_MAIN);

            JLabel lblSubtitle = new JLabel("Lookup tickets by PNR, Passenger Name, or Train Number. Cancel tickets with confirmation.");
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblSubtitle.setForeground(COLOR_TEXT_MUTED);

            headerPanel.add(lblTitle);
            headerPanel.add(lblSubtitle);
            add(headerPanel, BorderLayout.NORTH);

            // Center Panel: Split into Search Table (Left) and Ticket Detail Card (Right)
            JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
            centerPanel.setOpaque(false);

            // Search Bar Card
            CardPanel searchCard = new CardPanel();
            searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));

            JLabel lblSearch = new JLabel("Search Query:");
            lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));

            txtSearchQuery = new JTextField(22);
            txtSearchQuery.setPreferredSize(new Dimension(220, 34));
            txtSearchQuery.setBorder(new CompoundBorder(new RoundedBorder(COLOR_CARD_BORDER, 8), new EmptyBorder(2, 8, 2, 8)));
            txtSearchQuery.addActionListener(e -> executeSearch());

            ModernButton btnSearch = new ModernButton("Search", COLOR_PRIMARY_BLUE);
            btnSearch.addActionListener(e -> executeSearch());

            ModernButton btnReset = new ModernButton("Show All", new Color(71, 85, 105));
            btnReset.addActionListener(e -> refreshTable());

            searchCard.add(lblSearch);
            searchCard.add(txtSearchQuery);
            searchCard.add(btnSearch);
            searchCard.add(btnReset);
            centerPanel.add(searchCard, BorderLayout.NORTH);

            // Split View: Table on Left (60%), Details on Right (40%)
            JPanel splitBody = new JPanel(new GridLayout(1, 2, 20, 0));
            splitBody.setOpaque(false);

            // Left: Table
            CardPanel tableCard = new CardPanel();
            tableCard.setLayout(new BorderLayout());
            tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));

            String[] cols = {"PNR", "Passenger", "Train", "Category", "Fare"};
            searchTableModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };

            searchTable = createStyledTable(searchTableModel);
            searchTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = searchTable.getSelectedRow();
                    if (row >= 0) {
                        String pnr = (String) searchTableModel.getValueAt(row, 0);
                        selectTicket(pnr);
                    }
                }
            });

            tableCard.add(new JScrollPane(searchTable), BorderLayout.CENTER);
            splitBody.add(tableCard);

            // Right: Ticket Details & Action Card
            detailCard = new CardPanel();
            detailCard.setLayout(new BorderLayout(15, 15));
            detailCard.setBorder(new EmptyBorder(20, 22, 20, 22));

            JLabel lblCardHeader = new JLabel("Selected Ticket Details");
            lblCardHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblCardHeader.setForeground(COLOR_TEXT_MAIN);
            detailCard.add(lblCardHeader, BorderLayout.NORTH);

            JPanel infoGrid = new JPanel(new GridLayout(6, 1, 0, 8));
            infoGrid.setOpaque(false);

            lblDetailPNR = new JLabel("PNR: —");
            lblDetailName = new JLabel("Passenger: —");
            lblDetailTrain = new JLabel("Train Number: —");
            lblDetailCategory = new JLabel("Category: —");
            lblDetailFare = new JLabel("Total Fare: —");

            Font fontVal = new Font("Segoe UI", Font.PLAIN, 14);
            lblDetailPNR.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblDetailPNR.setForeground(COLOR_PRIMARY_BLUE);
            lblDetailName.setFont(fontVal);
            lblDetailTrain.setFont(fontVal);
            lblDetailCategory.setFont(fontVal);
            lblDetailFare.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblDetailFare.setForeground(COLOR_SUCCESS_GREEN);

            infoGrid.add(lblDetailPNR);
            infoGrid.add(lblDetailName);
            infoGrid.add(lblDetailTrain);
            infoGrid.add(lblDetailCategory);
            infoGrid.add(lblDetailFare);

            detailCard.add(infoGrid, BorderLayout.CENTER);

            // Action Buttons
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            actions.setOpaque(false);

            ModernButton btnCancelThis = new ModernButton("Cancel This Ticket", COLOR_DANGER_RED);
            btnCancelThis.addActionListener(e -> cancelCurrentTicket());

            ModernButton btnViewReceiptThis = new ModernButton("View Receipt", COLOR_PRIMARY_BLUE);
            btnViewReceiptThis.addActionListener(e -> {
                if (currentSelectedTicket != null) {
                    app.showReceiptForPNR(currentSelectedTicket.getPnr());
                }
            });

            actions.add(btnCancelThis);
            actions.add(btnViewReceiptThis);
            detailCard.add(actions, BorderLayout.SOUTH);

            splitBody.add(detailCard);
            centerPanel.add(splitBody, BorderLayout.CENTER);

            add(centerPanel, BorderLayout.CENTER);

            refreshTable();
        }

        public void refreshTable() {
            txtSearchQuery.setText("");
            populate(app.getManager().getAllTickets());
        }

        private void executeSearch() {
            String query = txtSearchQuery.getText().trim();
            if (query.isEmpty()) {
                refreshTable();
            } else {
                populate(app.getManager().searchTickets(query));
            }
        }

        private void populate(ArrayList<Ticket> list) {
            searchTableModel.setRowCount(0);
            for (Ticket t : list) {
                searchTableModel.addRow(new Object[]{
                        t.getPnr(),
                        t.getPassengerName(),
                        t.getTrainNumber(),
                        t.getTicketType(),
                        String.format("Rs. %.2f", t.calculateTotalFare())
                });
            }
            if (!list.isEmpty()) {
                searchTable.setRowSelectionInterval(0, 0);
                selectTicket(list.get(0).getPnr());
            } else {
                clearDetails();
            }
        }

        private void selectTicket(String pnr) {
            Ticket t = app.getManager().findTicketByPNR(pnr);
            if (t != null) {
                currentSelectedTicket = t;
                lblDetailPNR.setText("PNR: " + t.getPnr());
                lblDetailName.setText("Passenger: " + t.getPassengerName());
                lblDetailTrain.setText("Train Number: " + t.getTrainNumber());
                lblDetailCategory.setText("Category: " + t.getTicketType());
                lblDetailFare.setText(String.format("Total Fare: Rs. %.2f", t.calculateTotalFare()));
            }
        }

        private void clearDetails() {
            currentSelectedTicket = null;
            lblDetailPNR.setText("PNR: —");
            lblDetailName.setText("Passenger: — (No matches)");
            lblDetailTrain.setText("Train Number: —");
            lblDetailCategory.setText("Category: —");
            lblDetailFare.setText("Total Fare: —");
        }

        private void cancelCurrentTicket() {
            if (currentSelectedTicket == null) {
                JOptionPane.showMessageDialog(this, "Please select a ticket first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel ticket " + currentSelectedTicket.getPnr() + "?",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = app.getManager().cancelTicket(currentSelectedTicket.getPnr());
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Ticket cancelled successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        refreshTable();
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Persistence error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // =========================================================================
    // SECTION 5: TICKET RECEIPT PANEL (E-Ticket View & PNR_receipt.txt Export)
    // =========================================================================

    public static class TicketReceiptPanel extends JPanel {
        private final TrainReservationApp app;
        private final JComboBox<String> cmbPnrSelector;
        private final JTextArea txtAsciiReceipt;
        private final JLabel lblTicketPnr;
        private final JLabel lblTicketPassenger;
        private final JLabel lblTicketTrain;
        private final JLabel lblTicketClass;
        private final JLabel lblTicketDetails;
        private final JLabel lblTicketFare;
        private final JLabel lblTicketStatus;

        public TicketReceiptPanel(TrainReservationApp app) {
            this.app = app;
            setLayout(new BorderLayout(15, 15));
            setBackground(COLOR_CANVAS);
            setBorder(new EmptyBorder(25, 30, 25, 30));

            // Top Header
            JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            headerPanel.setOpaque(false);
            JLabel lblTitle = new JLabel("Passenger E-Ticket & Official Receipt");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblTitle.setForeground(COLOR_TEXT_MAIN);

            JLabel lblSubtitle = new JLabel("Practicum 12: Generates formatted receipt and exports directly to PNR_receipt.txt.");
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblSubtitle.setForeground(COLOR_TEXT_MUTED);

            headerPanel.add(lblTitle);
            headerPanel.add(lblSubtitle);
            add(headerPanel, BorderLayout.NORTH);

            // Toolbar: PNR selector & Export buttons
            CardPanel toolbar = new CardPanel();
            toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));

            JLabel lblSelect = new JLabel("Select PNR:");
            lblSelect.setFont(new Font("Segoe UI", Font.BOLD, 12));

            cmbPnrSelector = new JComboBox<>();
            cmbPnrSelector.setPreferredSize(new Dimension(170, 34));
            cmbPnrSelector.addActionListener(e -> {
                String selected = (String) cmbPnrSelector.getSelectedItem();
                if (selected != null && !selected.trim().isEmpty()) {
                    loadTicketByPNR(selected.trim());
                }
            });

            ModernButton btnGenerateFile = new ModernButton("Export to PNR_receipt.txt", COLOR_SUCCESS_GREEN);
            btnGenerateFile.addActionListener(e -> exportReceiptFile());

            ModernButton btnOpenFile = new ModernButton("Open Saved Receipt", new Color(71, 85, 105));
            btnOpenFile.addActionListener(e -> openReceiptInEditor());

            toolbar.add(lblSelect);
            toolbar.add(cmbPnrSelector);
            toolbar.add(btnGenerateFile);
            toolbar.add(btnOpenFile);

            // Center: Tabs for Visual E-Boarding Pass & Raw ASCII View
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            // Tab 1: Visual E-Ticket Pass
            JPanel visualPassCard = new CardPanel();
            visualPassCard.setLayout(new BorderLayout());
            visualPassCard.setBorder(new EmptyBorder(25, 30, 25, 30));

            JPanel passHeader = new JPanel(new BorderLayout());
            passHeader.setBackground(COLOR_PRIMARY_BLUE);
            passHeader.setBorder(new EmptyBorder(15, 20, 15, 20));

            JLabel lblBrand = new JLabel("RAILBOOK PASSENGER E-TICKET");
            lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblBrand.setForeground(Color.WHITE);

            lblTicketStatus = new JLabel("STATUS: CONFIRMED");
            lblTicketStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTicketStatus.setForeground(new Color(187, 247, 208));

            passHeader.add(lblBrand, BorderLayout.WEST);
            passHeader.add(lblTicketStatus, BorderLayout.EAST);
            visualPassCard.add(passHeader, BorderLayout.NORTH);

            // Details and decorative barcode split
            JPanel passBody = new JPanel(new BorderLayout(20, 0));
            passBody.setOpaque(false);

            JPanel passDetails = new JPanel(new GridLayout(6, 1, 0, 10));
            passDetails.setOpaque(false);
            passDetails.setBorder(new EmptyBorder(18, 10, 18, 10));

            lblTicketPnr = new JLabel("PNR Number: —");
            lblTicketPassenger = new JLabel("Passenger Name: —");
            lblTicketTrain = new JLabel("Train Number: —");
            lblTicketClass = new JLabel("Travel Category: —");
            lblTicketDetails = new JLabel("Class Specifics: —");
            lblTicketFare = new JLabel("Total Fare: —");

            Font fontTicket = new Font("Segoe UI", Font.PLAIN, 14);
            lblTicketPnr.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTicketPnr.setForeground(COLOR_PRIMARY_BLUE);
            lblTicketPassenger.setFont(fontTicket);
            lblTicketTrain.setFont(fontTicket);
            lblTicketClass.setFont(fontTicket);
            lblTicketDetails.setFont(fontTicket);
            lblTicketFare.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTicketFare.setForeground(COLOR_SUCCESS_GREEN);

            passDetails.add(lblTicketPnr);
            passDetails.add(lblTicketPassenger);
            passDetails.add(lblTicketTrain);
            passDetails.add(lblTicketClass);
            passDetails.add(lblTicketDetails);
            passDetails.add(lblTicketFare);

            passBody.add(passDetails, BorderLayout.CENTER);

            // Decorative Boarding Pass Barcode graphic drawn with Java2D
            JPanel barcodePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();

                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(5, 10, w - 10, h - 20, 8, 8);

                    // Draw vertical barcode lines
                    g2.setColor(COLOR_TEXT_MAIN);
                    int[] stripeWidths = {2, 4, 1, 3, 2, 5, 1, 4, 2, 3, 1, 2, 4, 2, 1, 3, 4, 2, 1, 5, 2, 3, 1, 4};
                    int x = 20;
                    for (int sw : stripeWidths) {
                        if (x + sw > w - 20) break;
                        g2.fillRect(x, 25, sw, h - 70);
                        x += sw + 3;
                    }

                    // Barcode caption text
                    g2.setFont(new Font("Monospaced", Font.BOLD, 11));
                    g2.setColor(COLOR_TEXT_MUTED);
                    g2.drawString("BOARDING BARCODE", 20, h - 30);
                    g2.dispose();
                }
            };
            barcodePanel.setPreferredSize(new Dimension(170, 180));
            barcodePanel.setOpaque(false);
            passBody.add(barcodePanel, BorderLayout.EAST);

            visualPassCard.add(passBody, BorderLayout.CENTER);

            // Academic Footer Notice
            JLabel lblDisclaimer = new JLabel("<html><center>* College Microproject Demonstration (Practicum 2, 10, 12) — Not an official IRCTC boarding ticket *</center></html>", SwingConstants.CENTER);
            lblDisclaimer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblDisclaimer.setForeground(COLOR_TEXT_MUTED);
            visualPassCard.add(lblDisclaimer, BorderLayout.SOUTH);

            // Tab 2: Raw ASCII Text Receipt
            txtAsciiReceipt = new JTextArea();
            txtAsciiReceipt.setFont(new Font("Monospaced", Font.PLAIN, 12));
            txtAsciiReceipt.setEditable(false);
            txtAsciiReceipt.setBackground(new Color(248, 250, 252));
            txtAsciiReceipt.setBorder(new EmptyBorder(15, 15, 15, 15));
            JScrollPane asciiScroll = new JScrollPane(txtAsciiReceipt);

            tabbedPane.addTab("Visual Boarding Pass", visualPassCard);
            tabbedPane.addTab("Raw File Receipt (PNR_receipt.txt)", asciiScroll);

            JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
            centerContainer.setOpaque(false);
            centerContainer.add(toolbar, BorderLayout.NORTH);
            centerContainer.add(tabbedPane, BorderLayout.CENTER);

            add(centerContainer, BorderLayout.CENTER);

            populatePnrSelector();
        }

        public void populatePnrSelector() {
            cmbPnrSelector.removeAllItems();
            ArrayList<Ticket> all = app.getManager().getAllTickets();
            for (Ticket t : all) {
                cmbPnrSelector.addItem(t.getPnr());
            }
            if (!all.isEmpty()) {
                loadTicketByPNR(all.get(0).getPnr());
            } else {
                clearReceiptView();
            }
        }

        public void loadTicketByPNR(String pnr) {
            Ticket t = app.getManager().findTicketByPNR(pnr);
            if (t != null) {
                cmbPnrSelector.setSelectedItem(pnr);

                lblTicketPnr.setText("PNR Number: " + t.getPnr());
                lblTicketPassenger.setText("Passenger Name: " + t.getPassengerName());
                lblTicketTrain.setText("Train Number: " + t.getTrainNumber());
                lblTicketClass.setText("Ticket Category: " + t.getTicketType());

                String specific = "General Travel";
                if (t instanceof SleeperTicket) {
                    specific = "Allocated Berth: " + ((SleeperTicket) t).getBerthType() + " | Surcharge: Rs. 50.00";
                } else if (t instanceof ACTierTicket) {
                    ACTierTicket at = (ACTierTicket) t;
                    specific = String.format("Tier %dA | Surcharge: Rs. %.2f | GST: 5%%", at.getAcTier(), at.getTierSurcharge());
                } else if (t instanceof GeneralTicket) {
                    specific = "Validity: " + ((GeneralTicket) t).getValidityHours() + " Hours | Service Fee: Rs. 15.00";
                }
                lblTicketDetails.setText("Class Specifics: " + specific);
                lblTicketFare.setText(String.format("TOTAL FARE: Rs. %.2f", t.calculateTotalFare()));

                generateAsciiPreview(t);
            }
        }

        private void clearReceiptView() {
            lblTicketPnr.setText("PNR Number: — (No Reservations)");
            lblTicketPassenger.setText("Passenger Name: —");
            lblTicketTrain.setText("Train Number: —");
            lblTicketClass.setText("Ticket Category: —");
            lblTicketDetails.setText("Class Specifics: —");
            lblTicketFare.setText("TOTAL FARE: —");
            txtAsciiReceipt.setText("No active reservations found to display receipt.");
        }

        private void generateAsciiPreview(Ticket t) {
            StringBuilder sb = new StringBuilder();
            sb.append("================================================================\n");
            sb.append("                 COLLEGE RAILWAY RESERVATION SYSTEM             \n");
            sb.append("                     PASSENGER E-TICKET RECEIPT                 \n");
            sb.append("================================================================\n");
            sb.append(String.format("PNR Number       : %s%n", t.getPnr()));
            sb.append(String.format("Passenger Name   : %s%n", t.getPassengerName()));
            sb.append(String.format("Train Number     : %s%n", t.getTrainNumber()));
            sb.append(String.format("Ticket Category  : %s%n", t.getTicketType()));

            if (t instanceof SleeperTicket) {
                sb.append(String.format("Allocated Berth  : %s%n", ((SleeperTicket) t).getBerthType()));
                sb.append(String.format("Sleeper Charge   : Rs. %.2f%n", ((SleeperTicket) t).getSleeperSurcharge()));
            } else if (t instanceof ACTierTicket) {
                ACTierTicket at = (ACTierTicket) t;
                sb.append(String.format("AC Class Tier    : Tier %d (%dA)%n", at.getAcTier(), at.getAcTier()));
                sb.append(String.format("Tier Surcharge   : Rs. %.2f%n", at.getTierSurcharge()));
                sb.append(String.format("GST Rate Applied : %.0f%%%n", at.getGstRate() * 100));
            } else if (t instanceof GeneralTicket) {
                sb.append(String.format("Ticket Validity  : %d Hours%n", ((GeneralTicket) t).getValidityHours()));
                sb.append(String.format("Service Fee      : Rs. %.2f%n", ((GeneralTicket) t).getGeneralServiceCharge()));
            }

            sb.append("----------------------------------------------------------------\n");
            sb.append(String.format("Base Fare        : Rs. %.2f%n", t.getBaseFare()));
            sb.append(String.format("TOTAL FARE       : Rs. %.2f%n", t.calculateTotalFare()));
            sb.append("----------------------------------------------------------------\n");
            sb.append("Booking Status   : CONFIRMED\n");
            sb.append(String.format("Issued On        : %s%n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            sb.append("================================================================\n");
            sb.append("        * ACADEMIC PROJECT DEMONSTRATION - NOT AN OFFICIAL TICKET *\n");
            sb.append("                  WISH YOU A SAFE AND HAPPY JOURNEY!            \n");
            sb.append("================================================================\n");

            txtAsciiReceipt.setText(sb.toString());
        }

        private void exportReceiptFile() {
            String pnr = (String) cmbPnrSelector.getSelectedItem();
            if (pnr == null || pnr.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No ticket available to export.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            try {
                // Practicum 12: Generates PNR_receipt.txt
                File file = app.getManager().generateReceipt(pnr.trim());
                JOptionPane.showMessageDialog(this,
                        "Receipt saved to file:\n" + file.getAbsolutePath(),
                        "Receipt Exported (Practicum 12)", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void openReceiptInEditor() {
            File receipt = new File("PNR_receipt.txt");
            if (!receipt.exists()) {
                exportReceiptFile();
            }

            if (receipt.exists() && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(receipt);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Could not open file in editor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // =========================================================================
    // CUSTOM UI COMPONENTS (CardPanel, AnimatedSlideCard, ModernButton, Checkmark, Table)
    // =========================================================================

    /**
     * White card container with rounded corners, subtle soft shadow, and border.
     */
    public static class CardPanel extends JPanel {
        private final int cornerRadius = 14;

        public CardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Subtle soft shadow effect
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fillRoundRect(2, 2, getWidth() - 3, getHeight() - 3, cornerRadius, cornerRadius);

            // Card background
            g2.setColor(COLOR_CARD_BG);
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);

            // Card border
            g2.setColor(COLOR_CARD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Animated Slide-in card for ticket confirmation entrance.
     */
    public static class AnimatedSlideCard extends CardPanel {
        private float slideAlpha = 0.0f;
        private int slideOffsetY = 24;
        private Timer slideTimer;

        public AnimatedSlideCard() {
            super();
        }

        public void startSlideIn(Runnable onComplete) {
            if (slideTimer != null && slideTimer.isRunning()) {
                slideTimer.stop();
            }
            slideAlpha = 0.0f;
            slideOffsetY = 24;
            setVisible(true);

            slideTimer = new Timer(15, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    slideAlpha = Math.min(1.0f, slideAlpha + 0.12f);
                    slideOffsetY = Math.max(0, slideOffsetY - 3);
                    repaint();
                    if (slideAlpha >= 1.0f && slideOffsetY <= 0) {
                        slideTimer.stop();
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                }
            });
            slideTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, slideAlpha));
            g2.translate(0, slideOffsetY);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    /**
     * Metric Summary Card for Dashboard.
     */
    public static class StatCard extends CardPanel {
        private final JLabel lblVal;

        public StatCard(String title, String initialValue, Color accentColor) {
            setLayout(new BorderLayout(0, 4));
            setBorder(new EmptyBorder(16, 16, 16, 16));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblTitle.setForeground(COLOR_TEXT_MUTED);

            lblVal = new JLabel(initialValue);
            lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblVal.setForeground(COLOR_TEXT_MAIN);

            JPanel accentStrip = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(accentColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.dispose();
                }
            };
            accentStrip.setPreferredSize(new Dimension(30, 4));
            accentStrip.setOpaque(false);

            add(lblTitle, BorderLayout.NORTH);
            add(lblVal, BorderLayout.CENTER);
            add(accentStrip, BorderLayout.SOUTH);
        }

        public void setValue(String value) {
            lblVal.setText(value);
        }
    }

    /**
     * Modern rounded button with smooth hover animation and press feedback.
     */
    public static class ModernButton extends JButton {
        private final Color baseColor;
        private Color currentColor;

        public ModernButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
            this.currentColor = baseColor;

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Smooth Hover and Pressed feedback
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    currentColor = brighten(baseColor, 0.18f);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    currentColor = baseColor;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    currentColor = darken(baseColor, 0.15f);
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    currentColor = isMouseOver(e) ? brighten(baseColor, 0.18f) : baseColor;
                    repaint();
                }

                private boolean isMouseOver(MouseEvent e) {
                    return contains(e.getPoint());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }

        private static Color brighten(Color c, float factor) {
            int r = Math.min(255, (int) (c.getRed() + (255 - c.getRed()) * factor));
            int g = Math.min(255, (int) (c.getGreen() + (255 - c.getGreen()) * factor));
            int b = Math.min(255, (int) (c.getBlue() + (255 - c.getBlue()) * factor));
            return new Color(r, g, b);
        }

        private static Color darken(Color c, float factor) {
            int r = Math.max(0, (int) (c.getRed() * (1 - factor)));
            int g = Math.max(0, (int) (c.getGreen() * (1 - factor)));
            int b = Math.max(0, (int) (c.getBlue() * (1 - factor)));
            return new Color(r, g, b);
        }
    }

    /**
     * Sidebar Navigation Button with active state indicator.
     */
    public static class SidebarButton extends JButton {
        private boolean isActive = false;
        private boolean isHover = false;
        private final String actionKey;

        public SidebarButton(String text, String actionKey) {
            super(text);
            this.actionKey = actionKey;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(203, 213, 225));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(216, 38));
            setMaximumSize(new Dimension(216, 38));
            setBorder(new EmptyBorder(0, 16, 0, 0));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHover = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
            setForeground(active ? Color.WHITE : new Color(203, 213, 225));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                g2.setColor(COLOR_SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Cyan indicator strip on left
                g2.setColor(COLOR_CYAN_ACCENT);
                g2.fillRoundRect(0, 4, 4, getHeight() - 8, 4, 4);
            } else if (isHover) {
                g2.setColor(COLOR_SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            g2.dispose();
            super.paintComponent(g);
        }

        public String getActionKey() {
            return actionKey;
        }
    }

    /**
     * Animated Success Checkmark using javax.swing.Timer (Circle sweep & checkmark tick).
     */
    public static class AnimatedCheckmark extends JComponent {
        private float progress = 0.0f;
        private Timer animTimer;

        public AnimatedCheckmark() {
            setOpaque(false);
        }

        public void startAnimation() {
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
            progress = 0.0f;
            animTimer = new Timer(18, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    progress += 0.07f;
                    if (progress >= 1.0f) {
                        progress = 1.0f;
                        animTimer.stop();
                    }
                    repaint();
                }
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 6;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setColor(COLOR_SUCCESS_GREEN);
            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Animated circular sweep
            int angle = (int) (Math.min(progress, 0.4f) / 0.4f * 360);
            g2.drawArc(x, y, size, size, 90, -angle);

            // Draw checkmark stroke once circle has completed
            if (progress > 0.4f) {
                float checkProgress = (progress - 0.4f) / 0.6f;
                int p1x = x + (int) (size * 0.28);
                int p1y = y + (int) (size * 0.52);
                int p2x = x + (int) (size * 0.45);
                int p2y = y + (int) (size * 0.70);
                int p3x = x + (int) (size * 0.75);
                int p3y = y + (int) (size * 0.32);

                if (checkProgress < 0.5f) {
                    float sub = checkProgress / 0.5f;
                    int curX = (int) (p1x + (p2x - p1x) * sub);
                    int curY = (int) (p1y + (p2y - p1y) * sub);
                    g2.drawLine(p1x, p1y, curX, curY);
                } else {
                    g2.drawLine(p1x, p1y, p2x, p2y);
                    float sub = (checkProgress - 0.5f) / 0.5f;
                    int curX = (int) (p2x + (p3x - p2x) * sub);
                    int curY = (int) (p2y + (p3y - p2y) * sub);
                    g2.drawLine(p2x, p2y, curX, curY);
                }
            }

            g2.dispose();
        }
    }

    /**
     * Clean rounded border for text fields and combo boxes.
     */
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        public RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(6, 10, 6, 10);
        }
    }

    /**
     * Pre-configured styled JTable with alternating row colors and bold PNR column.
     */
    public static JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(COLOR_TEXT_MAIN);
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));

                if (!isSel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }

                if (col == 0) { // PNR bold
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    setForeground(COLOR_PRIMARY_BLUE);
                } else if ("CONFIRMED".equals(val)) {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    setForeground(COLOR_SUCCESS_GREEN);
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    setForeground(COLOR_TEXT_MAIN);
                }
                return c;
            }
        });

        return table;
    }

    // =========================================================================
    // MAIN LAUNCHER
    // =========================================================================

    public static void main(String[] args) {
        // Enable anti-aliasing for text and 2D graphics
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            TrainReservationApp app = new TrainReservationApp();
            app.setVisible(true);
        });
    }
}
