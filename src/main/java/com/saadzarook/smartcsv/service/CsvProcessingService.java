package com.saadzarook.smartcsv.service;

import com.saadzarook.smartcsv.annotation.SmartCsvField;
import com.saadzarook.smartcsv.annotation.SmartCsvProcessor;
import com.saadzarook.smartcsv.utils.CSVParserResult;
import com.saadzarook.smartcsv.utils.CsvParser;
import com.saadzarook.smartcsv.utils.RecordError;
import com.saadzarook.smartcsv.validator.HeaderValidator;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

@Service
public class CsvProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(CsvProcessingService.class);

    /**
     * Processes the CSV file using the specified method and annotations.
     */
    public List<RecordError> processCsv(MultipartFile file, Method method, Object bean) throws Exception {
        SmartCsvProcessor csvProcessor = method.getAnnotation(SmartCsvProcessor.class);
        Class<?> modelClass = csvProcessor.model();
        String validationStrategy = csvProcessor.validationStrategy();
        HeaderValidator headerValidator = csvProcessor.headerValidator().getDeclaredConstructor().newInstance();

        List<String> headers;
        List<Map<String, String>> records;

        // Step 1: Parse CSV headers and records
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParserResult parserResult = CsvParser.parse(reader);
            headers = parserResult.getHeaders();
            records = parserResult.getRecords();
        }

        // Step 2: Validate Headers
        List<String> headerErrors = headerValidator.validateHeaders(headers);
        if (!headerErrors.isEmpty()) {
            logger.error("Header validation failed: {}", headerErrors);
            throw new Exception("Header validation failed: " + headerErrors);
        }

        // Step 3: Prepare for parallel processing
        List<RecordError> errorList = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();

        // Step 4: Process Records in Parallel
        for (int i = 0; i < records.size(); i++) {
            int recordNumber = i + 1;
            Map<String, String> recordData = records.get(i);

            Future<?> future = executorService.submit(() -> {
                try {
                    Object modelInstance = mapRecordToModel(recordData, modelClass);
                    // Invoke the user's method with the model instance
                    method.invoke(bean, modelInstance);
                } catch (Exception e) {
                    String errorMessage = String.format("Error processing record %d: %s", recordNumber, e.getMessage());
                    logger.error(errorMessage);
                    errorList.add(new RecordError(recordNumber, errorMessage));

                    if ("stop".equalsIgnoreCase(validationStrategy)) {
                        throw new RuntimeException(errorMessage);
                    }
                }
            });

            futures.add(future);
        }

        // Step 5: Await Completion
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                // If strategy is "stop", rethrow the exception to halt processing
                if ("stop".equalsIgnoreCase(validationStrategy)) {
                    executorService.shutdownNow();
                    throw e;
                }
                // Otherwise, continue processing other records
            }
        }

        executorService.shutdown();

        // Step 6: Return Errors if any
        if ("collect".equalsIgnoreCase(validationStrategy) && !errorList.isEmpty()) {
            return errorList;
        }

        return Collections.emptyList();
    }

    /**
     * Maps a CSV record to the specified model class using reflection.
     */
    private Object mapRecordToModel(Map<String, String> recordData, Class<?> modelClass) throws Exception {
        Object modelInstance = modelClass.getDeclaredConstructor().newInstance();
        Field[] fields = modelClass.getDeclaredFields();

        for (Field field : fields) {
            SmartCsvField csvField = field.getAnnotation(SmartCsvField.class);
            if (csvField != null) {
                String columnName = csvField.column();
                String value = recordData.get(columnName);

                // Handle required fields
                if (csvField.required() && (value == null || value.isEmpty())) {
                    throw new Exception(String.format("Field '%s' is required", columnName));
                }

                // Handle custom validation
                if (!csvField.validation().isEmpty()) {
                    if (!value.matches(csvField.validation())) {
                        throw new Exception(String.format("Field '%s' does not match validation pattern", columnName));
                    }
                }

                // Set the value to the model instance
                field.setAccessible(true);
                field.set(modelInstance, convertValue(field.getType(), value));
            }
        }

        return modelInstance;
    }

    /**
     * Converts a String value to the specified type.
     */
    private Object convertValue(Class<?> fieldType, String value) {
        if (fieldType.equals(String.class)) {
            return value;
        } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
            return Double.parseDouble(value);
        }
        return null;
    }
}
