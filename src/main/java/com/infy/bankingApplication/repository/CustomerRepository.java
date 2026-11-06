package com.infy.bankingApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.infy.bankingApplication.entity.Customer;

public interface CustomerRepository extends JpaRepository <Customer, Long>{
	

}
