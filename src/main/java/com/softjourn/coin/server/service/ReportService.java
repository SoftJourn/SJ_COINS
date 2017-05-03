package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.ReportDefiner;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface ReportService {

    <T> Workbook toReport(String name, List<T> entities, List<ReportDefiner> definers) throws NoSuchFieldException, IllegalAccessException;

}
