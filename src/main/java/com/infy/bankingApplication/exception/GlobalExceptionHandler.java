package com.infy.bankingApplication.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	 @ExceptionHandler(ResourceNotFoundException.class)
	    public ResponseEntity<Map<String, Object>> handleNotFound(
	            ResourceNotFoundException ex, HttpServletRequest request) {

	        Map<String, Object> response = new HashMap<>();

	        response.put("timestamp", LocalDateTime.now());
	        response.put("status", 404);
	        response.put("error", "Not Found");
	        response.put("message", ex.getMessage());
	        response.put("path", request.getRequestURI());

	        return ResponseEntity
	                .status(HttpStatus.NOT_FOUND)
	                .body(response);
	    }

	    @ExceptionHandler(InsufficientBalanceException.class)
	    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(
	            InsufficientBalanceException ex,  HttpServletRequest request) {

	        Map<String, Object> response = new HashMap<>();

	        response.put("timestamp", LocalDateTime.now());
	        response.put("status", 400);
	        response.put("error", "Insufficient Balance");
	        response.put("message", ex.getMessage());
	        response.put("path", request.getRequestURI());

	        return ResponseEntity
	                .badRequest()
	                .body(response);
	    }

	    @ExceptionHandler(IllegalArgumentException.class)
	    public ResponseEntity<Map<String, Object>> handleBadRequest(
	            IllegalArgumentException ex, HttpServletRequest request) {

	        Map<String, Object> response = new HashMap<>();

	        response.put("timestamp", LocalDateTime.now());
	        response.put("status", 400);
	        response.put("error", "Bad Request");
	        response.put("message", ex.getMessage());
	        response.put("path", request.getRequestURI());

	        return ResponseEntity
	                .badRequest()
	                .body(response);
	    }
	    
	    @ExceptionHandler(MethodArgumentNotValidException.class)
	    public ResponseEntity<Map<String, Object>> handleValidationException(
	            MethodArgumentNotValidException ex, HttpServletRequest request) {

	        Map<String, Object> errors = new HashMap<>();

	        ex.getBindingResult()
	                .getFieldErrors()
	                .forEach(error ->
	                        errors.put(
	                                error.getField(),
	                                error.getDefaultMessage()
	                        )
	                );

	        Map<String, Object> response = new HashMap<>();

	        response.put("status", 400);
	        response.put("error", "Validation Failed");
	        response.put("errors", errors);
	        response.put("timestamp", LocalDateTime.now());
	        response.put("path", request.getRequestURI());

	        return ResponseEntity
	                .badRequest()
	                .body(response);
	    }
	    
	    @ExceptionHandler(AccountBlockedException.class)
	    public ResponseEntity<Map<String, Object>> handleAccountBlocked(
	            AccountBlockedException ex,
	            HttpServletRequest request) {

	        Map<String, Object> response = new HashMap<>();

	        response.put("timestamp", LocalDateTime.now());
	        response.put("status", 400);
	        response.put("error", "Account Not Active");
	        response.put("message", ex.getMessage());
	        response.put("path", request.getRequestURI());

	        return ResponseEntity
	                .badRequest()
	                .body(response);
	    }
	    
	    @ExceptionHandler(AuthorizationDeniedException.class)
	    public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(
	            AuthorizationDeniedException ex,
	            HttpServletRequest request) {

	        Map<String, Object> response = new HashMap<>();

	        response.put("timestamp", LocalDateTime.now());
	        response.put("status", 403);
	        response.put("error", "Forbidden");
	        response.put("message", "You do not have permission to access this resource.");
	        response.put("path", request.getRequestURI());

	        return ResponseEntity
	                .status(HttpStatus.FORBIDDEN)
	                .body(response);
	    }

}
