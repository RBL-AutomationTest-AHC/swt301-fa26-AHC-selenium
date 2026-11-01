package vn.edu.fpt.ancu.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import vn.edu.fpt.ancu.config.Config;
import vn.edu.fpt.ancu.core.BasePage;

public final class LoginPage extends BasePage {
    private static final By EMAIL = By.id("login-email");
    private static final By PASSWORD = By.id("login-password");
    private static final By SUBMIT = By.xpath("//button[@type='submit'][contains(normalize-space(.),'Đăng nhập')]");
    private static final By ALERT = By.cssSelector("[role='alert']");
    private static final By ACCOUNT_MENU = By.cssSelector("button[aria-label='Vai trò và tài khoản']");
    private static final By LOGOUT = By.id("btn-logout");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(Config.get("base.url").replaceAll("/+$", "") + "/login");
        visible(EMAIL);
        return this;
    }

    public LoginPage enterEmail(String value) {
        type(EMAIL, value);
        return this;
    }

    public LoginPage enterPassword(String value) {
        type(PASSWORD, value);
        return this;
    }

    public LoginPage submit() {
        click(SUBMIT);
        return this;
    }

    public LoginPage login(String email, String password) {
        return enterEmail(email).enterPassword(password).submit();
    }

    public String alert() {
        return text(ALERT);
    }

    public boolean emailBrowserValid() {
        return browserValid(EMAIL);
    }

    public boolean passwordBrowserValid() {
        return browserValid(PASSWORD);
    }

    public String passwordType() {
        return present(PASSWORD).getAttribute("type");
    }

    public String passwordValue() {
        return present(PASSWORD).getAttribute("value");
    }

    public LoginPage togglePassword() {
        By toggle = By.cssSelector("button[aria-label='Hiện mật khẩu'],button[aria-label='Ẩn mật khẩu']");
        click(toggle);
        return this;
    }

    public boolean submitDisabled() {
        return !present(SUBMIT).isEnabled();
    }

    public LoginPage waitUntilAuthenticated() {
        waitUrlNotContains("/login");
        visible(ACCOUNT_MENU);
        return this;
    }

    public boolean accountMenuVisible() {
        return !all(ACCOUNT_MENU).isEmpty() && all(ACCOUNT_MENU).get(0).isDisplayed();
    }

    public LoginPage logout() {
        click(ACCOUNT_MENU);
        click(LOGOUT);
        wait.until(driver -> driver.findElements(ACCOUNT_MENU).isEmpty());
        return this;
    }

    public LoginPage waitUntilAtLogin() {
        wait.until(ExpectedConditions.urlContains("/login"));
        visible(EMAIL);
        return this;
    }
}
