package com.example.busnotice.domain.subway;

import lombok.Getter;

@Getter
public enum LineType {
    LINE_1("1호선"),
    LINE_2("2호선"),
    LINE_3("3호선"),
    LINE_4("4호선"),
    LINE_5("5호선"),
    LINE_6("6호선"),
    LINE_7("7호선"),
    LINE_8("8호선"),
    LINE_9("9호선");

    private final String displayName;

    LineType(String displayName) {
        this.displayName = displayName;
    }

    // 한글 → Enum 변환 메서드
    public static LineType fromDisplayName(String displayName) {
        for (LineType type : LineType.values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 노선명: " + displayName);
    }

    public String get() {
        return displayName;
    }
}
