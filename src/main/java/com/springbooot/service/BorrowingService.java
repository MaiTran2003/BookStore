package com.springbooot.service;

import com.springbooot.dto.request.BorrowBookRequest;
import com.springbooot.dto.request.ReturnBookRequest;
import com.springbooot.dto.response.BorrowBookResponse;
import com.springbooot.dto.response.ReturnBookResponse;

public interface BorrowingService {

    BorrowBookResponse borrowBook(BorrowBookRequest borrowBookRequest);

    ReturnBookResponse returnBook(ReturnBookRequest returnBookRequest);

}
