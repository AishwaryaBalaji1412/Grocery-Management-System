
package com.grocery;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MainUI extends JFrame {

    private GroceryManagement.User currentUser;

    // ---------------- BLUE THEME COLORS ----------------

    private static final Color NAVY =
            new Color(23, 43, 77);

    private static final Color BACKGROUND =
            new Color(234, 242, 250);

    private static final Color BLUE =
            new Color(49, 92, 145);

    private static final Color MEDIUM_BLUE =
            new Color(75, 119, 173);

    private static final Color LIGHT_BLUE =
            new Color(115, 153, 198);

    private static final Color PALE_BLUE =
            new Color(220, 233, 248);

    private static final Color CARD =
            new Color(255, 255, 255);

    private static final Color TEXT =
            new Color(39, 53, 73);

    private static final Color MUTED =
            new Color(105, 121, 141);

    private static final Color BORDER =
            new Color(213, 225, 239);

    private JTable inventoryTable;
    private JTable transactionTable;

    private DefaultTableModel inventoryModel;
    private DefaultTableModel transactionModel;

    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> transactionTypeFilter;
    private JTextField fromDateField;
    private JTextField toDateField;

    private JLabel dashboardItems;
    private JLabel dashboardLowStock;
    private JLabel dashboardValue;

    private boolean updatingCategoryFilter = false;

    public MainUI() {
        showLogin();
    }

    // ---------------- LOGIN ----------------

    private void showLogin() {

        setTitle("Grocery Management System");
        setSize(540, 650);
        setMinimumSize(new Dimension(460, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(BACKGROUND);
        background.setBorder(new EmptyBorder(30, 35, 30, 35));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(35, 45, 35, 45)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 0, 8, 0);

        JLabel logo = new JLabel("GROCERY", SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.BOLD, 31));
        logo.setForeground(NAVY);

        JLabel heading = new JLabel(
                "MANAGEMENT SYSTEM",
                SwingConstants.CENTER
        );
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));
        heading.setForeground(BLUE);

        JLabel subtitle = new JLabel(
                "Inventory & Store Management",
                SwingConstants.CENTER
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER);

        JLabel roleLabel = new JLabel("SELECT LOGIN TYPE");
        roleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        roleLabel.setForeground(NAVY);

        JRadioButton staffOption = new JRadioButton("Staff Login", true);
        JRadioButton adminOption = new JRadioButton("Admin Login");

        styleRadioButton(staffOption);
        styleRadioButton(adminOption);

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(staffOption);
        roleGroup.add(adminOption);

        JPanel rolePanel = new JPanel(new GridLayout(1, 2, 12, 0));
        rolePanel.setBackground(CARD);
        rolePanel.add(staffOption);
        rolePanel.add(adminOption);

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        styleInput(username);
        styleInput(password);

        JLabel usernameLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password");

        styleFormLabel(usernameLabel);
        styleFormLabel(passwordLabel);

        JButton loginButton = createButton("LOGIN", BLUE);
        JButton registerButton = createButton(
                "CREATE STAFF ACCOUNT", NAVY
        );

        gbc.gridy = 0;
        panel.add(logo, gbc);

        gbc.gridy++;
        panel.add(heading, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 0, 17, 0);
        panel.add(subtitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(separator, gbc);

        gbc.gridy++;
        panel.add(roleLabel, gbc);

        gbc.gridy++;
        panel.add(rolePanel, gbc);

        gbc.gridy++;
        panel.add(usernameLabel, gbc);

        gbc.gridy++;
        panel.add(username, gbc);

        gbc.gridy++;
        panel.add(passwordLabel, gbc);

        gbc.gridy++;
        panel.add(password, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(18, 0, 8, 0);
        panel.add(loginButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(registerButton, gbc);

        gbc.gridy++;
        JLabel footer = new JLabel(
                "Secure Inventory Management",
                SwingConstants.CENTER
        );
        footer.setForeground(MUTED);
        footer.setFont(new Font("SansSerif", Font.ITALIC, 11));
        panel.add(footer, gbc);

        background.add(panel);

        loginButton.addActionListener(e -> {

            String user = username.getText().trim();
            char[] passwordChars = password.getPassword();
            String pass = new String(passwordChars);

            try {
                if (user.isEmpty() || pass.isEmpty()) {
                    showError("Enter your username and password.");
                    return;
                }

                GroceryManagement.User authenticatedUser =
                        GroceryManagement.login(user, pass);

                if (authenticatedUser == null) {
                    showError("Invalid username or password.");
                    return;
                }

                boolean selectedAdmin = adminOption.isSelected();
                boolean actualAdmin = authenticatedUser.isAdmin();

                if (selectedAdmin != actualAdmin) {
                    showError(
                            "The selected login type does not match your account role.\n"
                                    + "Please select the correct login option."
                    );
                    return;
                }

                currentUser = authenticatedUser;
                showDashboard();

            } catch (Exception ex) {
                showError(ex.getMessage());

            } finally {
                java.util.Arrays.fill(passwordChars, '\0');
                password.setText("");
            }
        });

        password.addActionListener(e -> loginButton.doClick());

        registerButton.addActionListener(e -> showRegistration());

        setContentPane(background);
        setVisible(true);
    }

    private void styleRadioButton(JRadioButton radio) {
        radio.setBackground(CARD);
        radio.setForeground(TEXT);
        radio.setFont(new Font("SansSerif", Font.BOLD, 13));
        radio.setFocusPainted(false);
        radio.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleFormLabel(JLabel label) {
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(TEXT);
    }

    // ---------------- REGISTRATION ----------------

    private void showRegistration() {

        JTextField name = new JTextField();
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JPasswordField confirmPassword = new JPasswordField();

        styleInput(name);
        styleInput(username);
        styleInput(password);
        styleInput(confirmPassword);

        JPanel form = new JPanel(new GridLayout(0, 1, 7, 7));
        form.setBackground(CARD);
        form.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel nameLabel = new JLabel("Full Name");
        JLabel usernameLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password (minimum 8 characters)");
        JLabel confirmLabel = new JLabel("Confirm Password");

        styleFormLabel(nameLabel);
        styleFormLabel(usernameLabel);
        styleFormLabel(passwordLabel);
        styleFormLabel(confirmLabel);

        form.add(nameLabel);
        form.add(name);
        form.add(usernameLabel);
        form.add(username);
        form.add(passwordLabel);
        form.add(password);
        form.add(confirmLabel);
        form.add(confirmPassword);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Create Staff Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        char[] passwordChars = password.getPassword();
        char[] confirmChars = confirmPassword.getPassword();

        try {
            String pass = new String(passwordChars);
            String confirm = new String(confirmChars);

            if (!pass.equals(confirm)) {
                showError("Passwords do not match.");
                return;
            }

            GroceryManagement.registerUser(
                    name.getText().trim(),
                    username.getText().trim(),
                    pass
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Staff account created successfully!\nYou can now log in.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            showError(ex.getMessage());

        } finally {
            java.util.Arrays.fill(passwordChars, '\0');
            java.util.Arrays.fill(confirmChars, '\0');
            password.setText("");
            confirmPassword.setText("");
        }
    }

    // ---------------- MAIN DASHBOARD ----------------

    private void showDashboard() {

        setTitle("Grocery Management System | " + currentUser.fullName);
        setSize(1200, 780);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(NAVY);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(28, 17, 22, 17));

        JLabel logo = new JLabel("  GROCERY");
        logo.setFont(new Font("SansSerif", Font.BOLD, 24));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcome = new JLabel("  " + currentUser.fullName);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 14));
        welcome.setForeground(PALE_BLUE);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel role = new JLabel("  " + safeRole());
        role.setForeground(LIGHT_BLUE);
        role.setFont(new Font("SansSerif", Font.BOLD, 11));
        role.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setBackground(BACKGROUND);
        tabs.setForeground(NAVY);

        tabs.addTab("Dashboard", createDashboard());
        tabs.addTab("Inventory", createInventory());
        tabs.addTab("Transactions", createTransactions());
        tabs.addTab("Reports", createReports());

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(18));
        sidebar.add(welcome);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(role);
        sidebar.add(Box.createVerticalStrut(42));

        addSidebarButton(sidebar, "Dashboard", () -> tabs.setSelectedIndex(0));
        addSidebarButton(sidebar, "Inventory", () -> tabs.setSelectedIndex(1));
        addSidebarButton(sidebar, "Transactions", () -> tabs.setSelectedIndex(2));
        addSidebarButton(sidebar, "Reports", () -> tabs.setSelectedIndex(3));

        sidebar.add(Box.createVerticalGlue());

        JButton logout = createButton("LOG OUT", MEDIUM_BLUE);
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(185, 42));

        logout.addActionListener(e -> {
            currentUser = null;
            showLogin();
        });

        sidebar.add(logout);

        root.add(sidebar, BorderLayout.WEST);
        root.add(tabs, BorderLayout.CENTER);

        setContentPane(root);

        refreshAll();
        setVisible(true);
    }

    private String safeRole() {
        return currentUser == null || currentUser.role == null
                ? "USER"
                : currentUser.role;
    }

    private void addSidebarButton(
            JPanel sidebar,
            String text,
            Runnable action
    ) {

        JButton button = new JButton(text);

        button.setForeground(Color.WHITE);
        button.setBackground(NAVY);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(185, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> action.run());

        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(7));
    }

    // ---------------- DASHBOARD ----------------

    private JPanel createDashboard() {

        JPanel panel = pagePanel();
        JLabel heading = pageHeading("Dashboard Overview");

        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 16));
        cards.setBackground(BACKGROUND);

        dashboardItems = new JLabel("0");
        dashboardLowStock = new JLabel("0");
        dashboardValue = new JLabel("₹0.00");

        cards.add(createStatCard("Total Products", dashboardItems, BLUE));
        cards.add(createStatCard("Low Stock Items", dashboardLowStock, MEDIUM_BLUE));
        cards.add(createStatCard("Inventory Value", dashboardValue, NAVY));

        JLabel description = new JLabel(
                "Monitor products, stock levels and inventory value."
        );
        description.setForeground(MUTED);
        description.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel content = new JPanel(new BorderLayout(12, 22));
        content.setBackground(BACKGROUND);

        content.add(heading, BorderLayout.NORTH);
        content.add(cards, BorderLayout.CENTER);
        content.add(description, BorderLayout.SOUTH);

        panel.add(content, BorderLayout.NORTH);

        JButton refresh = createButton("REFRESH DATA", BLUE);
        refresh.addActionListener(e -> refreshAll());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(BACKGROUND);
        bottom.add(refresh);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatCard(String title, JLabel value, Color color) {

        JPanel card = new JPanel(new GridLayout(2, 1, 5, 10));
        card.setBackground(CARD);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(22, 20, 22, 20)
        ));

        JLabel heading = new JLabel(title);
        heading.setForeground(MUTED);
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));

        value.setForeground(color);
        value.setFont(new Font("SansSerif", Font.BOLD, 27));

        card.add(heading);
        card.add(value);

        return card;
    }

    // ---------------- INVENTORY ----------------

    private JPanel createInventory() {

        JPanel panel = pagePanel();
        JLabel heading = pageHeading("Grocery Inventory");

        searchField = new JTextField(17);
        styleInput(searchField);

        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        categoryFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));
        categoryFilter.setBackground(CARD);
        categoryFilter.setForeground(TEXT);

        JButton search = createButton("SEARCH", BLUE);
        JButton reset = createButton("RESET", NAVY);
        JButton export = createButton("EXPORT CSV", MEDIUM_BLUE);

        JPanel filters = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 5)
        );

        filters.setBackground(BACKGROUND);
        filters.add(new JLabel("Search:"));
        filters.add(searchField);
        filters.add(new JLabel("Category:"));
        filters.add(categoryFilter);
        filters.add(search);
        filters.add(reset);
        filters.add(export);

        JButton add = createButton("ADD ITEM", NAVY);
        JButton edit = createButton("EDIT", BLUE);
        JButton delete = createButton("DELETE", MEDIUM_BLUE);
        JButton stockIn = createButton("STOCK IN", BLUE);
        JButton stockOut = createButton("STOCK OUT", LIGHT_BLUE);

        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        add.setVisible(isAdmin);
        edit.setVisible(isAdmin);
        delete.setVisible(isAdmin);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 5)
        );

        actions.setBackground(BACKGROUND);
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        actions.add(stockIn);
        actions.add(stockOut);

        inventoryModel = new DefaultTableModel(
                new String[]{
                        "ID", "Item Name", "Category", "Quantity",
                        "Unit", "Price (₹)", "Minimum Stock", "Status"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(inventoryModel);
        styleTable(inventoryTable);
        inventoryTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JPanel top = new JPanel(new BorderLayout(5, 7));
        top.setBackground(BACKGROUND);
        top.add(heading, BorderLayout.NORTH);
        top.add(filters, BorderLayout.CENTER);
        top.add(actions, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        search.addActionListener(e -> applyInventoryFilters());
        searchField.addActionListener(e -> applyInventoryFilters());

        categoryFilter.addActionListener(e -> {
            if (!updatingCategoryFilter) {
                applyInventoryFilters();
            }
        });

        reset.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            refreshInventory();
        });

        export.addActionListener(
                e -> exportTable(inventoryTable, "inventory.csv")
        );

        add.addActionListener(e -> addItemDialog());
        edit.addActionListener(e -> editItemDialog());
        delete.addActionListener(e -> deleteItem());

        stockIn.addActionListener(e -> stockDialog("STOCK_IN"));
        stockOut.addActionListener(e -> stockDialog("STOCK_OUT"));

        return panel;
    }

    private void applyInventoryFilters() {

        try {
            String keyword = searchField.getText().trim();
            String category = (String) categoryFilter.getSelectedItem();

            List<GroceryManagement.GroceryItem> items =
                    new ArrayList<>(
                            keyword.isEmpty()
                                    ? GroceryManagement.getAllItems()
                                    : GroceryManagement.searchItems(keyword)
                    );

            if (category != null
                    && !category.equals("All Categories")) {
                items.removeIf(
                        item -> !category.equalsIgnoreCase(item.category)
                );
            }

            displayInventory(items);

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshCategoryFilter() {

        if (categoryFilter == null) {
            return;
        }

        String selected = (String) categoryFilter.getSelectedItem();

        updatingCategoryFilter = true;

        try {
            categoryFilter.removeAllItems();
            categoryFilter.addItem("All Categories");

            for (String category : GroceryManagement.getCategories()) {
                categoryFilter.addItem(category);
            }

            if (selected != null) {
                categoryFilter.setSelectedItem(selected);
            }

        } catch (Exception ex) {
            showError(ex.getMessage());

        } finally {
            updatingCategoryFilter = false;
        }
    }

    // ---------------- ADD / EDIT ITEM ----------------

    private void addItemDialog() {

        JTextField name = new JTextField();
        JTextField category = new JTextField();
        JTextField quantity = new JTextField();
        JTextField unit = new JTextField("pcs");
        JTextField price = new JTextField();
        JTextField minimum = new JTextField("5");

        styleInput(name);
        styleInput(category);
        styleInput(quantity);
        styleInput(unit);
        styleInput(price);
        styleInput(minimum);

        JPanel form = createItemForm(
                name, category, quantity, unit, price, minimum
        );

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Add Grocery Item",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            GroceryManagement.addItem(
                    name.getText().trim(),
                    category.getText().trim(),
                    Integer.parseInt(quantity.getText().trim()),
                    unit.getText().trim(),
                    new BigDecimal(price.getText().trim()),
                    Integer.parseInt(minimum.getText().trim()),
                    currentUser.userId
            );

            showInfo("Item added successfully.");
            refreshAll();

        } catch (NumberFormatException ex) {
            showError(
                    "Enter valid numbers for quantity, price and minimum stock."
            );

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void editItemDialog() {

        GroceryManagement.GroceryItem item = getSelectedItem();

        if (item == null) {
            return;
        }

        JTextField name = new JTextField(item.itemName);
        JTextField category = new JTextField(item.category);
        JTextField quantity = new JTextField(String.valueOf(item.quantity));
        JTextField unit = new JTextField(item.unit);
        JTextField price = new JTextField(item.price.toPlainString());
        JTextField minimum = new JTextField(String.valueOf(item.minStock));

        styleInput(name);
        styleInput(category);
        styleInput(quantity);
        styleInput(unit);
        styleInput(price);
        styleInput(minimum);

        quantity.setEditable(false);
        quantity.setBackground(BACKGROUND);

        JPanel form = createItemForm(
                name, category, quantity, unit, price, minimum
        );

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Edit Grocery Item",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            GroceryManagement.updateItem(
                    item.itemId,
                    name.getText().trim(),
                    category.getText().trim(),
                    unit.getText().trim(),
                    new BigDecimal(price.getText().trim()),
                    Integer.parseInt(minimum.getText().trim()),
                    currentUser.userId
            );

            showInfo("Item updated successfully.");
            refreshAll();

        } catch (NumberFormatException ex) {
            showError("Enter a valid price and minimum stock.");

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private JPanel createItemForm(
            JTextField name,
            JTextField category,
            JTextField quantity,
            JTextField unit,
            JTextField price,
            JTextField minimum
    ) {

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBackground(CARD);
        form.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel nameLabel = new JLabel("Item Name");
        JLabel categoryLabel = new JLabel("Category");
        JLabel quantityLabel = new JLabel("Quantity");
        JLabel unitLabel = new JLabel("Unit (kg, litre, pcs, etc.)");
        JLabel priceLabel = new JLabel("Price per Unit (₹)");
        JLabel minimumLabel = new JLabel("Minimum Stock Alert");

        styleFormLabel(nameLabel);
        styleFormLabel(categoryLabel);
        styleFormLabel(quantityLabel);
        styleFormLabel(unitLabel);
        styleFormLabel(priceLabel);
        styleFormLabel(minimumLabel);

        form.add(nameLabel);
        form.add(name);

        form.add(categoryLabel);
        form.add(category);

        form.add(quantityLabel);
        form.add(quantity);

        form.add(unitLabel);
        form.add(unit);

        form.add(priceLabel);
        form.add(price);

        form.add(minimumLabel);
        form.add(minimum);

        return form;
    }

    // ---------------- DELETE / STOCK ----------------

    private void deleteItem() {

        GroceryManagement.GroceryItem item = getSelectedItem();

        if (item == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete '" + item.itemName + "'?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            GroceryManagement.deleteItem(
                    item.itemId,
                    currentUser.userId
            );

            showInfo("Item deleted successfully.");
            refreshAll();

        } catch (Exception ex) {
            showError(
                    "Could not delete this item. It may have transaction history.\n"
                            + ex.getMessage()
            );
        }
    }

    private void stockDialog(String type) {

        GroceryManagement.GroceryItem item = getSelectedItem();

        if (item == null) {
            return;
        }

        JTextField amount = new JTextField();
        JTextField notes = new JTextField();

        styleInput(amount);
        styleInput(notes);

        JPanel form = new JPanel(new GridLayout(0, 1, 7, 7));
        form.setBackground(CARD);
        form.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel itemLabel = new JLabel("Item: " + item.itemName);
        JLabel quantityLabel = new JLabel(
                "Current Quantity: " + item.quantity + " " + item.unit
        );
        JLabel amountLabel = new JLabel(
                "Quantity to " +
                        (type.equals("STOCK_IN") ? "add" : "remove")
        );
        JLabel notesLabel = new JLabel("Notes (optional)");

        styleFormLabel(itemLabel);
        styleFormLabel(quantityLabel);
        styleFormLabel(amountLabel);
        styleFormLabel(notesLabel);

        form.add(itemLabel);
        form.add(quantityLabel);
        form.add(amountLabel);
        form.add(amount);
        form.add(notesLabel);
        form.add(notes);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                type.equals("STOCK_IN") ? "Stock In" : "Stock Out",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int quantity = Integer.parseInt(amount.getText().trim());

            GroceryManagement.adjustStock(
                    item.itemId,
                    currentUser.userId,
                    type,
                    quantity,
                    notes.getText().trim()
            );

            showInfo("Stock updated successfully.");
            refreshAll();

        } catch (NumberFormatException ex) {
            showError("Enter a whole number for the stock quantity.");

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ---------------- TRANSACTIONS ----------------

    private JPanel createTransactions() {

        JPanel panel = pagePanel();
        JLabel heading = pageHeading("Stock Transaction History");

        transactionTypeFilter = new JComboBox<>(
                new String[]{"ALL", "STOCK_IN", "STOCK_OUT"}
        );
        transactionTypeFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));
        transactionTypeFilter.setBackground(CARD);
        transactionTypeFilter.setForeground(TEXT);

        fromDateField = new JTextField(10);
        toDateField = new JTextField(10);

        styleInput(fromDateField);
        styleInput(toDateField);

        fromDateField.setToolTipText("Optional start date: yyyy-MM-dd");
        toDateField.setToolTipText("Optional end date: yyyy-MM-dd");

        JButton filter = createButton("APPLY FILTER", BLUE);
        JButton clear = createButton("CLEAR", NAVY);
        JButton export = createButton("EXPORT CSV", MEDIUM_BLUE);

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 5)
        );

        controls.setBackground(BACKGROUND);
        controls.add(new JLabel("Type:"));
        controls.add(transactionTypeFilter);
        controls.add(new JLabel("From (yyyy-MM-dd):"));
        controls.add(fromDateField);
        controls.add(new JLabel("To:"));
        controls.add(toDateField);
        controls.add(filter);
        controls.add(clear);
        controls.add(export);

        transactionModel = new DefaultTableModel(
                new String[]{
                        "ID", "Item", "User", "Type",
                        "Quantity", "Notes", "Date"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(transactionModel);
        styleTable(transactionTable);
        transactionTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JPanel top = new JPanel(new BorderLayout(5, 7));
        top.setBackground(BACKGROUND);
        top.add(heading, BorderLayout.NORTH);
        top.add(controls, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        filter.addActionListener(e -> filterTransactions());
        fromDateField.addActionListener(e -> filterTransactions());
        toDateField.addActionListener(e -> filterTransactions());

        clear.addActionListener(e -> {
            fromDateField.setText("");
            toDateField.setText("");
            transactionTypeFilter.setSelectedIndex(0);
            refreshTransactions();
        });

        export.addActionListener(
                e -> exportTable(transactionTable, "transactions.csv")
        );

        return panel;
    }

    private void filterTransactions() {

        try {
            Date from = parseOptionalDate(fromDateField.getText().trim());
            Date to = parseOptionalDate(toDateField.getText().trim());

            if (from != null && to != null && from.after(to)) {
                showError("Start date cannot be after end date.");
                return;
            }

            String type = (String) transactionTypeFilter.getSelectedItem();

            if (type == null || type.equals("ALL")) {
                type = null;
            }

            loadTransactions(
                    GroceryManagement.getTransactionHistory(from, to, type)
            );

        } catch (DateTimeParseException ex) {
            showError(
                    "Enter dates in yyyy-MM-dd format, for example 2026-11-18."
            );

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private Date parseOptionalDate(String value) {

        if (value.isEmpty()) {
            return null;
        }

        return Date.valueOf(LocalDate.parse(value));
    }

    // ---------------- REPORTS ----------------

    private JPanel createReports() {

        JPanel panel = pagePanel();
        JLabel heading = pageHeading("Inventory Reports");

        JTextArea report = new JTextArea();

        report.setFont(new Font("Monospaced", Font.PLAIN, 13));
        report.setEditable(false);
        report.setMargin(new Insets(15, 15, 15, 15));
        report.setBackground(CARD);
        report.setForeground(TEXT);
        report.setCaretColor(BLUE);
        report.setLineWrap(false);

        JButton generate = createButton("GENERATE REPORT", BLUE);
        JButton save = createButton("SAVE REPORT", NAVY);

        generate.addActionListener(e -> {
            try {
                report.setText(buildReport());
                report.setCaretPosition(0);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        save.addActionListener(e -> {
            if (report.getText().isBlank()) {
                showError("Generate a report before saving it.");
                return;
            }

            saveTextFile(
                    report.getText(),
                    "grocery-inventory-report.txt"
            );
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackground(BACKGROUND);
        buttons.add(generate);
        buttons.add(save);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BACKGROUND);
        top.add(heading, BorderLayout.WEST);
        top.add(buttons, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(report), BorderLayout.CENTER);

        return panel;
    }

    private String buildReport() throws Exception {

        List<GroceryManagement.GroceryItem> items =
                GroceryManagement.getAllItems();

        List<GroceryManagement.GroceryItem> lowStock =
                GroceryManagement.getLowStockItems();

        StringBuilder output = new StringBuilder();

        output.append("GROCERY INVENTORY REPORT\n");
        output.append("Generated: ")
                .append(java.time.LocalDateTime.now())
                .append("\n");

        output.append(
                "============================================================\n\n"
        );

        output.append("Total Products: ")
                .append(GroceryManagement.getTotalItemCount())
                .append("\n");

        output.append("Low Stock Products: ")
                .append(lowStock.size())
                .append("\n");

        output.append("Inventory Value: ₹")
                .append(formatMoney(GroceryManagement.getInventoryValue()))
                .append("\n\n");

        output.append("LOW STOCK ITEMS\n");
        output.append("------------------------------\n");

        if (lowStock.isEmpty()) {
            output.append("No low-stock items.\n");
        }

        for (GroceryManagement.GroceryItem item : lowStock) {
            output.append(item.itemName)
                    .append(" | Quantity: ")
                    .append(item.quantity)
                    .append(' ')
                    .append(item.unit)
                    .append(" | Minimum: ")
                    .append(item.minStock)
                    .append('\n');
        }

        output.append("\nCOMPLETE INVENTORY\n");
        output.append("------------------------------\n");

        for (GroceryManagement.GroceryItem item : items) {
            output.append(item.itemName)
                    .append(" | ")
                    .append(item.category)
                    .append(" | Quantity: ")
                    .append(item.quantity)
                    .append(' ')
                    .append(item.unit)
                    .append(" | Price: ₹")
                    .append(formatMoney(item.price))
                    .append('\n');
        }

        return output.toString();
    }

    // ---------------- DATA REFRESH ----------------

    private void refreshAll() {
        refreshDashboard();
        refreshCategoryFilter();
        refreshInventory();
        refreshTransactions();
    }

    private void refreshDashboard() {

        try {
            if (dashboardItems != null) {
                dashboardItems.setText(
                        String.valueOf(GroceryManagement.getTotalItemCount())
                );
            }

            if (dashboardLowStock != null) {
                dashboardLowStock.setText(
                        String.valueOf(GroceryManagement.getLowStockCount())
                );
            }

            if (dashboardValue != null) {
                dashboardValue.setText(
                        "₹" + formatMoney(GroceryManagement.getInventoryValue())
                );
            }

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshInventory() {
        try {
            displayInventory(GroceryManagement.getAllItems());
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void displayInventory(
            List<GroceryManagement.GroceryItem> items
    ) {

        if (inventoryModel == null) {
            return;
        }

        inventoryModel.setRowCount(0);

        for (GroceryManagement.GroceryItem item : items) {

            String status = item.quantity <= item.minStock
                    ? "LOW STOCK"
                    : "IN STOCK";

            inventoryModel.addRow(new Object[]{
                    item.itemId,
                    item.itemName,
                    item.category,
                    item.quantity,
                    item.unit,
                    formatMoney(item.price),
                    item.minStock,
                    status
            });
        }
    }

    private void refreshTransactions() {
        try {
            loadTransactions(
                    GroceryManagement.getTransactionHistory()
            );
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void loadTransactions(List<String[]> rows) {

        if (transactionModel == null) {
            return;
        }

        transactionModel.setRowCount(0);

        for (String[] row : rows) {
            transactionModel.addRow(row);
        }
    }

    private GroceryManagement.GroceryItem getSelectedItem() {

        int selectedRow = inventoryTable.getSelectedRow();

        if (selectedRow == -1) {
            showError("Please select an item from the inventory table.");
            return null;
        }

        int modelRow = inventoryTable.convertRowIndexToModel(selectedRow);

        int itemId = Integer.parseInt(
                inventoryModel.getValueAt(modelRow, 0).toString()
        );

        try {
            for (GroceryManagement.GroceryItem item :
                    GroceryManagement.getAllItems()) {

                if (item.itemId == itemId) {
                    return item;
                }
            }

        } catch (Exception ex) {
            showError(ex.getMessage());
        }

        showError(
                "The selected item could not be found. Refresh the inventory and try again."
        );

        return null;
    }

    // ---------------- CSV / FILE EXPORT ----------------

    private void exportTable(JTable table, String suggestedName) {

        if (table == null || table.getRowCount() == 0) {
            showError("There is no data to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(suggestedName));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();

        if (!path.toString().toLowerCase().endsWith(".csv")) {
            path = Path.of(path.toString() + ".csv");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8
        )) {

            for (int column = 0;
                 column < table.getColumnCount();
                 column++) {

                if (column > 0) {
                    writer.write(',');
                }

                writer.write(csv(table.getColumnName(column)));
            }

            writer.newLine();

            for (int row = 0; row < table.getRowCount(); row++) {
                for (int column = 0;
                     column < table.getColumnCount();
                     column++) {

                    if (column > 0) {
                        writer.write(',');
                    }

                    writer.write(
                            csv(String.valueOf(
                                    table.getValueAt(row, column)
                            ))
                    );
                }

                writer.newLine();
            }

            showInfo(
                    "CSV file saved successfully:\n"
                            + path.toAbsolutePath()
            );

        } catch (IOException ex) {
            showError("Could not save the CSV file: " + ex.getMessage());
        }
    }

    private String csv(String value) {

        String safe = value == null ? "" : value;

        // Prevent spreadsheet formula execution for untrusted text.
        if (!safe.isEmpty()
                && "=+-@".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }

        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private void saveTextFile(String content, String suggestedName) {

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(suggestedName));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();

        try {
            Files.writeString(
                    path,
                    content,
                    StandardCharsets.UTF_8
            );

            showInfo(
                    "Report saved successfully:\n"
                            + path.toAbsolutePath()
            );

        } catch (IOException ex) {
            showError("Could not save the report: " + ex.getMessage());
        }
    }

    // ---------------- UI HELPERS ----------------

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private JPanel pagePanel() {

        JPanel panel = new JPanel(new BorderLayout(12, 15));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(22, 22, 22, 22));

        return panel;
    }

    private JLabel pageHeading(String text) {

        JLabel heading = new JLabel(text);
        heading.setFont(new Font("SansSerif", Font.BOLD, 25));
        heading.setForeground(NAVY);

        return heading;
    }

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(130, 39));

        return button;
    }

    private void styleInput(JComponent input) {

        input.setFont(new Font("SansSerif", Font.PLAIN, 14));
        input.setBackground(Color.WHITE);
        input.setForeground(TEXT);

        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleTable(JTable table) {

        table.setRowHeight(34);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setBackground(CARD);
        table.setForeground(TEXT);

        table.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);

        table.setSelectionBackground(PALE_BLUE);
        table.setSelectionForeground(NAVY);
        table.setGridColor(BORDER);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message == null || message.isBlank()
                        ? "An unexpected error occurred."
                        : message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
