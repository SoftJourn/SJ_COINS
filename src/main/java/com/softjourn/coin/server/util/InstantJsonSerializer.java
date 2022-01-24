package com.softjourn.coin.server.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class InstantJsonSerializer extends JsonSerializer<Instant> {

  @Override
  public void serialize(
      Instant value, JsonGenerator gen, SerializerProvider serializers
  ) throws IOException {
    gen.writeString(DateTimeFormatter
        .ISO_DATE_TIME
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.of("+0"))
        .format(value)
    );
  }
}
