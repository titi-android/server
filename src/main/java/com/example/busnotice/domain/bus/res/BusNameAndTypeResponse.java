package com.example.busnotice.domain.bus.res;

import java.io.Serializable;

public record BusNameAndTypeResponse(
        String name,
        String type
) implements Serializable {

}
