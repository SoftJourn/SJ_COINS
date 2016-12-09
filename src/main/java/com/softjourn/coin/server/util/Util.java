package com.softjourn.coin.server.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.softjourn.coin.server.exceptions.WrongMimeTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class Util {

    public static <T> MappingIterator<T> getDataFromCSV(byte[] file, Class<T> tClass) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(tClass).withHeader().withColumnSeparator(',');
        ObjectReader r = mapper.readerFor(tClass).with(schema);
        return r.readValues(file);
    }

    public static void validateMultipartFileMimeType(MultipartFile multipartFile, String pattern) {
        if (!multipartFile.getContentType().matches(pattern)) {
            throw new WrongMimeTypeException(String.format("File's extension is not %s", pattern));
        }
    }

}
