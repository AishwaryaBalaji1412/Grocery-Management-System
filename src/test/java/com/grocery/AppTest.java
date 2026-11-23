
package com.grocery;

import org.junit.Test;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

public class AppTest {

    // ---------------- APPLICATION TESTS ----------------

    @Test
    public void applicationClassesShouldExist() {
        assertNotNull(App.class);
        assertNotNull(DatabaseConnection.class);
        assertNotNull(GroceryManagement.class);
        assertNotNull(MainUI.class);
    }

    // ---------------- USER MODEL TESTS ----------------

    @Test
    public void userShouldStoreCorrectDetails() {
        GroceryManagement.User user =
                new GroceryManagement.User(
                        1, "Test User", "testuser", "STAFF"
                );

        assertEquals(1, user.userId);
        assertEquals("Test User", user.fullName);
        assertEquals("testuser", user.username);
        assertEquals("STAFF", user.role);
    }

    @Test
    public void adminRoleShouldBeRecognized() {
        GroceryManagement.User user =
                new GroceryManagement.User(
                        1, "Admin", "admin", "ADMIN"
                );

        assertTrue(user.isAdmin());
    }

    @Test
    public void staffShouldNotBeRecognizedAsAdmin() {
        GroceryManagement.User user =
                new GroceryManagement.User(
                        2, "Staff", "staff", "STAFF"
                );

        assertFalse(user.isAdmin());
    }

    // ---------------- GROCERY ITEM TESTS ----------------

    @Test
    public void groceryItemShouldStoreCorrectDetails() {
        BigDecimal price = new BigDecimal("65.50");

        GroceryManagement.GroceryItem item =
                new GroceryManagement.GroceryItem(
                        101, "Rice", "Grains", 20,
                        "kg", price, 5
                );

        assertEquals(101, item.itemId);
        assertEquals("Rice", item.itemName);
        assertEquals("Grains", item.category);
        assertEquals(20, item.quantity);
        assertEquals("kg", item.unit);
        assertEquals(price, item.price);
        assertEquals(5, item.minStock);
    }

    // ---------------- PASSWORD SECURITY TESTS ----------------

    @Test
    public void passwordShouldBeHashed()
            throws GeneralSecurityException {

        String password = "SecurePass123";

        String hashedPassword =
                GroceryManagement.hashPassword(password);

        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
    }

    @Test
    public void correctPasswordShouldBeVerified()
            throws GeneralSecurityException {

        String password = "SecurePass123";

        String hashedPassword =
                GroceryManagement.hashPassword(password);

        assertTrue(
                GroceryManagement.verifyPassword(
                        password, hashedPassword
                )
        );
    }

    @Test
    public void incorrectPasswordShouldFailVerification()
            throws GeneralSecurityException {

        String hashedPassword =
                GroceryManagement.hashPassword("SecurePass123");

        assertFalse(
                GroceryManagement.verifyPassword(
                        "WrongPassword123", hashedPassword
                )
        );
    }

    @Test
    public void samePasswordShouldGenerateDifferentHashes()
            throws GeneralSecurityException {

        String firstHash =
                GroceryManagement.hashPassword("SecurePass123");

        String secondHash =
                GroceryManagement.hashPassword("SecurePass123");

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    public void malformedPasswordHashShouldBeRejected()
            throws GeneralSecurityException {

        assertFalse(
                GroceryManagement.verifyPassword(
                        "SecurePass123", "invalid-hash"
                )
        );
    }

    @Test
    public void nullPasswordShouldFailVerification()
            throws GeneralSecurityException {

        assertFalse(
                GroceryManagement.verifyPassword(
                        null, "invalid-hash"
                )
        );
    }

    // ---------------- ITEM VALIDATION TESTS ----------------

    @Test
    public void validItemDetailsShouldPassValidation() {
        GroceryManagement.validateItem(
                "Rice", "Grains", 20, "kg",
                new BigDecimal("65.50"), 5
        );
    }

    @Test
    public void zeroPriceShouldBeAccepted() {
        GroceryManagement.validateItem(
                "Rice", "Grains", 20, "kg",
                BigDecimal.ZERO, 5
        );
    }

    @Test
    public void maximumDatabasePriceShouldBeAccepted() {
        GroceryManagement.validateItem(
                "Rice", "Grains", 20, "kg",
                new BigDecimal("99999999.99"), 5
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyItemNameShouldBeRejected() {
        GroceryManagement.validateItem(
                "", "Grains", 20, "kg",
                new BigDecimal("65.50"), 5
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeQuantityShouldBeRejected() {
        GroceryManagement.validateItem(
                "Rice", "Grains", -5, "kg",
                new BigDecimal("65.50"), 5
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativePriceShouldBeRejected() {
        GroceryManagement.validateItem(
                "Rice", "Grains", 20, "kg",
                new BigDecimal("-10.00"), 5
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPriceShouldBeRejected() {
        GroceryManagement.validateItem(
                "Rice", "Grains", 20, "kg",
                null, 5
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void priceWithMoreThanTwoDecimalPlacesShouldBeRejected() {
        GroceryManagement.validateItem(
                "Rice", "Grains", 20, "kg",
                new BigDecimal("65.555"), 5
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void priceAboveDatabaseLimitShouldBeRejected() {
        GroceryManagement.validateItem(
                "Rice", "Grains", 20, "kg",
                new BigDecimal("100000000.00"), 5
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeMinimumStockShouldBeRejected() {
        GroceryManagement.validateItem(
                "Rice", "Grains", 20, "kg",
                new BigDecimal("65.50"), -1
        );
    }
}
