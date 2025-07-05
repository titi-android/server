package com.example.busnotice.domain.subway;

public enum LineDir {
    UP("상행"),
    DOWN("하행");

    private final String lineDir;

    LineDir(String lineDir) {
        this.lineDir = lineDir;
    }

    // 한글 → Enum 변환 메서드
    public static LineDir fromDisplayName(String displayName) {
        if ("UP".equalsIgnoreCase(displayName) || "상행".equals(displayName)) {
            return UP;
        }
        if ("DOWN".equalsIgnoreCase(displayName) || "하행".equals(displayName)) {
            return DOWN;
        }
        throw new IllegalArgumentException("지원하지 않는 방향: " + displayName);
    }

    public String get() {
        return lineDir;
    }

}
