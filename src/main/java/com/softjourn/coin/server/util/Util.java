package com.softjourn.coin.server.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.softjourn.coin.server.exceptions.WrongMimeTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class Util {

    public static <T> MappingIterator<T> getDataFromCSV(byte[] file, Class<T> tClass) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(tClass).withHeader().withColumnSeparator(',');
        ObjectReader r = mapper.readerFor(tClass).with(schema);
        return r.readValues(file);
    }

    public static <T> void dataToCSV(Writer writer, List<T> list, Class<T> tClass) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(tClass).withHeader().withColumnSeparator(',');
        ObjectWriter objectWriter = mapper.writerFor(tClass).with(schema);
        objectWriter.writeValuesAsArray(writer).writeAll(list).flush();

    }

    public static void validateMultipartFileMimeType(MultipartFile multipartFile, String pattern) {
        if (!multipartFile.getContentType().matches(pattern)) {
            throw new WrongMimeTypeException(String.format("File's extension is not %s", pattern));
        }
    }

    public static String instantToRFC_1123_DATE_TIME(Instant instant) {
        return DateTimeFormatter
                .RFC_1123_DATE_TIME
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.of("+0"))
                .format(instant);
    }

}
