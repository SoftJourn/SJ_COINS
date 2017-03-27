package com.softjourn.coin.server.util;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.softjourn.coin.server.entity.Account;

import java.io.IOException;

public class TransactionAccountJSONSerializer extends JsonSerializer<Account> {
    @Override
    public void serialize(Account value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeString(value.getFullName());
    }
}
