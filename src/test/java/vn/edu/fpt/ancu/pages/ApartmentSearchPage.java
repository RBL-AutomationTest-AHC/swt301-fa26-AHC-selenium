package vn.edu.fpt.ancu.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import vn.edu.fpt.ancu.config.Config;
import vn.edu.fpt.ancu.core.BasePage;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ApartmentSearchPage extends BasePage {
    private static final By QUERY = By.id("apartment-search");
    private static final By CITY = By.id("address-city");
    private static final By DISTRICT = By.id("address-district");
    private static final By WARD = By.id("address-ward");
    private static final By MIN_RENT = By.id("apartment-min-rent");
    private static final By MAX_RENT = By.id("apartment-max-rent");
    private static final By BEDROOMS = By.id("apartment-bedrooms");
    private static final By SEARCH = By.xpath("//form[@aria-label='Bộ lọc căn hộ']//button[@type='submit']");
    private static final By RESULTS = By.cssSelector("section.home-results");
    private static final By STATUS = By.cssSelector("section.home-results [role='status']");
    private static final By ALERT = By.cssSelector(".home-filter-error[role='alert'],.home-error[role='alert']");
    private static final By EMPTY = By.cssSelector(".home-empty h3");
    private static final By CLEAR = By.xpath("//section[contains(@class,'home-results')]//button[normalize-space()='Xóa bộ lọc']");
    private static final By CARDS = By.cssSelector("[data-testid='apartment-card']");
    private static final By NEXT = By.xpath("//nav[@aria-label='Phân trang']//button[normalize-space()='Trang sau']");
    private static final By PAGE_TEXT = By.xpath("//nav[@aria-label='Phân trang']/span");

    public ApartmentSearchPage(WebDriver driver) {
        super(driver);
    }

    public ApartmentSearchPage open() {
        driver.get(Config.get("base.url").replaceAll("/+$", "") + "/");
        visible(QUERY);
        waitForResults();
        return this;
    }

    public ApartmentSearchPage keyword(String value) {
        type(QUERY, value);
        return this;
    }

    public ApartmentSearchPage address(String city, String district, String ward) {
        if (city == null || city.isBlank()) {
            selectValue(CITY, "");
            return this;
        }
        selectText(CITY, city);
        wait.until(ExpectedConditions.elementToBeClickable(DISTRICT));
        if (district == null || district.isBlank()) {
            selectValue(DISTRICT, "");
            return this;
        }
        selectText(DISTRICT, district);
        wait.until(ExpectedConditions.elementToBeClickable(WARD));
        if (ward == null || ward.isBlank()) selectValue(WARD, "");
        else selectText(WARD, ward);
        return this;
    }

    public ApartmentSearchPage rent(String minimum, String maximum) {
        type(MIN_RENT, minimum);
        type(MAX_RENT, maximum);
        return this;
    }

    public ApartmentSearchPage bedrooms(String value) {
        selectValue(BEDROOMS, value);
        return this;
    }

    public ApartmentSearchPage search() {
        click(SEARCH);
        waitForResults();
        return this;
    }

    public ApartmentSearchPage clearFilters() {
        click(CLEAR);
        wait.until(driver -> queryValue().isEmpty() && cityValue().isEmpty()
                && districtValue().isEmpty() && wardValue().isEmpty()
                && minRentValue().isEmpty() && maxRentValue().isEmpty()
                && bedroomsValue().isEmpty());
        waitForResults();
        return this;
    }

    public List<ResultCard> cards() {
        return all(CARDS).stream().map(ResultCard::new).toList();
    }

    public String resultStatus() {
        return text(STATUS);
    }

    public String alert() {
        return text(ALERT);
    }

    public String emptyMessage() {
        return text(EMPTY);
    }

    public String queryValue() { return present(QUERY).getAttribute("value"); }
    public String cityValue() { return selectedValue(CITY); }
    public String districtValue() { return selectedValue(DISTRICT); }
    public String wardValue() { return selectedValue(WARD); }
    public String minRentValue() { return present(MIN_RENT).getAttribute("value"); }
    public String maxRentValue() { return present(MAX_RENT).getAttribute("value"); }
    public String bedroomsValue() { return selectedValue(BEDROOMS); }

    public ApartmentSearchPage chooseDifferentCity() {
        Select select = new Select(visible(CITY));
        String current = select.getFirstSelectedOption().getAttribute("value");
        List<WebElement> options = select.getOptions();
        for (int index = 0; index < options.size(); index++) {
            String value = options.get(index).getAttribute("value");
            if (value != null && !value.isBlank() && !value.equals(current)) {
                select.selectByIndex(index);
                return this;
            }
        }
        select.selectByIndex(0);
        return this;
    }

    public int totalPages() {
        List<WebElement> labels = all(PAGE_TEXT);
        if (labels.isEmpty()) return 1;
        String[] parts = labels.get(0).getText().split("/");
        return Integer.parseInt(parts[1].trim());
    }

    public int currentPage() {
        List<WebElement> labels = all(PAGE_TEXT);
        if (labels.isEmpty()) return 1;
        String text = labels.get(0).getText().replace("Trang", "").trim();
        return Integer.parseInt(text.split("/")[0].trim());
    }

    public Set<String> cardLinks() {
        Set<String> links = new LinkedHashSet<>();
        for (ResultCard card : cards()) links.add(card.link());
        return links;
    }

    public ApartmentSearchPage nextPage() {
        Set<String> oldLinks = cardLinks();
        click(NEXT);
        wait.until(driver -> !cardLinks().equals(oldLinks));
        waitForResults();
        return this;
    }

    private void waitForResults() {
        visible(RESULTS);
        wait.until(driver -> "false".equals(driver.findElement(RESULTS).getAttribute("aria-busy")));
        visible(STATUS);
    }

    public static final class ResultCard {
        private final WebElement root;

        private ResultCard(WebElement root) {
            this.root = root;
        }

        public String title() {
            return root.findElement(By.cssSelector("h2")).getText().strip();
        }

        public String address() {
            return root.findElement(By.xpath(".//p[.//*[local-name()='svg']]")).getText().strip();
        }

        public String bedrooms() {
            return root.findElement(By.xpath(".//span[contains(.,'Studio') or contains(.,'phòng ngủ')]")).getText().strip();
        }

        public BigDecimal rent() {
            String digits = root.findElement(By.cssSelector("p.text-lg")).getText().replaceAll("[^0-9]", "");
            if (digits.isEmpty()) throw new IllegalStateException("Không parse được giá trên card: " + title());
            return new BigDecimal(digits);
        }

        public String link() {
            return root.findElement(By.cssSelector("h2 a")).getAttribute("href");
        }
    }
}
