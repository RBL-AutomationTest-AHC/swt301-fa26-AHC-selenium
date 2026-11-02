package vn.edu.fpt.ancu.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CsvData {
    private CsvData() {
    }

    public static Map<String, String> row(String resource, String tcId) {
        List<Map<String, String>> rows = read(resource);
        return rows.stream()
                .filter(value -> tcId.equals(value.get("tcId")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy " + tcId + " trong " + resource));
    }

    public static List<Map<String, String>> read(String resource) {
        try (InputStream stream = CsvData.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalArgumentException("Không tìm thấy resource: " + resource);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                List<String> headers = parse(requiredLine(reader, resource));
                List<Map<String, String>> rows = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    List<String> values = parse(line);
                    if (values.size() != headers.size()) {
                        throw new IllegalArgumentException("Sai số cột CSV trong " + resource + ": " + line);
                    }
                    Map<String, String> row = new LinkedHashMap<>();
                    for (int index = 0; index < headers.size(); index++) {
                        row.put(headers.get(index), values.get(index));
                    }
                    rows.add(Map.copyOf(row));
                }
                return List.copyOf(rows);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Không đọc được CSV: " + resource, exception);
        }
    }

    private static String requiredLine(BufferedReader reader, String resource) throws IOException {
        String line = reader.readLine();
        if (line == null) throw new IllegalArgumentException("CSV rỗng: " + resource);
        return line.startsWith("\uFEFF") ? line.substring(1) : line;
    }

    private static List<String> parse(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char value = line.charAt(index);
            if (value == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (value == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(value);
            }
        }
        if (quoted) throw new IllegalArgumentException("CSV có quote chưa đóng: " + line);
        values.add(current.toString());
        return values;
    }
}
