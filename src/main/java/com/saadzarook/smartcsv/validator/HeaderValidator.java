package com.saadzarook.smartcsv.validator;

import java.util.List;

public interface HeaderValidator {
    /**
     * Validates the CSV headers.
     * @param headers The list of headers from the CSV file.
     * @return A list of error messages if validation fails.
     */
    List<String> validateHeaders(List<String> headers);
}