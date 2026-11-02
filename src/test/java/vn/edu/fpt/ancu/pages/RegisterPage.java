package vn.edu.fpt.ancu.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import vn.edu.fpt.ancu.config.Config;
import vn.edu.fpt.ancu.core.BasePage;

import java.net.URI;
import java.util.Map;

public final class RegisterPage extends BasePage {
    private final By fullName = By.id("register-name");
    private final By email = By.id("register-email");
    private final By password = By.id("register-password");
    private final By confirmation = By.id("register-confirm");
    private final By phone = By.id("register-phone");
    private final By submit = By.cssSelector("form button[type='submit']");
    private final By alert = By.cssSelector("[role='alert']");
    private final By createdNotice = By.xpath("//*[@role='status'][contains(.,'Đăng ký thành công') or contains(.,'Tài khoản đã được tạo')]");
    private final By accountMenu = By.cssSelector("button[aria-label='Vai trò và tài khoản']");
    private final By verificationEmail = By.id("email-new");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public RegisterPage open() {
        driver.get(Config.get("base.url").replaceAll("/+$", "") + "/register");
        visible(fullName);
        return this;
    }

    public RegisterPage fill(Map<String, String> data) {
        type(fullName, data.get("fullName"));
        type(email, data.get("email"));
        type(phone, data.get("phone"));
        type(password, data.get("password"));
        type(confirmation, data.get("confirmPassword"));
        return this;
    }

    public RegisterPage submit() {
        click(submit);
        return this;
    }

    public boolean fieldValid(String field) {
        return browserValid(switch (field) {
            case "fullName" -> fullName;
            case "email" -> email;
            case "password" -> password;
            case "confirmPassword" -> confirmation;
            case "phone" -> phone;
            default -> throw new IllegalArgumentException("Unknown Register field: " + field);
        });
    }

    public String alert() {
        return text(alert);
    }

    public boolean atRegister() {
        return path().equals("/register");
    }

    public boolean authenticatedMenuVisible() {
        return all(accountMenu).stream().anyMatch(element -> element.isDisplayed());
    }

    public String waitForCreatedNotice() {
        wait.until(driver -> path().equals("/account/settings/email") || path().equals("/login"));
        return text(createdNotice);
    }

    public boolean atVerification() {
        return path().equals("/account/settings/email");
    }

    public String verificationEmail() {
        wait.until(driver -> !present(verificationEmail).getAttribute("value").isBlank());
        return present(verificationEmail).getAttribute("value");
    }

    public boolean atCreatedAccountLogin() {
        return path().equals("/login");
    }

    public void waitUntilSubmissionFinished() {
        wait.until(ExpectedConditions.elementToBeClickable(submit));
    }

    private String path() {
        return URI.create(driver.getCurrentUrl()).getPath().replaceAll("/+$", "");
    }
}
