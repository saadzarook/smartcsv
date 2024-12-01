package com.saadzarook.smartcsv.validator;

import java.util.*;

public class DefaultHeaderValidator implements HeaderValidator {
    @Override
    public List<String> validateHeaders(List<String> headers) {
        // Default implementation assumes all headers are acceptable
        return Collections.emptyList();
    }
}