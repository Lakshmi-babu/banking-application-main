package com.infy.bankingApplication.service.impl;

import org.springframework.stereotype.Service;

import com.infy.bankingApplication.dto.CustomerResponse;
import com.infy.bankingApplication.entity.Customer;
import com.infy.bankingApplication.exception.ResourceNotFoundException;
import com.infy.bankingApplication.repository.CustomerRepository;
import com.infy.bankingApplication.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService{
	
	private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerResponse createCustomer(Customer customer) {

        Customer savedCustomer = customerRepository.save(customer);

        return new CustomerResponse(
                savedCustomer.getId(),
                savedCustomer.getFirstName(),
                savedCustomer.getLastName(),
                savedCustomer.getEmail(),
                savedCustomer.getPhone()
        );
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found"));

        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone()
        );

    }
}
