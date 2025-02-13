package com.example.busnotice.domain.schedule;

import lombok.Data;

@Data
public class BusInfo {

    private String name;
    private String type;

    public BusInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }
}
