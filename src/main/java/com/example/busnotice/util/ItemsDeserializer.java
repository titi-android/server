package com.example.busnotice.util;

import com.example.busnotice.domain.busStop.res.BusStopsDto;
import com.example.busnotice.domain.busStop.res.BusStopsDto.Item;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemsDeserializer extends JsonDeserializer<List<Item>> {

    @Override
    public List<Item> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        List<BusStopsDto.Item> items = new ArrayList<>();

        if (p.currentToken() == JsonToken.START_ARRAY) {
            // JSON이 배열인 경우
            items = mapper.readValue(p, mapper.getTypeFactory()
                .constructCollectionType(List.class, BusStopsDto.Item.class));
        } else if (p.currentToken() == JsonToken.START_OBJECT) {
            // JSON이 단일 객체인 경우
            BusStopsDto.Item item = mapper.readValue(p, BusStopsDto.Item.class);
            items.add(item);
        }

        return items;
    }
}