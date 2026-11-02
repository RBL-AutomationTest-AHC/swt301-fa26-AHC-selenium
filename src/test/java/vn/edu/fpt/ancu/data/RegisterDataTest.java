package vn.edu.fpt.ancu.data;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class RegisterDataTest {
    private static final String CSV = "/testdata/register_data.csv";

    @Test
    void tenIndependentCasesHaveCompleteExpectedOutcomes() {
        var rows = CsvData.read(CSV);
        assertEquals(10, rows.size());
        assertEquals(IntStream.rangeClosed(1, 10).mapToObj(n -> "TC-REG-%03d".formatted(n))
                .collect(Collectors.toSet()), rows.stream().map(row -> row.get("tcId")).collect(Collectors.toSet()));
        for (var row : rows) {
            assertFalse(row.get("expectedOutcome").isBlank());
            switch (row.get("expectedOutcome")) {
                case "ERROR" -> assertFalse(row.get("expectedMessage").isBlank());
                case "CLIENT_VALIDATION" -> assertFalse(row.get("invalidField").isBlank());
                case "SUCCESS" -> assertTrue(row.get("email").contains("${UNIQUE}"));
                default -> fail("Unknown expected outcome: " + row.get("tcId"));
            }
        }
    }

    @Test
    void resolvedBoundaryDataAndConfirmationsMatchTheTestDesign() {
        var shortPassword = TestData.resolvedRow(CSV, "TC-REG-005");
        var minimumPassword = TestData.resolvedRow(CSV, "TC-REG-006");
        var bytePassword = TestData.resolvedRow(CSV, "TC-REG-007");
        assertEquals(11, shortPassword.get("password").length());
        assertEquals(12, minimumPassword.get("password").length());
        assertEquals(73, bytePassword.get("password").getBytes(StandardCharsets.UTF_8).length);
        assertEquals(bytePassword.get("password"), bytePassword.get("confirmPassword"));
        assertNotEquals(minimumPassword.get("email"), TestData.resolvedRow(CSV, "TC-REG-006").get("email"));
        assertEquals("", TestData.resolvedRow(CSV, "TC-REG-001").get("phone"));
        assertEquals("091 234 5678", TestData.resolvedRow(CSV, "TC-REG-009").get("phone"));
    }

    @Test
    void duplicateEmailFixtureUsesUppercaseWithoutChangingTheConfiguredAccount() {
        String previous = System.getProperty("EXISTING_EMAIL");
        System.setProperty("EXISTING_EMAIL", "existing@example.test");
        try {
            assertEquals("EXISTING@EXAMPLE.TEST", TestData.resolvedRow(CSV, "TC-REG-004").get("email"));
        } finally {
            if (previous == null) System.clearProperty("EXISTING_EMAIL");
            else System.setProperty("EXISTING_EMAIL", previous);
        }
    }
}
