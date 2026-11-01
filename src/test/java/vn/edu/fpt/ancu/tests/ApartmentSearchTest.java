package vn.edu.fpt.ancu.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.fpt.ancu.core.BaseTest;
import vn.edu.fpt.ancu.data.TestData;
import vn.edu.fpt.ancu.pages.ApartmentSearchPage;
import vn.edu.fpt.ancu.pages.ApartmentSearchPage.ResultCard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApartmentSearchTest extends BaseTest {
    private static final String CSV = "/testdata/apartment_search_data.csv";

    @Test
    @DisplayName("TC-SEARCH-001")
    void titleSubstring() {
        Map<String, String> data = data("TC-SEARCH-001");
        ApartmentSearchPage page = new ApartmentSearchPage(driver).open().keyword(data.get("query")).search();
        assertMatchingCards(page.cards(), data.get("query"), data.get("expectedMarker"));
    }

    @Test
    @DisplayName("TC-SEARCH-002")
    void addressSubstring() {
        Map<String, String> data = data("TC-SEARCH-002");
        ApartmentSearchPage page = new ApartmentSearchPage(driver).open().keyword(data.get("query")).search();
        assertMatchingCards(page.cards(), data.get("query"), data.get("expectedMarker"));
    }

    @Test
    @DisplayName("TC-SEARCH-003")
    void caseInsensitive() {
        Map<String, String> data = data("TC-SEARCH-003");
        ApartmentSearchPage page = new ApartmentSearchPage(driver).open().keyword(data.get("query")).search();
        String marker = lower(data.get("expectedMarker"));
        assertTrue(page.cards().stream().map(ResultCard::title).map(this::lower).anyMatch(value -> value.contains(marker)));
    }

    @Test
    @DisplayName("TC-SEARCH-004")
    void hierarchicalAddress() {
        Map<String, String> data = data("TC-SEARCH-004");
        ApartmentSearchPage page = new ApartmentSearchPage(driver).open()
                .address(data.get("city"), data.get("district"), data.get("ward")).search();
        assertFalse(page.cards().isEmpty());
        assertAll(
                () -> assertEquals(data.get("city"), page.cityValue()),
                () -> assertEquals(data.get("district"), page.districtValue()),
                () -> assertEquals(data.get("ward"), page.wardValue()),
                () -> assertTrue(page.cards().stream().anyMatch(card ->
                        lower(card.title()).contains(lower(data.get("expectedMarker")))))
        );
        for (ResultCard card : page.cards()) {
            String address = lower(card.address());
            assertTrue(address.contains(lower(data.get("city"))), card.address());
        }
        page.chooseDifferentCity();
        assertAll(
                () -> assertEquals("", page.districtValue()),
                () -> assertEquals("", page.wardValue())
        );
    }

    @Test
    @DisplayName("TC-SEARCH-005")
    void inclusiveRent() {
        Map<String, String> data = data("TC-SEARCH-005");
        BigDecimal expected = digits(data.get("minRent"));
        ApartmentSearchPage page = new ApartmentSearchPage(driver).open()
                .rent(data.get("minRent"), data.get("maxRent")).search();
        assertFalse(page.cards().isEmpty());
        assertTrue(page.cards().stream().anyMatch(card -> lower(card.title()).contains(lower(data.get("expectedMarker")))));
        page.cards().forEach(card -> assertEquals(0, expected.compareTo(card.rent()), card.title()));
    }

    @Test
    @DisplayName("TC-SEARCH-006")
    void invalidRentRange() {
        ApartmentSearchPage page = new ApartmentSearchPage(driver).open().rent("10000000", "5000000").search();
        assertEquals("Giá tối thiểu phải nhỏ hơn hoặc bằng giá tối đa.", page.alert());
    }

    @Test
    @DisplayName("TC-SEARCH-007")
    void studioFilter() {
        Map<String, String> data = data("TC-SEARCH-007");
        ApartmentSearchPage page = new ApartmentSearchPage(driver).open().bedrooms(data.get("bedrooms")).search();
        assertFalse(page.cards().isEmpty());
        assertTrue(page.cards().stream().anyMatch(card -> lower(card.title()).contains(lower(data.get("expectedMarker")))));
        page.cards().forEach(card -> assertEquals("Studio", card.bedrooms(), card.title()));
    }

    @Test
    @DisplayName("TC-SEARCH-008")
    void combinedFilters() {
        Map<String, String> data = data("TC-SEARCH-008");
        ApartmentSearchPage page = new ApartmentSearchPage(driver).open()
                .keyword(data.get("query"));
        if (!data.get("city").isBlank()) {
            page.address(data.get("city"), data.get("district"), data.get("ward"));
        }
        page.rent(data.get("minRent"), data.get("maxRent"))
                .bedrooms(data.get("bedrooms")).search();
        assertFalse(page.cards().isEmpty());
        assertTrue(page.cards().stream().anyMatch(card -> lower(card.title()).contains(lower(data.get("expectedMarker")))));
        BigDecimal min = digits(data.get("minRent"));
        BigDecimal max = digits(data.get("maxRent"));
        for (ResultCard card : page.cards()) {
            String searchable = lower(card.title() + " " + card.address());
            assertTrue(searchable.contains(lower(data.get("query"))), card.title());
            assertTrue(lower(card.address()).contains(lower(data.get("city"))), card.address());
            assertTrue(card.rent().compareTo(min) >= 0 && card.rent().compareTo(max) <= 0, card.title());
            assertTrue(card.bedrooms().startsWith(data.get("bedrooms") + " "), card.bedrooms());
        }
    }

    @Test
    @DisplayName("TC-SEARCH-009")
    void emptyResult() {
        Map<String, String> data = data("TC-SEARCH-009");
        ApartmentSearchPage page = new ApartmentSearchPage(driver).open().keyword(data.get("query")).search();
        assertAll(
                () -> assertEquals(data.get("expectedMarker"), page.emptyMessage()),
                () -> assertTrue(page.cards().isEmpty())
        );
    }

    @Test
    @DisplayName("TC-SEARCH-010")
    void paginationAndClear() {
        Map<String, String> data = data("TC-SEARCH-010");
        ApartmentSearchPage page = new ApartmentSearchPage(driver).open();
        assertTrue(page.totalPages() >= 2, "Fixture APT_PAGE_SET cần ít nhất 7 căn visible");
        Set<String> firstPage = page.cardLinks();
        page.nextPage();
        assertAll(
                () -> assertEquals(2, page.currentPage()),
                () -> assertNotEquals(firstPage, page.cardLinks())
        );

        page.keyword(data.get("query")).search();
        assertEquals(1, page.currentPage(), "Filter mới phải reset về page 1");
        page.rent("1000000", "9000000").bedrooms("1").clearFilters();
        assertAll(
                () -> assertEquals("", page.queryValue()),
                () -> assertEquals("", page.cityValue()),
                () -> assertEquals("", page.minRentValue()),
                () -> assertEquals("", page.maxRentValue()),
                () -> assertEquals("", page.bedroomsValue()),
                () -> assertEquals(1, page.currentPage())
        );
    }

    private void assertMatchingCards(List<ResultCard> cards, String query, String marker) {
        assertFalse(cards.isEmpty());
        assertTrue(cards.stream().anyMatch(card -> lower(card.title()).contains(lower(marker))));
        for (ResultCard card : cards) {
            assertTrue(lower(card.title() + " " + card.address()).contains(lower(query)), card.title());
        }
    }

    private BigDecimal digits(String value) {
        String normalized = value.replaceAll("[^0-9]", "");
        if (normalized.isEmpty()) throw new IllegalArgumentException("Giá fixture không hợp lệ: " + value);
        return new BigDecimal(normalized);
    }

    private String lower(String value) {
        return value.toLowerCase(Locale.forLanguageTag("vi"));
    }

    private Map<String, String> data(String tcId) {
        return TestData.resolvedRow(CSV, tcId);
    }
}
