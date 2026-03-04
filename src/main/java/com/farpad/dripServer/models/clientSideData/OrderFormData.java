package com.farpad.dripServer.models.clientSideData;

import com.farpad.dripServer.models.Order;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import lombok.Data;

import java.util.List;

@Data
public class OrderFormData {
    private String id;
    private String productId;
    private String productName;
    private String customerId;
    private String customerEmail;
    private List<Order.OrderedVariant> variants;
    @JsonIgnore
    private List<String> variantsJson;
    private String pricePaid;

    public List<Order.OrderedVariant> _getVariants() {
        if(this.variants != null) return this.variants;
        Gson gson = new Gson();
        return this.getVariantsJson().stream().map(variant -> gson.fromJson(variant, Order.OrderedVariant.class)).toList();
    }
}