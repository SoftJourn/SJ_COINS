package com.softjourn.coin.server.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReportDefiner {

    private final String fieldName;

    /**
     * Column name that will be used in report header
     */
    private final String header;

    /**
     * This list is for nested objects
     */
    List<ReportDefiner> definers;

    public ReportDefiner(String fieldName, String header) {
        this.fieldName = fieldName;
        this.header = header;
        definers = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "ReportDefiner{" +
                "fieldName='" + fieldName + '\'' +
                ", header='" + header + '\'' +
                ", definers=" + definers +
                '}';
    }
}
