package com.infy.bankingApplication.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.infy.bankingApplication.entity.User;
import com.infy.bankingApplication.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	    private final JwtService jwtService;
	    private final UserRepository userRepository;

	    public JwtAuthenticationFilter(
	            JwtService jwtService,
	            UserRepository userRepository) {

	        this.jwtService = jwtService;
	        this.userRepository = userRepository;
	    }

	    @Override
	    protected void doFilterInternal(
	            HttpServletRequest request,
	            HttpServletResponse response,
	            FilterChain filterChain)
	            throws ServletException, IOException {

	        String authHeader =
	                request.getHeader("Authorization");

	        if (authHeader == null ||
	                !authHeader.startsWith("Bearer ")) {

	            filterChain.doFilter(request, response);
	            return;
	        }

	        String token =
	                authHeader.substring(7);

	        if (!jwtService.isTokenValid(token)) {

	            filterChain.doFilter(request, response);
	            return;
	        }

	        String username =
	                jwtService.extractUsername(token);

	        User user = userRepository
	                .findByUsername(username)
	                .orElse(null);

	        if (user != null &&
	                SecurityContextHolder
	                    .getContext()
	                    .getAuthentication() == null) {

	            SimpleGrantedAuthority authority =
	                    new SimpleGrantedAuthority(
	                            "ROLE_" + user.getRole());

	            UsernamePasswordAuthenticationToken authentication =
	                    new UsernamePasswordAuthenticationToken(
	                            username,
	                            null,
	                            java.util.List.of(authority));

	            SecurityContextHolder
	                    .getContext()
	                    .setAuthentication(authentication);
	        }

	        filterChain.doFilter(request, response);
	    }
	    @Override
	    protected boolean shouldNotFilter(HttpServletRequest request) {
	        return request.getServletPath().startsWith("/actuator");
	    }

}
