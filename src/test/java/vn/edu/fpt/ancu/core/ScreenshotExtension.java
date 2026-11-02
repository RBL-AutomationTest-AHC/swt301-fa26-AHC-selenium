package vn.edu.fpt.ancu.core;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.opentest4j.TestAbortedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotExtension implements AfterTestExecutionCallback {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    @Override
    public void afterTestExecution(ExtensionContext context) throws IOException {
        if (context.getExecutionException().isEmpty()
                || context.getExecutionException().get() instanceof TestAbortedException) {
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
        try {
            Path directory = Path.of(vn.edu.fpt.ancu.config.Config.optional("screenshot.directory", "target/screenshots"));
            Files.createDirectories(directory);
            Path destination = directory.resolve(tcId + "__FAIL__" + STAMP.format(LocalDateTime.now()) + ".png");
            Files.write(destination, camera.getScreenshotAs(OutputType.BYTES));
        } catch (IOException | RuntimeException failure) {
            // Keep the original assertion/driver failure as the primary failure.
            context.getExecutionException().get().addSuppressed(failure);
        }
    }
}
