package com.farpad.dripServer.models.clientSideData;

import lombok.Data;

@Data
public class OrderFormData {
    private String id;
    private String productId;
    private String productName;
    private String customerId;
    private String customerEmail;
    private String pricePaid;
}