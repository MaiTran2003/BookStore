package com.springbooot.dto.request;

public class ReturnBookRequest {

    private Long borrowingId;

    public ReturnBookRequest() {
    }

    public ReturnBookRequest(Long borrowingId) {
        super();
        this.borrowingId = borrowingId;
    }

    public Long getBorrowingId() {
        return borrowingId;
    }

    public void setBorrowingId(Long borrowingId) {
        this.borrowingId = borrowingId;
    }

}
