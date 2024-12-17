package com.farpad.dripServer.models.clientSideData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPaginatedResponse {
    private List<ProductFormData> products;
    private Boolean hasNext;
    private Boolean hasPrev;
    private Integer totalPages;
}

