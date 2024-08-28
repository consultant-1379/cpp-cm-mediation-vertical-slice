package com.ericsson.oss.mediation.testcase.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cucumber.api.DataTable;

public class TestUtils {

    public static List<Map<String, String>> toMapList(final DataTable dataTable) {
        return toMapList(dataTable.raw());
    }

    public static List<Map<String, String>> toMapList(final List<List<String>> rawList) {
        final List<Map<String, String>> result = new ArrayList<>();
        final List<String> headers = rawList.get(0);
        for (int i = 1; i < rawList.size(); i++) {
            final Map<String, String> entry = new HashMap<>();
            final List<String> cells = rawList.get(i);
            for (int j = 0; j < cells.size(); j++) {
                entry.put(headers.get(j), cells.get(j));
            }

            result.add(entry);
        }

        return result;
    }

}