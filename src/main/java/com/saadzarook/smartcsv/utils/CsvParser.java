package com.saadzarook.smartcsv.utils;

import org.slf4j.*;
import java.io.*;
import java.util.*;

public class CsvParser {

    private static final Logger logger = LoggerFactory.getLogger(CsvParser.class);

    public static CSVParserResult parse(Reader reader) throws IOException {
        List<String> headers = new ArrayList<>();
        List<Map<String, String>> records = new ArrayList<>();

        BufferedReader br = new BufferedReader(reader);
        String line;
        String[] headerArray = null;

        // Read headers
        if ((line = br.readLine()) != null) {
            headerArray = line.split(",");
            headers.addAll(Arrays.asList(headerArray));
        }

        // Read records
        int lineNumber = 1;
        while ((line = br.readLine()) != null) {
            lineNumber++;
            String[] values = line.split(",");
            if (values.length != headerArray.length) {
                logger.warn("Line {} has incorrect number of columns", lineNumber);
                continue; // Skip malformed lines
            }
            Map<String, String> record = new HashMap<>();
            for (int i = 0; i < headerArray.length; i++) {
                record.put(headerArray[i], values[i]);
            }
            records.add(record);
        }

        return new CSVParserResult(headers, records);
    }
}
