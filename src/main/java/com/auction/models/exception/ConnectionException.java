package com.auction.models.exception;

/**
 * Exception khi lỗi kết nối DB.
 */
public class ConnectionException extends DatabaseException {

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}