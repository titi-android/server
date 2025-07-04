package com.example.busnotice.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.busnotice.domain.bus.res.BusArrInfosDto.Item;

public class BusArrInfosDtoItemsDeserializer extends JsonDeserializer<List<Item>> {


    @Override
    public List<Item> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        List<Item> items = new ArrayList<>();

        if (p.currentToken() == JsonToken.START_ARRAY) {
            // JSON이 배열인 경우
            items = mapper.readValue(p, mapper.getTypeFactory()
                    .constructCollectionType(List.class, Item.class));
        } else if (p.currentToken() == JsonToken.START_OBJECT) {
            // JSON이 단일 객체인 경우
            Item item = mapper.readValue(p, Item.class);
            items.add(item);
        }

        return items;
    }
}
