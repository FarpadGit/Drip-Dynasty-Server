package com.farpad.dripServer.models.clientSideData;

import com.farpad.dripServer.models.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductFiltersData {
    private List<Product.SearchTag> searchTags;
    private Integer maxPrice;
}
