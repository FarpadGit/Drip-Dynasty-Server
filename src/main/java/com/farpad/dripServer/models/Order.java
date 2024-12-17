package com.farpad.dripServer.models;

import com.farpad.dripServer.models.clientSideData.OrderFormData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Getter @Setter
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
	@Id
	private @NonNull String id;
	private @NonNull String productId;
	private @NonNull String customerId;

	private @NonNull Integer pricePaid;

	private @NonNull Date createdAt;
	private @NonNull Date updatedAt;

	@Transient
	public OrderFormData asClientSide() {
		OrderFormData o = new OrderFormData();
		o.setId(this.id);
		o.setProductId(this.getProductId());
		o.setCustomerId(this.getCustomerId());
		o.setPricePaid(this.getPricePaid().toString());
		return o;
	}
}
