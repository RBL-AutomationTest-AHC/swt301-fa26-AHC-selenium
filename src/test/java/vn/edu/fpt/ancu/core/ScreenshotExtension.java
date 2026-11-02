package vn.edu.fpt.ancu.core;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotExtension implements AfterTestExecutionCallback {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    @Override
    public void afterTestExecution(ExtensionContext context) throws IOException {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        Object instance = context.getRequiredTestInstance();
        if (!(instance instanceof BaseTest test)) {
            return;
        }
        WebDriver driver = test.driverForScreenshot();
        if (!(driver instanceof TakesScreenshot camera)) {
            return;
        }
        String tcId = context.getDisplayName().replaceAll("[^A-Za-z0-9_-]+", "-");
        Path directory = Path.of("target", "screenshots");
        Files.createDirectories(directory);
        Path destination = directory.resolve(tcId + "__FAIL__" + STAMP.format(LocalDateTime.now()) + ".png");
        Files.copy(camera.getScreenshotAs(OutputType.FILE).toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
    }
}
