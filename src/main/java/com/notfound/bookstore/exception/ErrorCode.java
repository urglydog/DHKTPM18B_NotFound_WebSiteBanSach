package com.notfound.bookstore.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ==== 1xxx: Lỗi xác thực & phân quyền ====
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, 1001, "Username or password incorrect."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 1002, "Token has expired."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 1003, "Invalid token."),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, 1004, "Invalid token type."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 1005, "User not authenticated."),
    FORBIDDEN(HttpStatus.FORBIDDEN, 1006, "Access denied."),
    INVALID_CREDENTIALS_OTP(HttpStatus.UNAUTHORIZED, 1007, "Invalid OTP."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, 1008, "OTP chưa hết hạn, vui lòng thử lại sau."),

    // ==== 2xxx: Lỗi Not Found ====
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 2001, "User not found."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, 2002, "Address not found."),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, 2003, "Book not found."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, 2004, "Category not found."),
    AUTHOR_NOT_FOUND(HttpStatus.NOT_FOUND, 2005, "Author not found."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, 2006, "Order not found."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, 2007, "Cart not found."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, 2008, "Cart item not found."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, 2009, "Review not found."),
    PROMOTION_NOT_FOUND(HttpStatus.NOT_FOUND, 2010, "Promotion not found."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 2011, "Payment not found."),
    MAIL_NOT_EXISTED(HttpStatus.NOT_FOUND, 2012, "Email not exists."),
    NOT_FOUND(HttpStatus.NOT_FOUND, 2099, "Resource not found."),

    // ==== 3xxx: Lỗi Conflict & Business Logic ====
    USER_EXISTED(HttpStatus.CONFLICT, 3001, "User already exists."),
    EMAIL_EXISTED(HttpStatus.CONFLICT, 3002, "Email already exists."),
    USERNAME_EXISTED(HttpStatus.CONFLICT, 3003, "Username already exists."),
    CATEGORY_EXISTED(HttpStatus.CONFLICT, 3004, "Category already exists."),
    AUTHOR_EXISTED(HttpStatus.CONFLICT, 3005, "Author already exists."),
    PROMOTION_CODE_EXISTED(HttpStatus.CONFLICT, 3006, "Promotion code already exists."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, 3007, "Insufficient stock available."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, 3008, "Payment already processed."),
    CONFLICT(HttpStatus.CONFLICT, 3099, "Conflict with current state of resource."),

    // ==== 4xxx: Lỗi Validation & Request ====
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 4001, "Invalid syntax for this request."),
    USERNAME_INVALID(HttpStatus.BAD_REQUEST, 4002, "Username must be at least 3 characters."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, 4003, "Password must be at least 8 characters."),
    INVALID_KEY(HttpStatus.BAD_REQUEST, 4004, "Invalid API key."),
    MISSING_ARGUMENTS(HttpStatus.BAD_REQUEST, 4005, "Missing required arguments."),
    INVALID_ARGUMENTS(HttpStatus.BAD_REQUEST, 4006, "Invalid request parameters."),
    INVALID_PAYMENT_SIGNATURE(HttpStatus.BAD_REQUEST, 4007, "Invalid payment signature."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, 4008, "Payment amount mismatch."),
    PAYMENT_TRANSACTION_FAILED(HttpStatus.BAD_REQUEST, 4009, "Payment transaction failed."),
    PROMOTION_CODE_INVALID(HttpStatus.BAD_REQUEST, 4010, "Mã khuyến mãi không hợp lệ."),
    PROMOTION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, 4011, "Mã khuyến mãi đã hết hạn."),
    PROMOTION_CODE_USED_UP(HttpStatus.BAD_REQUEST, 4012, "Mã khuyến mãi đã hết lượt sử dụng."),
    PROMOTION_CODE_NOT_APPLICABLE(HttpStatus.BAD_REQUEST, 4013, "Mã khuyến mãi không áp dụng cho sản phẩm này."),
    PROMOTION_INACTIVE(HttpStatus.BAD_REQUEST, 4014, "Khuyến mãi đang không hoạt động."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 4015, "HTTP method not supported."),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, 4016, "Invalid Accept header."),
    LENGTH_REQUIRED(HttpStatus.LENGTH_REQUIRED, 4017, "Content length required."),
    PRECONDITION_FAILED(HttpStatus.PRECONDITION_FAILED, 4018, "Request precondition failed."),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, 4019, "Request entity too large."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, 4020, "Media type not supported."),
    UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, 4021, "Semantic validation failed."),
    PAYMENT_FAILED(HttpStatus.PAYMENT_REQUIRED, 4022, "Payment processing failed."),
    GONE(HttpStatus.GONE, 4023, "The requested resource is no longer available."),

    // ==== 5xxx: Lỗi Server ====
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5001, "Unexpected internal server error."),
    ERROR_ENCODE(HttpStatus.INTERNAL_SERVER_ERROR, 5002, "Error encoding URL parameters."),
    ERROR_CREATE_HMACSHA512(HttpStatus.INTERNAL_SERVER_ERROR, 5003, "Error creating HMAC-SHA512 signature."),
    PAYMENT_CONFIG_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, 5004, "Payment configuration is missing."),
    INITIALIZATION_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, 5005, "Service initialization failure."),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, 5006, "Feature not implemented."),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, 5007, "Invalid response from upstream server."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, 5008, "Service currently unavailable."),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, 5009, "Gateway timeout."),

    // ==== 9xxx: Lỗi không phân loại ====
    UNCATEGORIZED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 9999, "Uncategorized error.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}