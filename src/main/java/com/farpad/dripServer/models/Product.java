package com.farpad.dripServer.models;

import com.farpad.dripServer.models.clientSideData.ProductFormData;
import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {
	@Id
	private @NonNull String id;
	private @NonNull String name;
	private @NonNull String description;
	private List<String> categories;
	private @NonNull Integer price;
	private Integer discount;
	private String extra;
	private @NonNull List<String> fileNames;
	private @NonNull Boolean isAvailableForPurchase;
	private List<String> orders;
	private @NonNull Date createdAt;
	private @NonNull Date updatedAt;

	@Transient
	public static final String imageUploadDirectoryAccessPath = "images/products/";
	@Transient
	public static final String imageUploadDirectory = "src/main/resources/static/" + imageUploadDirectoryAccessPath;
	@Transient
	public ProductFormData asClientSide(String serverRootPath) {
		ProductFormData p = new ProductFormData();
		p.setId(this.id);
		p.setName(this.name);
		p.setDescription(this.description);
		p.setCategories(this.categories);
		p.setPrice(this.price.toString());
		p.setDiscount(this.discount == null ? "0" : this.discount.toString());
		p.setExtra(this.extra);
		p.setIsActive(this.isAvailableForPurchase.toString());
		p.setImagePaths(this.getPrefixedFileNames(serverRootPath));
		p.setCreatedSince(Long.valueOf(new Date().getTime() - this.getCreatedAt().getTime()).toString());
		p.setOrders(this.orders);
		return p;
	}

	@Transient
	public List<String> getPrefixedFileNames(String serverRootPath) {
		if(serverRootPath == null) return this.fileNames;
		return this.fileNames.stream().map(s -> serverRootPath + "/" + s).toList();
	}
}
