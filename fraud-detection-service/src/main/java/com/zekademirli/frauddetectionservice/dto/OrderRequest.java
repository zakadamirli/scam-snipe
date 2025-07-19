package com.zekademirli.frauddetectionservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private String orderId;
    private Long userId;
    private Double amount;

    @Override
    public String toString() {
        return "OrderRequest{" +
                "orderId='" + orderId + '\'' +
                ", userId=" + userId +
                ", amount=" + amount +
                '}';
    }
}
