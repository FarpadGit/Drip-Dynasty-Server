package com.farpad.dripServer.models;

import com.farpad.dripServer.models.clientSideData.CustomerFormData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@Document(collection = "customers")
public class Customer {
	@Id
	private @NonNull String id;
	private @NonNull String email;
	private List<String> orders;

	private @NonNull Date createdAt;
	private @NonNull Date updatedAt;


	@Transient
	public CustomerFormData asClientSide() {
		CustomerFormData c = new CustomerFormData();
		c.setId(this.id);
		c.setEmail(this.email);
		c.setOrders(this.orders);
		return c;
	}

}
