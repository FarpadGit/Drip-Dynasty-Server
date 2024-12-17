package com.farpad.dripServer.controllers;

import com.farpad.dripServer.models.Customer;
import com.farpad.dripServer.models.clientSideData.CustomerFormData;
import com.farpad.dripServer.services.AggregationService;
import com.farpad.dripServer.services.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/customers")
public class CustomersController {

  private final CustomerService customerService;
  private final AggregationService aggregationService;

  @GetMapping("")
  public ResponseEntity<List<CustomerFormData>> getCustomers() {
    List<CustomerFormData> customers = customerService.getCustomers().stream().map(aggregationService::populateCustomer).toList();
    return new ResponseEntity<>(customers, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
    aggregationService.deleteOrdersByCustomer(id);

    boolean deleted = customerService.deleteCustomer(id);
    if (!deleted) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);

  }
}