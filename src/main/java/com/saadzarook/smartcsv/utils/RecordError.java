package com.saadzarook.smartcsv.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RecordError {
    private int recordNumber;
    private String errorMessage;
}
