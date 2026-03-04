package com.farpad.dripServer.models;

import com.farpad.dripServer.models.clientSideData.ProductFormData;
import com.google.gson.Gson;
import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {
	@Id
	private @NonNull String id;
	private @NonNull String name;
	private @NonNull String slug;
	private @NonNull String description;
	private List<String> categories;
	private @NonNull Integer price;
	private Integer discount;
	private String emailMessage;
	private List<Variant> variants = new ArrayList<>();
	private List<SearchTag> searchTags = new ArrayList<>();
	private Integer defaultStock;
	private @NonNull List<String> fileNames;
	private @NonNull Boolean isAvailableForPurchase;
	private List<String> orders;
	private @NonNull Date createdAt;
	private @NonNull Date updatedAt;

	@Getter @Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Variant {
		private String groupName;
		private String type;
		private List<Values> variants;

        @Getter @Setter
		static class Values {
			private String name;
			private Integer stock;
		}

        public void decreaseStockFor(String value) {
            Values values = this.getVariants().stream().filter(v -> v.getName().equals(value)).toList().get(0);
            values.stock -= 1;
        }
	}

	@Getter @Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class SearchTag {
		private String name;
		private String value;
	}

	@Getter @Setter
	public static class Filters {
		private Integer priceMin;
		private Integer priceMax;
		private String category;
		private Map<String, List<SearchTag>> tags;
		private Integer page;
		private String sortBy;
		private String sortOrder;

		public Filters(String priceMin, String priceMax, String category, List<String> tags, String page, String sort) {
			try {
				this.priceMin = priceMin == null ? null : Integer.parseInt(priceMin);
				this.priceMax = priceMax == null ? null :Integer.parseInt(priceMax);
				this.category = category;
				this.page = page == null ? 0 :Integer.parseInt(page);
				if(tags == null || tags.isEmpty()) this.tags = null;
				else {
					this.tags = new HashMap<>();
					Gson gson = new Gson();
					tags.forEach(tag -> {
						SearchTag searchTag = gson.fromJson(tag, SearchTag.class);
						if(!this.tags.containsKey(searchTag.getName()))
							this.tags.put(searchTag.getName(), new ArrayList<>());
						this.tags.get(searchTag.getName()).add(searchTag);
					});
				}
				List<String> sortStrings = Arrays.stream(sort.split("\\.")).toList();
				if(sortStrings.get(0).equals("date")) this.sortBy = "createdAt";
				if(sortStrings.get(0).equals("price")) this.sortBy = "price";
				this.sortOrder = List.of("ASC", "DESC").contains(sortStrings.get(1)) ? sortStrings.get(1) : null;
			} catch(Exception ignored) {}
		}

		public boolean hasPriceFilters() {
			return this.getPriceMin() != null || this.getPriceMax() != null;
		}

		public boolean hasTagFilters() {
			return this.getTags() != null;
		}

		public boolean hasSorting() { return this.getSortBy() != null || this.getSortOrder() != null; }
	}

	@Transient
	public static final String imageUploadDirectoryAccessPath = "images/products/";
	@Transient
	public static final String imageUploadDirectory = "src/main/resources/static/" + imageUploadDirectoryAccessPath;
	@Transient
	public ProductFormData asClientSide(String serverRootPath) {
		Gson gson = new Gson();
		ProductFormData p = new ProductFormData();
		p.setId(this.id);
		p.setName(this.name);
		p.setSlug(this.slug);
		p.setDescription(this.description);
		p.setCategories(this.categories);
		p.setPrice(this.price.toString());
		p.setDiscount(this.discount == null ? "0" : this.discount.toString());
		p.setEmailMessage(this.emailMessage);
        p.setVariants(this.variants);
        p.setSearchTags(this.searchTags);
//		p.setVariantsJson(this.variants.isEmpty() ? new ArrayList<>() : this.variants.stream().map(gson::toJson).toList());
//		p.setSearchTagsJson(this.searchTags.isEmpty() ? new ArrayList<>() : this.searchTags.stream().map(gson::toJson).toList());
		p.setDefaultStock(this.defaultStock == null ? null : this.defaultStock.toString());
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
