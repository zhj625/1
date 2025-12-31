package com.library.service;

import com.library.dto.response.BorrowRecordResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface ExcelExportService {

    /**
     * 导出借阅记录到 Excel
     */
    void exportBorrowRecords(List<BorrowRecordResponse> records, HttpServletResponse response);
}
