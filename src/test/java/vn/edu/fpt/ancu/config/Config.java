package vn.edu.fpt.ancu.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public final class Config {
    private static final Properties PROPERTIES = loadProperties();
    private static final Map<String, String> ENV_ALIASES = Map.of(
            "ACTIVE_EMAIL", "LOCAL_CUSTOMER_EMAIL",
            "ACTIVE_PASSWORD", "LOCAL_CUSTOMER_PASSWORD",
            "EXISTING_EMAIL", "LOCAL_CUSTOMER_EMAIL"
    );
    private static final Properties FILE_ENV = loadEnvironmentFile();

    private Config() {
    }

    public static String get(String key) {
        String value = externalValue(key, toEnvironmentKey(key));
        if (hasText(value)) return value.trim();
        String propertyValue = PROPERTIES.getProperty(key);
        if (hasText(propertyValue)) return propertyValue.trim();
        throw new IllegalStateException("Thiếu cấu hình bắt buộc: " + key);
    }

    public static String optional(String key, String fallback) {
        try {
            return get(key);
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    public static boolean bool(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static int integer(String key) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Cấu hình phải là số nguyên: " + key, exception);
        }
    }

    public static String requireSecret(String key) {
        String value = testValue(key);
        if (!hasText(value)) {
            throw new IllegalStateException("Thiếu secret cục bộ: " + key
                    + ". Hãy cấu hình trực tiếp hoặc thêm khóa tương ứng vào file SUT_ENV_FILE.");
        }
        return value;
    }

    public static String fixture(String key) {
        String value = testValue(key);
        if (!hasText(value)) throw new IllegalStateException("Thiếu fixture: " + key
                + ". Hãy nạp file fixture bằng -Dsut.env.file=<path> hoặc SUT_ENV_FILE; "
                + "nếu chạy IntelliJ, thêm VM option này vào đúng cấu hình Run đang dùng.");
        return value.trim();
    }

    public static boolean hasFixture(String key) {
        return hasText(testValue(key));
    }

    private static String testValue(String key) {
        String value = System.getProperty(key);
        if (hasText(value)) return value;
        value = System.getenv(key);
        if (hasText(value) && !looksLikePastedTemplate(value)) return value;
        String alias = ENV_ALIASES.get(key);
        if (alias != null) {
            value = System.getenv(alias);
            if (hasText(value) && !looksLikePastedTemplate(value)) return value;
            value = FILE_ENV.getProperty(alias);
            if (hasText(value)) return value;
        }
        return FILE_ENV.getProperty(key);
    }

    private static boolean looksLikePastedTemplate(String value) {
        return value.contains("ACTIVE_PASSWORD=") || value.contains("EXISTING_EMAIL=")
                || value.contains("LOCKED_EMAIL=") || value.contains("LOCKED_PASSWORD=")
                || value.contains("<email") || value.contains("<password");
    }

    private static String externalValue(String systemKey, String environmentKey) {
        String value = System.getProperty(systemKey);
        if (!hasText(value)) value = System.getenv(environmentKey);
        if (!hasText(value)) value = FILE_ENV.getProperty(environmentKey);
        return value;
    }

    private static String toEnvironmentKey(String key) {
        return key.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_');
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = Config.class.getResourceAsStream("/config.properties")) {
            if (stream == null) throw new IllegalStateException("Không tìm thấy /config.properties");
            properties.load(stream);
            Path local = Path.of("config.local.properties");
            if (Files.isRegularFile(local)) {
                try (var reader = Files.newBufferedReader(local, StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }
            }
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Không đọc được config.properties hoặc config.local.properties", exception);
        }
    }

    private static Properties loadEnvironmentFile() {
        Properties values = new Properties();
        String configured = System.getProperty("sut.env.file");
        if (!hasText(configured)) configured = System.getenv("SUT_ENV_FILE");
        if (!hasText(configured)) configured = PROPERTIES.getProperty("sut.env.file");
        if (!hasText(configured)) return values;

        Path path = Path.of(configured.trim()).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Không tìm thấy file môi trường SUT: " + path);
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                int separator = trimmed.indexOf('=');
                if (separator <= 0) continue;
                String key = trimmed.substring(0, separator).trim();
                String value = unquote(trimmed.substring(separator + 1).trim());
                values.setProperty(key, value);
            }
            return values;
        } catch (IOException exception) {
            throw new IllegalStateException("Không đọc được file môi trường SUT: " + path, exception);
        }
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
