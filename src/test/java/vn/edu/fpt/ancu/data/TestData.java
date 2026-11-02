package vn.edu.fpt.ancu.data;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TestData {
    private TestData() {
    }

    public static Map<String, String> resolvedRow(String resource, String tcId) {
        Map<String, String> source = CsvData.row(resource, tcId);
        DynamicData.Context context = DynamicData.context();
        Map<String, String> resolved = new LinkedHashMap<>();
        source.forEach((key, value) -> resolved.put(key, DynamicData.resolve(value, context)));
        return Map.copyOf(resolved);
    }
}
