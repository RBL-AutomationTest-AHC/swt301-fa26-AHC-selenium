package vn.edu.fpt.ancu.data;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DataUtilitiesTest {
    @Test
    void tokensShareOneContextButParallelCasesRemainIndependent() {
        var context = DynamicData.context();
        String resolved = DynamicData.resolve("${UNIQUE}/${UNIQUE}", context);
        assertEquals(context.unique() + "/" + context.unique(), resolved);
        Set<String> unique = IntStream.range(0, 1000).parallel()
                .mapToObj(ignored -> DynamicData.context().unique()).collect(Collectors.toSet());
        assertEquals(1000, unique.size());
    }

    @Test
    void unicodeBoundaryUsesBytesRatherThanCharacterCount() {
        String password = DynamicData.resolve("${PASSWORD_73_BYTES}", DynamicData.context());
        assertEquals(73, password.getBytes(StandardCharsets.UTF_8).length);
        assertTrue(password.length() < 72, "Must fit HTML maxlength while exceeding the UTF-8 byte limit");
    }

    @Test
    void csvSupportsUtf8BomQuotedCommaEscapedQuoteAndEmptyField() {
        var row = CsvData.row("/framework/quoted.csv", "TC-DATA-001");
        assertEquals("Chương, Nguyễn", row.get("name"));
        assertEquals("a\"b", row.get("quoted"));
        assertEquals("", row.get("empty"));
        var resolved = TestData.resolvedRow("/framework/quoted.csv", "TC-DATA-001");
        assertEquals(resolved.get("email"), resolved.get("repeat"));
        assertFalse(resolved.get("email").contains("${"));
    }

    @Test
    void badSchemasAndMissingRowsFailBeforeBrowserInteraction() {
        assertThrows(IllegalArgumentException.class, () -> CsvData.read("/framework/bad-columns.csv"));
        assertThrows(IllegalArgumentException.class, () -> CsvData.read("/framework/unclosed-quote.csv"));
        assertThrows(IllegalArgumentException.class, () -> CsvData.row("/framework/quoted.csv", "missing"));
        assertThrows(IllegalArgumentException.class, () -> CsvData.read("/missing.csv"));
    }
}
