package com.farpad.dripServer.models.clientSideData;

import lombok.Data;

import java.util.List;

@Data
public class CustomerFormData {
    private String id;
    private String email;
    private List<String> orders;
    private String totalOrderValue;
}