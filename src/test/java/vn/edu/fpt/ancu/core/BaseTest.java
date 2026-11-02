package vn.edu.fpt.ancu.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import vn.edu.fpt.ancu.config.Config;

import java.time.Duration;
import java.util.Locale;

public abstract class BaseTest {
    protected WebDriver driver;

    @RegisterExtension
    final ScreenshotExtension screenshots = new ScreenshotExtension();

    @BeforeEach
    void openBrowser() {
        driver = createDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1440, 1000));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public WebDriver driverForScreenshot() {
        return driver;
    }

    protected String baseUrl() {
        return Config.get("base.url").replaceAll("/+$", "");
    }

    private WebDriver createDriver() {
        boolean headless = Config.bool("headless");
        return switch (Config.get("browser").toLowerCase(Locale.ROOT)) {
            case "chrome" -> {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--disable-notifications", "--window-size=1440,1000");
                if (headless) options.addArguments("--headless=new");
                yield new ChromeDriver(options);
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                options.addArguments("--disable-notifications", "--window-size=1440,1000");
                if (headless) options.addArguments("--headless=new");
                yield new EdgeDriver(options);
            }
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) options.addArguments("-headless");
                yield new FirefoxDriver(options);
            }
            default -> throw new IllegalStateException("Browser hỗ trợ: chrome, edge, firefox");
        };
    }
}
