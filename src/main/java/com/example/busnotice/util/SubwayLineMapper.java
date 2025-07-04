package com.example.busnotice.util;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SubwayLineMapper {

    private static final Map<String, String> codeToName = Map.ofEntries(
            Map.entry("1001", "1호선"),
            Map.entry("1002", "2호선"),
            Map.entry("1003", "3호선"),
            Map.entry("1004", "4호선"),
            Map.entry("1005", "5호선"),
            Map.entry("1006", "6호선"),
            Map.entry("1007", "7호선"),
            Map.entry("1008", "8호선"),
            Map.entry("1009", "9호선"),
            Map.entry("1092", "우이신설경전철"),
            Map.entry("1067", "경춘선"),
            Map.entry("1063", "경의중앙"),
            Map.entry("1075", "수인분당선"),
            Map.entry("1077", "신분당"),
            Map.entry("1065", "공항철도")
            // 필요하면 더 추가
    );

    private static final Map<String, String> nameToCode = Map.ofEntries(
            Map.entry("1호선", "1001"),
            Map.entry("2호선", "1002"),
            Map.entry("3호선", "1003"),
            Map.entry("4호선", "1004"),
            Map.entry("5호선", "1005"),
            Map.entry("6호선", "1006"),
            Map.entry("7호선", "1007"),
            Map.entry("8호선", "1008"),
            Map.entry("9호선", "1009"),
            Map.entry("우이신설경전철", "1092"),
            Map.entry("경춘선", "1067"),
            Map.entry("경의중앙", "1063"),
            Map.entry("수인분당선", "1075"),
            Map.entry("신분당", "1077"),
            Map.entry("공항철도", "1065")
            // 필요하면 더 추가
    );

    public static String getLineNameByCode(String code) {
        return codeToName.getOrDefault(code, "알 수 없음");
    }

    public static String getLineCodeByName(String name) {
        return nameToCode.getOrDefault(name, "알 수 없음");
    }
}
