package com.infy.bankingApplication.service;

import com.infy.bankingApplication.dto.CustomerResponse;
import com.infy.bankingApplication.entity.Customer;

public interface CustomerService {
	
	CustomerResponse createCustomer(Customer customer);

    CustomerResponse getCustomerById(Long id);

}
