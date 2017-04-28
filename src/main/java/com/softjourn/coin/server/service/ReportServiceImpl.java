package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.ReportDefiner;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static com.softjourn.coin.server.util.Util.instantToRFC_1123_DATE_TIME;
import static com.softjourn.common.utils.ReflectionUtil.tryToCastValue;

@Service
public class ReportServiceImpl implements ReportService {

    /**
     * Method creates excel sheet and writes transactions into sheet
     *
     * @param name     - sheet name
     * @param entities - data
     * @param definers - defines data to be recorded
     * @return Workbook
     */
    @Override
    public <T> Workbook toReport(String name, List<T> entities, List<ReportDefiner> definers)
            throws NoSuchFieldException, IllegalAccessException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(name);

        HSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setFontName("Calibri");

        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        int columns = 0;
        // header
        if (definers != null) {
            Row header = sheet.createRow(0);
            columns = prepareHeaders(header, style, 0, definers);
        }

        // main content
        if (entities != null) {
            for (int i = 0; i < entities.size(); i++) {
                Row content = sheet.createRow(i + 1);
                columns = prepareContent(content, style, 0, definers, entities.get(i));
            }
        }

        // auto size columns width
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    /**
     * Method prepares cell and sets data and style into cell
     *
     * @param row    - row where is needed to add content
     * @param style  - content style
     * @param column - column index
     * @param value  - value to set
     * @return Cell
     */
    private Cell prepareCell(Row row, CellStyle style, Integer column, Object value) {
        Cell cell = row.createCell(column);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Date)
            cell.setCellValue((Date) value);
        else if (value instanceof Instant)
            cell.setCellValue(instantToRFC_1123_DATE_TIME((Instant) value));
        else if (value instanceof Number || value.getClass().isPrimitive())
            cell.setCellValue((Double) tryToCastValue(Double.class, value));
        else if (value.getClass().isInstance(true))
            cell.setCellValue((Boolean) tryToCastValue(Boolean.class, value));
        else cell.setCellValue(value.toString());

        cell.setCellStyle(style);

        return cell;
    }

    /**
     * Method adds header into row reading definer of entity
     *
     * @param row      - row where is needed to add content
     * @param style    - content style
     * @param index    - number of column from where is needed to start filling
     * @param definers - entity definer
     * @return -  int - is needed for recursion
     */
    private int prepareHeaders(Row row, CellStyle style, int index, List<ReportDefiner> definers) {
        for (ReportDefiner definer : definers) {
            if (definer.getDefiners().size() == 0 && definer.getHeader() != null) {
                prepareCell(row, style, index++, definer.getHeader());
            } else {
                index = prepareHeaders(row, style, index, definer.getDefiners());
            }
        }
        return index;
    }

    /**
     * Method adds content into row reading entity and definer of entity
     *
     * @param row      - row where is needed to add content
     * @param style    - content style
     * @param index    - number of column from where is needed to start filling
     * @param definers - entity definer
     * @param entity   - data
     * @param <T>      - data class
     * @return -  int - is needed for recursion
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private <T> int prepareContent(Row row, CellStyle style, int index, List<ReportDefiner> definers, T entity) throws NoSuchFieldException, IllegalAccessException {
        for (ReportDefiner definer : definers) {
            if (entity == null) {
                prepareCell(row, style, index++, null);
            } else {
                Class<?> aClass = entity.getClass();
                Field field = aClass.getDeclaredField(definer.getFieldName());
                field.setAccessible(true);
                if (definer.getDefiners().size() == 0 && definer.getHeader() != null) {
                    prepareCell(row, style, index++, field.get(entity));
                } else {
                    index = prepareContent(row, style, index, definer.getDefiners(), field.get(entity));
                }
            }
        }
        return index;
    }

}
