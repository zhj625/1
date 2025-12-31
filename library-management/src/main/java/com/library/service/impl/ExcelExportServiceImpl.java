package com.library.service.impl;

import com.library.dto.response.BorrowRecordResponse;
import com.library.service.ExcelExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void exportBorrowRecords(List<BorrowRecordResponse> records, HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("借阅记录");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            // 创建数据样式
            CellStyle dataStyle = createDataStyle(workbook);

            // 创建表头
            String[] headers = {"订单号", "用户名", "真实姓名", "图书名称", "ISBN", "作者",
                               "借阅日期", "应还日期", "实际归还日期", "状态", "是否逾期", "备注"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (BorrowRecordResponse record : records) {
                Row row = sheet.createRow(rowNum++);

                createCell(row, 0, record.getId() != null ? record.getId().toString() : "", dataStyle);
                createCell(row, 1, record.getUsername() != null ? record.getUsername() : "", dataStyle);
                createCell(row, 2, record.getRealName() != null ? record.getRealName() : "", dataStyle);
                createCell(row, 3, record.getBookTitle() != null ? record.getBookTitle() : "", dataStyle);
                createCell(row, 4, record.getBookIsbn() != null ? record.getBookIsbn() : "", dataStyle);
                createCell(row, 5, record.getBookAuthor() != null ? record.getBookAuthor() : "", dataStyle);
                createCell(row, 6, formatDateTime(record.getBorrowDate()), dataStyle);
                createCell(row, 7, formatDateTime(record.getDueDate()), dataStyle);
                createCell(row, 8, formatDateTime(record.getReturnDate()), dataStyle);
                createCell(row, 9, record.getStatusDesc() != null ? record.getStatusDesc() : "", dataStyle);
                createCell(row, 10, record.getOverdue() != null && record.getOverdue() ? "是" : "否", dataStyle);
                createCell(row, 11, record.getRemark() != null ? record.getRemark() : "", dataStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小宽度
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.max(width, 3000));
            }

            // 设置响应头
            String fileName = "借阅记录_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            // 写入响应流
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();

        } catch (IOException e) {
            log.error("导出 Excel 失败", e);
            throw new RuntimeException("导出 Excel 失败: " + e.getMessage());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_FORMATTER);
    }
}
