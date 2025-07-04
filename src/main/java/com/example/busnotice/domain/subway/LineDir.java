package com.example.busnotice.domain.subway;

public enum LineDir {
    UP("상행"),
    DOWN("하행");

    private final String lineDir;

    LineDir(String lineDir) {
        this.lineDir = lineDir;
    }

    public String get() {
        return lineDir;
    }
}
