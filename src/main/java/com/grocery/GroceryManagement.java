
package com.grocery;

import java.sql.*;
import java.util.*;
import java.security.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class GroceryManagement {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final BigDecimal MAX_PRICE =
            new BigDecimal("99999999.99");

    // ---------------- USER MODEL ----------------

    public static class User {
        public int userId;
        public String fullName;
        public String username;
        public String role;

        public User(int userId, String fullName, String username, String role) {
            this.userId = userId;
            this.fullName = fullName;
            this.username = username;
            this.role = role;
        }

        public boolean isAdmin() {
            return "ADMIN".equalsIgnoreCase(role);
        }
    }

    // ---------------- GROCERY ITEM MODEL ----------------

    public static class GroceryItem {
        public int itemId;
        public String itemName;
        public String category;
        public int quantity;
        public String unit;
        public BigDecimal price;
        public int minStock;

        public GroceryItem(int itemId, String itemName, String category,
                           int quantity, String unit, BigDecimal price,
                           int minStock) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.category = category;
            this.quantity = quantity;
            this.unit = unit;
            this.price = price;
            this.minStock = minStock;
        }
    }

    // ---------------- PASSWORD SECURITY ----------------

    static String hashPassword(String password)
            throws GeneralSecurityException {

        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, ITERATIONS, KEY_LENGTH
        );

        try {
            byte[] hash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

            return ITERATIONS + ":"
                    + Base64.getEncoder().encodeToString(salt) + ":"
                    + Base64.getEncoder().encodeToString(hash);
        } finally {
            spec.clearPassword();
        }
    }

    static boolean verifyPassword(String password, String storedHash)
            throws GeneralSecurityException {

        if (password == null || storedHash == null) {
            return false;
        }

        try {
            String[] parts = storedHash.split(":");

            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);

            if (iterations < 10000 || iterations > 1000000) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] originalHash = Base64.getDecoder().decode(parts[2]);

            if (salt.length < 8 || salt.length > 64
                    || originalHash.length < 16
                    || originalHash.length > 64) {
                return false;
            }

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    iterations,
                    originalHash.length * 8
            );

            try {
                byte[] enteredHash = SecretKeyFactory
                        .getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec)
                        .getEncoded();

                return MessageDigest.isEqual(originalHash, enteredHash);
            } finally {
                spec.clearPassword();
            }

        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    // ---------------- USER REGISTRATION ----------------

    public static void registerUser(String fullName, String username,
                                    String password)
            throws SQLException, GeneralSecurityException {

        if (fullName == null || fullName.isBlank()
                || username == null || username.isBlank()
                || password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Enter all details. Password must contain at least 8 characters."
            );
        }

        String cleanName = fullName.trim();
        String cleanUsername = username.trim();

        if (cleanName.length() > 100) {
            throw new IllegalArgumentException(
                    "Full name cannot exceed 100 characters."
            );
        }

        if (cleanUsername.length() < 3 || cleanUsername.length() > 50
                || !cleanUsername.matches("[a-zA-Z0-9_.-]+")) {
            throw new IllegalArgumentException(
                    "Username must be 3–50 characters and contain only letters, numbers, dots, underscores or hyphens."
            );
        }

        if (password.length() > 128) {
            throw new IllegalArgumentException(
                    "Password cannot exceed 128 characters."
            );
        }

        String sql = """
                INSERT INTO users (full_name, username, password_hash, role)
                VALUES (?, ?, ?, 'STAFF')
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cleanName);
            ps.setString(2, cleanUsername);
            ps.setString(3, hashPassword(password));
            ps.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new SQLException(
                    "This username is already registered. Please choose another.",
                    ex
            );
        }
    }

    // ---------------- USER LOGIN ----------------

    public static User login(String username, String password)
            throws SQLException, GeneralSecurityException {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {
            return null;
        }

        String sql = """
                SELECT user_id, full_name, username, password_hash, role
                FROM users
                WHERE username = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && verifyPassword(
                        password, rs.getString("password_hash"))) {

                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("username"),
                            rs.getString("role")
                    );
                }
            }
        }

        return null;
    }

    // ---------------- ROLE AUTHORIZATION ----------------

    private static void requireRole(Connection con, int userId,
                                    String... allowedRoles)
            throws SQLException {

        if (userId <= 0) {
            throw new SecurityException(
                    "Please log in to perform this action."
            );
        }

        String sql = "SELECT role FROM users WHERE user_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SecurityException("Invalid user account.");
                }

                String role = rs.getString("role");

                for (String allowedRole : allowedRoles) {
                    if (allowedRole.equalsIgnoreCase(role)) {
                        return;
                    }
                }

                throw new SecurityException(
                        "You do not have permission to perform this action."
                );
            }
        }
    }

    // ---------------- ADD ITEM ----------------

    public static void addItem(String name, String category, int quantity,
                               String unit, BigDecimal price, int minStock,
                               int userId) throws SQLException {

        validateItem(name, category, quantity, unit, price, minStock);

        String sql = """
                INSERT INTO grocery_items
                (item_name, category, quantity, unit, price, min_stock)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                requireRole(con, userId, "ADMIN");

                int itemId;

                try (PreparedStatement ps = con.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, name.trim());
                    ps.setString(2, category.trim());
                    ps.setInt(3, quantity);
                    ps.setString(4, unit.trim());
                    ps.setBigDecimal(5, normalizePrice(price));
                    ps.setInt(6, minStock);

                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Could not retrieve item ID.");
                        }
                        itemId = keys.getInt(1);
                    }
                }

                if (quantity > 0) {
                    recordTransaction(
                            con, itemId, userId,
                            "STOCK_IN", quantity, "Initial stock"
                    );
                }

                con.commit();

            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                throw ex;
            }
        }
    }

    // ---------------- VIEW ALL ITEMS ----------------

    public static List<GroceryItem> getAllItems() throws SQLException {
        List<GroceryItem> items = new ArrayList<>();

        String sql = """
                SELECT item_id, item_name, category, quantity,
                       unit, price, min_stock
                FROM grocery_items
                ORDER BY item_name
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(readItem(rs));
            }
        }

        return items;
    }

    // ---------------- SEARCH ITEMS ----------------

    public static List<GroceryItem> searchItems(String keyword)
            throws SQLException {

        if (keyword == null || keyword.isBlank()) {
            return getAllItems();
        }

        List<GroceryItem> items = new ArrayList<>();

        String sql = """
                SELECT item_id, item_name, category, quantity,
                       unit, price, min_stock
                FROM grocery_items
                WHERE item_name LIKE ? OR category LIKE ?
                ORDER BY item_name
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String search = "%" + keyword.trim() + "%";
            ps.setString(1, search);
            ps.setString(2, search);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(readItem(rs));
                }
            }
        }

        return items;
    }

    // ---------------- CATEGORY FILTER ----------------

    public static List<String> getCategories() throws SQLException {
        List<String> categories = new ArrayList<>();

        String sql = """
                SELECT DISTINCT category
                FROM grocery_items
                ORDER BY category
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        }

        return categories;
    }

    public static List<GroceryItem> filterItemsByCategory(String category)
            throws SQLException {

        if (category == null || category.isBlank()
                || category.equalsIgnoreCase("All")
                || category.equalsIgnoreCase("All Categories")) {
            return getAllItems();
        }

        List<GroceryItem> items = new ArrayList<>();

        String sql = """
                SELECT item_id, item_name, category, quantity,
                       unit, price, min_stock
                FROM grocery_items
                WHERE category = ?
                ORDER BY item_name
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, category.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(readItem(rs));
                }
            }
        }

        return items;
    }

    // ---------------- UPDATE ITEM ----------------

    public static void updateItem(int itemId, String name, String category,
                                  String unit, BigDecimal price, int minStock,
                                  int userId) throws SQLException {

        if (itemId <= 0) {
            throw new IllegalArgumentException("Invalid grocery item ID.");
        }

        validateItem(name, category, 0, unit, price, minStock);

        String sql = """
                UPDATE grocery_items
                SET item_name = ?, category = ?, unit = ?,
                    price = ?, min_stock = ?
                WHERE item_id = ?
                """;

        try (Connection con = DatabaseConnection.getConnection()) {
            requireRole(con, userId, "ADMIN");

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, name.trim());
                ps.setString(2, category.trim());
                ps.setString(3, unit.trim());
                ps.setBigDecimal(4, normalizePrice(price));
                ps.setInt(5, minStock);
                ps.setInt(6, itemId);

                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Grocery item not found.");
                }
            }
        }
    }

    // ---------------- DELETE ITEM ----------------

public static void deleteItem(int itemId, int userId) throws SQLException {

    if (itemId <= 0) {
        throw new IllegalArgumentException("Invalid grocery item ID.");
    }

    try (Connection con = DatabaseConnection.getConnection()) {
        con.setAutoCommit(false);

        try {
            requireRole(con, userId, "ADMIN");

            String lockSQL = """
                    SELECT item_id FROM grocery_items
                    WHERE item_id = ? FOR UPDATE
                    """;

            try (PreparedStatement ps = con.prepareStatement(lockSQL)) {
                ps.setInt(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Grocery item not found.");
                    }
                }
            }

            int count = 0;
            int initialId = -1;
            boolean onlyInitial = false;

            String historySQL = """
                    SELECT transaction_id, transaction_type, notes
                    FROM stock_transactions
                    WHERE item_id = ?
                    ORDER BY transaction_id
                    FOR UPDATE
                    """;

            try (PreparedStatement ps = con.prepareStatement(historySQL)) {
                ps.setInt(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        count++;

                        if (count == 1
                                && "STOCK_IN".equals(rs.getString("transaction_type"))
                                && "Initial stock".equals(rs.getString("notes"))) {
                            initialId = rs.getInt("transaction_id");
                            onlyInitial = true;
                        } else {
                            onlyInitial = false;
                        }
                    }
                }
            }

            if (count > 1 || (count == 1 && !onlyInitial)) {
                throw new SQLException(
                        "This item has stock movement history and cannot be permanently deleted."
                );
            }

            if (count == 1) {
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM stock_transactions WHERE transaction_id = ? AND item_id = ?")) {
                    ps.setInt(1, initialId);
                    ps.setInt(2, itemId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Could not remove the initial stock record.");
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM grocery_items WHERE item_id = ?")) {
                ps.setInt(1, itemId);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Grocery item could not be deleted.");
                }
            }

            con.commit();

        } catch (SQLException | RuntimeException ex) {
            rollback(con, ex);
            throw ex;
        }
    }
}


    // ---------------- STOCK IN / STOCK OUT ----------------

    public static void adjustStock(int itemId, int userId,
                                   String type, int amount, String notes)
            throws SQLException {

        if (itemId <= 0) {
            throw new IllegalArgumentException("Invalid grocery item ID.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        if (!"STOCK_IN".equals(type) && !"STOCK_OUT".equals(type)) {
            throw new IllegalArgumentException(
                    "Invalid stock transaction type."
            );
        }

        String cleanNotes = notes == null ? "" : notes.trim();

        if (cleanNotes.length() > 255) {
            throw new IllegalArgumentException(
                    "Transaction notes cannot exceed 255 characters."
            );
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                requireRole(con, userId, "ADMIN", "STAFF");

                int currentQuantity;

                String selectSQL = """
                        SELECT quantity
                        FROM grocery_items
                        WHERE item_id = ?
                        FOR UPDATE
                        """;

                try (PreparedStatement ps = con.prepareStatement(selectSQL)) {
                    ps.setInt(1, itemId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Grocery item not found.");
                        }
                        currentQuantity = rs.getInt("quantity");
                    }
                }

                int newQuantity;

                if ("STOCK_IN".equals(type)) {
                    try {
                        newQuantity = Math.addExact(currentQuantity, amount);
                    } catch (ArithmeticException ex) {
                        throw new IllegalArgumentException(
                                "Stock quantity exceeds the maximum allowed value."
                        );
                    }
                } else {
                    if (amount > currentQuantity) {
                        throw new IllegalArgumentException(
                                "Insufficient stock available."
                        );
                    }
                    newQuantity = currentQuantity - amount;
                }

                String updateSQL = """
                        UPDATE grocery_items
                        SET quantity = ?
                        WHERE item_id = ?
                        """;

                try (PreparedStatement ps = con.prepareStatement(updateSQL)) {
                    ps.setInt(1, newQuantity);
                    ps.setInt(2, itemId);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Could not update stock.");
                    }
                }

                recordTransaction(
                        con, itemId, userId, type, amount, cleanNotes
                );

                con.commit();

            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                throw ex;
            }
        }
    }

    // ---------------- TRANSACTION RECORD ----------------

    private static void recordTransaction(Connection con, int itemId,
                                           int userId, String type,
                                           int quantity, String notes)
            throws SQLException {

        String sql = """
                INSERT INTO stock_transactions
                (item_id, user_id, transaction_type, quantity, notes)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            ps.setString(3, type);
            ps.setInt(4, quantity);
            ps.setString(5, notes == null ? "" : notes);
            ps.executeUpdate();
        }
    }

    // ---------------- LOW STOCK ALERTS ----------------

    public static List<GroceryItem> getLowStockItems() throws SQLException {
        List<GroceryItem> items = new ArrayList<>();

        String sql = """
                SELECT item_id, item_name, category, quantity,
                       unit, price, min_stock
                FROM grocery_items
                WHERE quantity <= min_stock
                ORDER BY quantity ASC
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(readItem(rs));
            }
        }

        return items;
    }

    // ---------------- INVENTORY VALUE ----------------

    public static BigDecimal getInventoryValue() throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(quantity * price), 0.00) AS total_value
                FROM grocery_items
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return BigDecimal.ZERO.setScale(2);
            }

            BigDecimal total = rs.getBigDecimal("total_value");

            return total == null
                    ? BigDecimal.ZERO.setScale(2)
                    : total.setScale(2, RoundingMode.HALF_UP);
        }
    }

    // ---------------- TRANSACTION HISTORY ----------------

    public static List<String[]> getTransactionHistory() throws SQLException {
        return getTransactionHistory(null, null, "ALL");
    }

    public static List<String[]> getTransactionHistory(
            java.sql.Date fromDate, java.sql.Date toDate, String type)
            throws SQLException {

        if (fromDate != null && toDate != null
                && fromDate.after(toDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date."
            );
        }

        if (type != null && !type.equalsIgnoreCase("ALL")
                && !type.equals("STOCK_IN")
                && !type.equals("STOCK_OUT")) {
            throw new IllegalArgumentException(
                    "Invalid transaction filter."
            );
        }

        List<String[]> history = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT t.transaction_id, g.item_name,
                       u.username, t.transaction_type,
                       t.quantity, t.notes, t.transaction_date
                FROM stock_transactions t
                JOIN grocery_items g ON t.item_id = g.item_id
                JOIN users u ON t.user_id = u.user_id
                WHERE 1 = 1
                """);

        if (fromDate != null) {
            sql.append(" AND DATE(t.transaction_date) >= ?");
        }
        if (toDate != null) {
            sql.append(" AND DATE(t.transaction_date) <= ?");
        }
        if (type != null && !type.equalsIgnoreCase("ALL")) {
            sql.append(" AND t.transaction_type = ?");
        }

        sql.append(" ORDER BY t.transaction_date DESC");

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;

            if (fromDate != null) {
                ps.setDate(index++, fromDate);
            }
            if (toDate != null) {
                ps.setDate(index++, toDate);
            }
            if (type != null && !type.equalsIgnoreCase("ALL")) {
                ps.setString(index, type);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp timestamp =
                            rs.getTimestamp("transaction_date");

                    history.add(new String[]{
                            String.valueOf(rs.getInt("transaction_id")),
                            rs.getString("item_name"),
                            rs.getString("username"),
                            rs.getString("transaction_type"),
                            String.valueOf(rs.getInt("quantity")),
                            rs.getString("notes"),
                            timestamp == null ? "" : timestamp.toString()
                    });
                }
            }
        }

        return history;
    }

    // ---------------- DASHBOARD COUNTS ----------------

    public static int getTotalItemCount() throws SQLException {
        return getCount("SELECT COUNT(*) FROM grocery_items");
    }

    public static int getLowStockCount() throws SQLException {
        return getCount(
                "SELECT COUNT(*) FROM grocery_items WHERE quantity <= min_stock"
        );
    }

    private static int getCount(String sql) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ---------------- HELPER METHODS ----------------

    private static GroceryItem readItem(ResultSet rs) throws SQLException {
        BigDecimal price = rs.getBigDecimal("price");

        return new GroceryItem(
                rs.getInt("item_id"),
                rs.getString("item_name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getString("unit"),
                price == null ? BigDecimal.ZERO.setScale(2)
                        : price.setScale(2, RoundingMode.HALF_UP),
                rs.getInt("min_stock")
        );
    }

    private static BigDecimal normalizePrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.UNNECESSARY);
    }

    static void validateItem(String name, String category,
                             int quantity, String unit,
                             BigDecimal price, int minStock) {

        if (name == null || name.isBlank()
                || category == null || category.isBlank()
                || unit == null || unit.isBlank()) {
            throw new IllegalArgumentException(
                    "Name, category and unit are required."
            );
        }

        if (name.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Item name cannot exceed 100 characters."
            );
        }

        if (category.trim().length() > 50) {
            throw new IllegalArgumentException(
                    "Category cannot exceed 50 characters."
            );
        }

        if (unit.trim().length() > 20) {
            throw new IllegalArgumentException(
                    "Unit cannot exceed 20 characters."
            );
        }

        if (quantity < 0 || minStock < 0) {
            throw new IllegalArgumentException(
                    "Quantity and minimum stock cannot be negative."
            );
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) < 0
                || price.compareTo(MAX_PRICE) > 0) {
            throw new IllegalArgumentException(
                    "Enter a valid price between 0 and 99,999,999.99."
            );
        }

        try {
            price.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "Price can contain a maximum of two decimal places."
            );
        }
    }

    private static void rollback(Connection con, Exception original) {
        try {
            con.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }
}
