package com.library.service;

import com.library.common.PageResult;
import com.library.dto.request.BorrowQueryRequest;
import com.library.dto.request.BorrowRequest;
import com.library.dto.response.BorrowRecordResponse;

import java.util.List;

public interface BorrowService {

    BorrowRecordResponse borrowBook(BorrowRequest request);

    BorrowRecordResponse returnBook(Long recordId);

    /**
     * 续借图书（延长归还日期30天，最多续借2次）
     */
    BorrowRecordResponse renewBook(Long recordId);

    /**
     * 缴纳逾期罚款
     */
    BorrowRecordResponse payFine(Long recordId);

    BorrowRecordResponse getRecordById(Long id);

    PageResult<BorrowRecordResponse> getRecords(BorrowQueryRequest request);

    PageResult<BorrowRecordResponse> getMyRecords(int page, int size);

    void checkOverdueRecords();

    /**
     * 获取所有借阅记录用于导出
     */
    List<BorrowRecordResponse> getAllRecordsForExport(BorrowQueryRequest request);
}
