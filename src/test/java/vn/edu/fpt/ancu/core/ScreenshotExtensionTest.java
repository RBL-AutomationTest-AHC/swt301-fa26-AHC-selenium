package vn.edu.fpt.ancu.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ScreenshotExtensionTest {
    @TempDir Path directory;

    @Test
    void capturesFailedTestBeforeDriverTeardownAndPreservesOriginalFailure() throws Exception {
        String previous = System.getProperty("screenshot.directory");
        System.setProperty("screenshot.directory", directory.toString());
        try {
            var calls = new AtomicInteger();
            BaseTest test = new BaseTest() { };
            byte[] png = { (byte) 137, 80, 78, 71 };
            test.driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{WebDriver.class, TakesScreenshot.class}, (proxy, method, args) -> {
                        if (method.getName().equals("getScreenshotAs")) { calls.incrementAndGet(); return png; }
                        throw new IllegalStateException("Unexpected driver call: " + method.getName());
                    });
            var extension = new ScreenshotExtension();
            extension.afterTestExecution(context(test, null));
            extension.afterTestExecution(context(test, new TestAbortedException("Missing fixture")));
            assertEquals(0, calls.get());
            extension.afterTestExecution(context(test, new AssertionError("Original failure")));
            assertEquals(1, calls.get());
            try (var files = Files.list(directory)) {
                Path screenshot = files.findFirst().orElseThrow();
                assertTrue(screenshot.getFileName().toString().startsWith("TC-REG-001__FAIL__"));
                assertArrayEquals(png, Files.readAllBytes(screenshot));
            }

            test.driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{WebDriver.class, TakesScreenshot.class}, (proxy, method, args) -> {
                        throw new IllegalStateException("Browser disconnected");
                    });
            var failure = new AssertionError("Original assertion");
            assertDoesNotThrow(() -> extension.afterTestExecution(context(test, failure)));
            assertEquals(1, failure.getSuppressed().length);
        } finally {
            if (previous == null) System.clearProperty("screenshot.directory");
            else System.setProperty("screenshot.directory", previous);
        }
    }

    private ExtensionContext context(BaseTest test, Throwable failure) {
        return (ExtensionContext) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ExtensionContext.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getExecutionException" -> Optional.ofNullable(failure);
                    case "getRequiredTestInstance" -> test;
                    case "getDisplayName" -> "TC-REG-001";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }
}
