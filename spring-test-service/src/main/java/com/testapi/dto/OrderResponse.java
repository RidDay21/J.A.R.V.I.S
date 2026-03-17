package com.testapi.dto;

public class OrderResponse {
    public String orderId;
    public String status;
    public String message;

    public OrderResponse(String orderId, String status, String message) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
    }
}
