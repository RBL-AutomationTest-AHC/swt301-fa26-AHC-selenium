package vn.edu.fpt.ancu.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @TempDir Path directory;

    @Test
    void explicitEnvironmentFixtureOverridesBackendFileAlias() throws Exception {
        Path file = directory.resolve("fixture.env");
        Files.writeString(file, "LOCAL_CUSTOMER_EMAIL=file@example.test\n");
        assertEquals("env@example.test", probe(file, Map.of("ACTIVE_EMAIL", "env@example.test"), null));
        assertEquals("jvm@example.test", probe(file, Map.of("ACTIVE_EMAIL", "env@example.test"), "jvm@example.test"));
        assertEquals("file@example.test", probe(file, Map.of(), null));
    }

    @Test
    void frameworkLoadsWithoutMachineSpecificEnvironmentFile() throws Exception {
        assertEquals("http://localhost:3000", probe(null, Map.of(), null));
    }

    @Test
    void localConfigurationLoadsFixtureWithoutRunOptionsAndExplicitOptionStillWins() throws Exception {
        Path localFixture = directory.resolve("local.env");
        Files.writeString(localFixture, "LOCAL_CUSTOMER_EMAIL=local@example.test\n");
        Files.writeString(directory.resolve("config.local.properties"),
                "sut.env.file=" + localFixture.toString().replace('\\', '/') + "\n");
        assertEquals("local@example.test", probe(null, Map.of(), null, "fixture"));
        Path explicitFixture = directory.resolve("explicit.env");
        Files.writeString(explicitFixture, "LOCAL_CUSTOMER_EMAIL=explicit@example.test\n");
        assertEquals("explicit@example.test", probe(explicitFixture, Map.of(), null));
    }

    private String probe(Path file, Map<String, String> environment, String override) throws Exception {
        return probe(file, environment, override, file == null ? "url" : "fixture");
    }

    private String probe(Path file, Map<String, String> environment, String override, String mode) throws Exception {
        var command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Dfile.encoding=UTF-8", "-cp", System.getProperty("java.class.path")));
        if (file != null) command.add("-Dsut.env.file=" + file);
        if (override != null) command.add("-DACTIVE_EMAIL=" + override);
        command.add(Probe.class.getName());
        command.add(mode);
        var builder = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true);
        for (String key : List.of("SUT_ENV_FILE", "ACTIVE_EMAIL", "LOCAL_CUSTOMER_EMAIL", "BASE_URL", "JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS")) {
            builder.environment().remove(key);
        }
        builder.environment().putAll(environment);
        var process = builder.start();
        if (!process.waitFor(20, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            fail("Config probe timed out");
        }
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip();
        assertEquals(0, process.exitValue(), output);
        return output;
    }

    public static class Probe {
        public static void main(String[] args) {
            System.out.print(args[0].equals("url") ? Config.get("base.url") : Config.fixture("ACTIVE_EMAIL"));
        }
    }
}
