package vn.edu.fpt.ancu.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import vn.edu.fpt.ancu.config.Config;
import vn.edu.fpt.ancu.core.BaseTest;
import vn.edu.fpt.ancu.data.TestData;
import vn.edu.fpt.ancu.pages.LoginPage;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoginTest extends BaseTest {
    private static final String CSV = "/testdata/login_data.csv";

    @Test
    @DisplayName("TC-LOGIN-001")
    void validLogin() {
        Map<String, String> data = data("TC-LOGIN-001");
        LoginPage page = new LoginPage(driver).open().login(data.get("email"), data.get("password")).waitUntilAuthenticated();
        assertFalse(driver.getCurrentUrl().contains("/login"));
        assertTrue(page.accountMenuVisible());
    }

    @Test
    @DisplayName("TC-LOGIN-002")
    void normalizedEmail() {
        Map<String, String> data = data("TC-LOGIN-002");
        new LoginPage(driver).open().login(data.get("email"), data.get("password")).waitUntilAuthenticated();
        assertFalse(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @DisplayName("TC-LOGIN-003")
    void unknownEmail() {
        assertGenericError(data("TC-LOGIN-003"));
    }

    @Test
    @DisplayName("TC-LOGIN-004")
    void wrongPassword() {
        assertGenericError(data("TC-LOGIN-004"));
    }

    @Test
    @DisplayName("TC-LOGIN-005")
    void clientValidation() {
        Map<String, String> data = data("TC-LOGIN-005");
        LoginPage page = new LoginPage(driver).open()
                .enterEmail(data.get("email"))
                .enterPassword(data.get("password"))
                .submit();
        assertFalse(page.emailBrowserValid(), "Email sai định dạng phải bị HTML validation chặn");
        assertTrue(driver.getCurrentUrl().contains("/login"));

        page.open().enterEmail("").enterPassword("").submit();
        assertFalse(page.emailBrowserValid());
        assertFalse(page.passwordBrowserValid());
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @DisplayName("TC-LOGIN-006")
    void passwordCaseSensitive() {
        assertGenericError(data("TC-LOGIN-006"));
    }

    @Test
    @DisplayName("TC-LOGIN-007")
    void passwordByteBoundary() {
        Map<String, String> data = data("TC-LOGIN-007");
        LoginPage page = new LoginPage(driver).open().login(data.get("email"), data.get("password"));
        assertEquals(data.get("expectedMessage"), page.alert());
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @DisplayName("TC-LOGIN-008")
    void lockedAccount() {
        Assumptions.assumeTrue(Config.hasFixture("LOCKED_EMAIL") && Config.hasFixture("LOCKED_PASSWORD"),
                "BLOCKED: chưa có fixture account LOCKED/INACTIVE");
        Map<String, String> data = data("TC-LOGIN-008");
        LoginPage page = new LoginPage(driver).open().login(data.get("email"), data.get("password"));
        assertEquals(data.get("expectedMessage"), page.alert());
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @DisplayName("TC-LOGIN-009")
    void passwordVisibility() {
        String value = "Không-in-secret-123";
        LoginPage page = new LoginPage(driver).open().enterPassword(value);
        assertEquals("password", page.passwordType());
        page.togglePassword();
        assertAll(
                () -> assertEquals("text", page.passwordType()),
                () -> assertEquals(value, page.passwordValue())
        );
        page.togglePassword();
        assertAll(
                () -> assertEquals("password", page.passwordType()),
                () -> assertEquals(value, page.passwordValue())
        );
    }

    @Test
    @DisplayName("TC-LOGIN-010")
    void sessionLifecycle() {
        Map<String, String> data = data("TC-LOGIN-001");
        LoginPage page = new LoginPage(driver).open().login(data.get("email"), data.get("password")).waitUntilAuthenticated();
        driver.navigate().refresh();
        page.waitUntilAuthenticated();
        assertTrue(page.accountMenuVisible(), "Reload phải khôi phục phiên");
        page.logout();
        driver.get(baseUrl() + "/account/profile");
        page.waitUntilAtLogin();
        assertTrue(driver.getCurrentUrl().contains("/login"), "Protected route phải về login sau logout");
    }

    private void assertGenericError(Map<String, String> data) {
        LoginPage page = new LoginPage(driver).open().login(data.get("email"), data.get("password"));
        assertEquals(data.get("expectedMessage"), page.alert());
        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertFalse(page.accountMenuVisible());
    }

    private Map<String, String> data(String tcId) {
        return TestData.resolvedRow(CSV, tcId);
    }
}
