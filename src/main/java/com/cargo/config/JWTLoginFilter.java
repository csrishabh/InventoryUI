package com.cargo.config;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.cargo.document.User;
import com.cargo.service.TokenAuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {
	
	private TokenAuthenticationService tokenService;
	
	public JWTLoginFilter(String url, AuthenticationManager authManager, TokenAuthenticationService tokenService) {
	    super(new AntPathRequestMatcher(url));
	    setAuthenticationManager(authManager);
	    this.tokenService = tokenService;
	  }

	  @Override
	  public Authentication attemptAuthentication(
	      HttpServletRequest req, HttpServletResponse res)
	      throws AuthenticationException, IOException, ServletException {
	    User creds = new ObjectMapper()
	        .readValue(req.getInputStream(), User.class);
	    return getAuthenticationManager().authenticate(
	        new UsernamePasswordAuthenticationToken(
	            creds.getUsername(),
	            creds.getPassword(),
	            Collections.emptyList()
	        )
	    );
	  }

	  @Override
	  protected void successfulAuthentication(
	      HttpServletRequest req,
	      HttpServletResponse res, FilterChain chain,
	      Authentication auth) throws IOException, ServletException {
	    tokenService.addAuthentication(res, auth.getName());
	  }

}
