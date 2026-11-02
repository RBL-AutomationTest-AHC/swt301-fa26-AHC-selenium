package vn.edu.fpt.ancu.data;

import vn.edu.fpt.ancu.config.Config;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DynamicData {
    private static final Pattern TOKEN = Pattern.compile("\\$\\{([A-Z0-9_]+)}");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private DynamicData() {
    }

    public static Context context() {
        return new Context(STAMP.format(LocalDateTime.now()) + "-" + UUID.randomUUID().toString().substring(0, 8));
    }

    public static String resolve(String source, Context context) {
        if (source == null || source.isEmpty()) return "";
        Matcher matcher = TOKEN.matcher(source);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String replacement = value(matcher.group(1), context);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String value(String token, Context context) {
        return switch (token) {
            case "UNIQUE" -> context.unique();
            case "ACTIVE_PASSWORD", "LOCKED_PASSWORD" -> Config.requireSecret(token);
            case "ACTIVE_EMAIL_UPPER" -> Config.fixture("ACTIVE_EMAIL").toUpperCase(Locale.ROOT);
            case "ACTIVE_PASSWORD_CASE_CHANGED" -> changeFirstLetterCase(Config.requireSecret("ACTIVE_PASSWORD"));
            case "PASSWORD_OVER_72_BYTES" -> "é".repeat(37);
            case "PASSWORD_73_BYTES" -> "é".repeat(36) + "a";
            case "VISIBLE_TITLE_UPPER" -> Config.fixture("VISIBLE_TITLE").toUpperCase(Locale.ROOT);
            default -> Config.fixture(token);
        };
    }

    private static String changeFirstLetterCase(String value) {
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (Character.isLetter(character)) {
                char changed = Character.isUpperCase(character)
                        ? Character.toLowerCase(character) : Character.toUpperCase(character);
                return value.substring(0, index) + changed + value.substring(index + 1);
            }
        }
        throw new IllegalStateException("ACTIVE_PASSWORD cần ít nhất một chữ cái cho test phân biệt hoa/thường");
    }

    public record Context(String unique) {
    }
}
