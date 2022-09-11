package com.cargo.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.cargo.service.TokenAuthenticationService;

import io.jsonwebtoken.ExpiredJwtException;

import org.springframework.security.core.Authentication;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class JWTAuthenticationFilter extends GenericFilterBean {

	private TokenAuthenticationService tokenService;

	public JWTAuthenticationFilter(TokenAuthenticationService tokenService) {
		this.tokenService = tokenService;
	}

  @Override
  public void doFilter(ServletRequest request,
             ServletResponse response,
             FilterChain filterChain)
      throws IOException, ServletException {
	try {  
    Authentication authentication = tokenService
        .getAuthentication((HttpServletRequest)request);

    SecurityContextHolder.getContext()
        .setAuthentication(authentication);
    filterChain.doFilter(request,response);
  }
	catch (ExpiredJwtException eje) {
        ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
}
