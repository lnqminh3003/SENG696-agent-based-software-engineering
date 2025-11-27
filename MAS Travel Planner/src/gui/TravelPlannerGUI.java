package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import agents.GUIUserAgent;

public class TravelPlannerGUI extends JFrame {
    // Components
    private JTextField destinationField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField budgetField;
    private JTextArea resultsArea;
    private JButton searchButton;
    private JButton bookButton;
    private JComboBox<String> planSelector;
    private JPanel mainPanel;

    // Colors
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);

    private GUIUserAgent userAgent; // Reference to the agent

    public TravelPlannerGUI() {
        setupGUI();
    }

    // Constructor with agent
    public TravelPlannerGUI(GUIUserAgent agent) {
        this.userAgent = agent;
        setupGUI();
    }

    private void setupGUI() {
        setTitle("✈️ Travel Planner System");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with gradient background
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Input panel
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.WEST);

        // Results panel
        JPanel resultsPanel = createResultsPanel();
        mainPanel.add(resultsPanel, BorderLayout.CENTER);

        // Footer with status
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("✈️ Travel Planner Multi-Agent System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Plan your perfect trip with AI-powered recommendations");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(236, 240, 241));

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitleLabel);

        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setPreferredSize(new Dimension(350, 0));

        // Title
        JLabel inputTitle = new JLabel("Search Criteria");
        inputTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        inputTitle.setForeground(TEXT_COLOR);
        inputTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(inputTitle);
        panel.add(Box.createVerticalStrut(20));

        // Destination
        panel.add(createFieldPanel("Destination:", destinationField = new JTextField()));
        panel.add(Box.createVerticalStrut(15));

        // Start Date
        panel.add(createFieldPanel("Start Date (YYYY-MM-DD):", startDateField = new JTextField()));
        panel.add(Box.createVerticalStrut(15));

        // End Date
        panel.add(createFieldPanel("End Date (YYYY-MM-DD):", endDateField = new JTextField()));
        panel.add(Box.createVerticalStrut(15));

        // Budget
        panel.add(createFieldPanel("Budget ($):", budgetField = new JTextField()));
        panel.add(Box.createVerticalStrut(25));

        // Search Button
        searchButton = new JButton("🔍 Search Travel Plans");
        styleButton(searchButton, PRIMARY_COLOR);
        searchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchButton.addActionListener(e -> handleSearch());
        panel.add(searchButton);

        panel.add(Box.createVerticalStrut(15));

        // Plan Selector
        JLabel planLabel = new JLabel("Select Plan:");
        planLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        planLabel.setForeground(TEXT_COLOR);
        planLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(planLabel);
        panel.add(Box.createVerticalStrut(5));

        planSelector = new JComboBox<>(new String[]{"Select a plan..."});
        planSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        planSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(planSelector);

        panel.add(Box.createVerticalStrut(15));

        // Book Button
        bookButton = new JButton("💳 Book Selected Plan");
        styleButton(bookButton, SECONDARY_COLOR);
        bookButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookButton.setEnabled(false);
        bookButton.addActionListener(e -> handleBooking());
        panel.add(bookButton);

        return panel;
    }

    private JPanel createFieldPanel(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel resultsTitle = new JLabel("Available Travel Plans");
        resultsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        resultsTitle.setForeground(TEXT_COLOR);
        panel.add(resultsTitle, BorderLayout.NORTH);

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        resultsArea.setBackground(new Color(250, 250, 250));
        resultsArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        resultsArea.setText("Search for travel plans to see results here...\n\n" +
                "📍 Enter your destination\n" +
                "📅 Select your travel dates\n" +
                "💰 Set your budget\n" +
                "🔍 Click Search!");

        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setBorder(new LineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel statusLabel = new JLabel("Ready to search | Powered by JADE Multi-Agent System");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_COLOR);

        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    private void handleSearch() {
        String destination = destinationField.getText().trim();
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();
        String budgetStr = budgetField.getText().trim();

        // Validation
        if (destination.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || budgetStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double budget = Double.parseDouble(budgetStr);
            if (budget <= 0) {
                throw new NumberFormatException();
            }

            // Show loading
            resultsArea.setText("🔄 Searching for travel plans...\n\n" +
                    "Destination: " + destination + "\n" +
                    "Dates: " + startDate + " to " + endDate + "\n" +
                    "Budget: $" + budget + "\n\n" +
                    "Please wait...");

            // Send to agent if connected, otherwise simulate
            if (userAgent != null) {
                userAgent.sendSearchRequest(destination, startDate, endDate, budget);
            } else {
                simulateSearch(destination, startDate, endDate, budget);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid budget amount!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simulateSearch(String dest, String start, String end, double budget) {
        // Simulate API call delay
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                SwingUtilities.invokeLater(() -> {
                    displayResults(dest, budget);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void displayResults(String destination, double budget) {
        StringBuilder results = new StringBuilder();
        results.append("=== SEARCH RESULTS ===\n\n");
        results.append("✈️ Destination: ").append(destination).append("\n\n");

        results.append("Plan #1: Budget Option\n");
        results.append("  Transport: Bus ($85.00)\n");
        results.append("  Hotel: Budget Inn ($240.00)\n");
        results.append("  TOTAL: $325.00\n\n");

        results.append("Plan #2: Mid-Range Option\n");
        results.append("  Transport: Train ($180.00)\n");
        results.append("  Hotel: Mid-Range Hotel ($360.00)\n");
        results.append("  TOTAL: $540.00\n\n");

        results.append("Plan #3: Premium Option\n");
        results.append("  Transport: Flight ($350.00)\n");
        results.append("  Hotel: Luxury Hotel ($540.00)\n");
        results.append("  TOTAL: $890.00\n\n");

        results.append("✅ 3 plans found within your budget!");

        resultsArea.setText(results.toString());

        // Update plan selector
        planSelector.removeAllItems();
        planSelector.addItem("Plan #1 - Budget ($325.00)");
        planSelector.addItem("Plan #2 - Mid-Range ($540.00)");
        planSelector.addItem("Plan #3 - Premium ($890.00)");

        bookButton.setEnabled(true);
    }

    private void handleBooking() {
        int selectedIndex = planSelector.getSelectedIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a plan to book!",
                    "No Plan Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedPlan = (String) planSelector.getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Book " + selectedPlan + "?",
                "Confirm Booking",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            showPaymentDialog(selectedPlan);
        }
    }

    private void showPaymentDialog(String plan) {
        JDialog paymentDialog = new JDialog(this, "Payment Details", true);
        paymentDialog.setSize(500, 450);
        paymentDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // Payment form fields
        JTextField emailField = new JTextField();
        JTextField cardField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField cvvField = new JTextField();
        JTextField expiryField = new JTextField();

        panel.add(new JLabel("Plan: " + plan));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createFieldPanel("Email:", emailField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createFieldPanel("Card Number:", cardField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createFieldPanel("Cardholder Name:", nameField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createFieldPanel("CVV:", cvvField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createFieldPanel("Expiry (MM/YY):", expiryField));
        panel.add(Box.createVerticalStrut(20));

        JButton confirmButton = new JButton("💳 Confirm Payment");
        styleButton(confirmButton, PRIMARY_COLOR);
        confirmButton.addActionListener(e -> {
            paymentDialog.dispose();

            // Collect input values
            String email = emailField.getText().trim();
            String cardHolder = nameField.getText().trim();
            String paymentMethod = cardField.getText().trim(); // or a dropdown for card type

            int selectedIndex = planSelector.getSelectedIndex();
            if (userAgent != null && selectedIndex >= 0) {
                // Call agent method to send payment request
                userAgent.sendBookingRequest(selectedIndex, paymentMethod, cardHolder, email);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error: Agent not connected or plan not selected",
                        "Booking Failed",
                        JOptionPane.ERROR_MESSAGE);
            }

            paymentDialog.dispose();
            showConfirmation();
        });

        panel.add(confirmButton);

        paymentDialog.add(panel);
        paymentDialog.setVisible(true);
    }

    private void showConfirmation() {
        String confirmation = "PAY-" + System.currentTimeMillis();
        String booking = "BK" + (int)(Math.random() * 90000 + 10000);

        JOptionPane.showMessageDialog(this,
                "✅ Payment Successful!\n\n" +
                        "Payment ID: " + confirmation + "\n" +
                        "Booking Reference: " + booking + "\n\n" +
                        "Confirmation email sent!",
                "Booking Confirmed",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new TravelPlannerGUI());
    }

    // Method to connect with GUIUserAgent
    public void setUserAgent(GUIUserAgent agent) {
        this.userAgent = agent;
    }

    // Method to send search request to agent
    public void sendSearchToAgent(String destination, String startDate, String endDate, double budget) {
        if (userAgent != null) {
            userAgent.sendSearchRequest(destination, startDate, endDate, budget);
        }
    }

    // Method called by agent to update results
    public void updateResults(String resultsText) {
        SwingUtilities.invokeLater(() -> {
            resultsArea.setText(resultsText);
        });
    }

    // Method called by agent to update plan selector
    public void updatePlanSelector(String[] plans) {
        SwingUtilities.invokeLater(() -> {
            planSelector.removeAllItems();
            for (String plan : plans) {
                planSelector.addItem(plan);
            }
            bookButton.setEnabled(plans.length > 0);
        });
    }
}
