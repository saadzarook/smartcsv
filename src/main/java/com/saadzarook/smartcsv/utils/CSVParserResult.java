package com.saadzarook.smartcsv.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class CSVParserResult {
    private List<String> headers;
    private List<Map<String, String>> records;
}
