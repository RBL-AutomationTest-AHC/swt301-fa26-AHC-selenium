package vn.edu.fpt.ancu.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.fpt.ancu.core.BaseTest;
import vn.edu.fpt.ancu.data.TestData;
import vn.edu.fpt.ancu.pages.RegisterPage;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegisterTest extends BaseTest {
    private static final String CSV = "/testdata/register_data.csv";

    @Test @DisplayName("TC-REG-001")
    void optionalPhone() { assertRegistrationCreated("TC-REG-001"); }

    @Test @DisplayName("TC-REG-002")
    void requiredName() { assertClientValidation("TC-REG-002"); }

    @Test @DisplayName("TC-REG-003")
    void invalidEmail() { assertClientValidation("TC-REG-003"); }

    @Test @DisplayName("TC-REG-004")
    void duplicateEmail() { assertRejected("TC-REG-004"); }

    @Test @DisplayName("TC-REG-005")
    void passwordLength11() { assertClientValidation("TC-REG-005"); }

    @Test @DisplayName("TC-REG-006")
    void passwordLength12() { assertRegistrationCreated("TC-REG-006"); }

    @Test @DisplayName("TC-REG-007")
    void passwordByte73() { assertRejected("TC-REG-007"); }

    @Test @DisplayName("TC-REG-008")
    void confirmationMismatch() { assertRejected("TC-REG-008"); }

    @Test @DisplayName("TC-REG-009")
    void normalizedVietnamPhone() { assertRegistrationCreated("TC-REG-009"); }

    @Test @DisplayName("TC-REG-010")
    void invalidPhone() { assertRejected("TC-REG-010"); }

    private void assertClientValidation(String tcId) {
        Map<String, String> data = TestData.resolvedRow(CSV, tcId);
        RegisterPage page = new RegisterPage(driver).open().fill(data).submit();
        assertEquals("CLIENT_VALIDATION", data.get("expectedOutcome"));
        assertAll(
                () -> assertFalse(page.fieldValid(data.get("invalidField")), "Browser must reject the invalid field"),
                () -> assertTrue(page.atRegister()),
                () -> assertFalse(page.authenticatedMenuVisible())
        );
    }

    private void assertRejected(String tcId) {
        Map<String, String> data = TestData.resolvedRow(CSV, tcId);
        RegisterPage page = new RegisterPage(driver).open().fill(data).submit();
        assertEquals("ERROR", data.get("expectedOutcome"));
        assertEquals(data.get("expectedMessage"), page.alert());
        page.waitUntilSubmissionFinished();
        assertAll(() -> assertTrue(page.atRegister()), () -> assertFalse(page.authenticatedMenuVisible()));
    }

    private void assertRegistrationCreated(String tcId) {
        Map<String, String> data = TestData.resolvedRow(CSV, tcId);
        RegisterPage page = new RegisterPage(driver).open().fill(data).submit();
        assertEquals("SUCCESS", data.get("expectedOutcome"));
        String notice = page.waitForCreatedNotice();
        if (page.atVerification()) {
            assertTrue(notice.startsWith("Đăng ký thành công"));
            assertEquals(data.get("email"), page.verificationEmail());
        } else {
            // The SUT explicitly supports this path after account creation if automatic login fails.
            assertTrue(page.atCreatedAccountLogin());
            assertEquals("Tài khoản đã được tạo. Vui lòng đăng nhập để xác minh email.", notice);
        }
    }
}
