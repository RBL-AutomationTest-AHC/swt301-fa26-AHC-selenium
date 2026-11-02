package vn.edu.fpt.ancu.core;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import vn.edu.fpt.ancu.config.Config;

import java.time.Duration;
import java.util.List;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.integer("timeout.seconds")));
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement present(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void type(By locator, String value) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(value == null ? "" : value);
    }

    protected void click(By locator) {
        clickable(locator).click();
    }

    protected String text(By locator) {
        return visible(locator).getText().strip();
    }

    protected List<WebElement> all(By locator) {
        return driver.findElements(locator);
    }

    protected void selectText(By locator, String value) {
        new Select(visible(locator)).selectByVisibleText(value);
    }

    protected void selectValue(By locator, String value) {
        new Select(visible(locator)).selectByValue(value);
    }

    protected String selectedValue(By locator) {
        return new Select(visible(locator)).getFirstSelectedOption().getAttribute("value");
    }

    protected boolean browserValid(By locator) {
        Object result = ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].checkValidity();", present(locator));
        return Boolean.TRUE.equals(result);
    }

    protected void waitUrlNotContains(String part) {
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains(part)));
    }
}
