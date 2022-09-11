package com.cargo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.cargo.document.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TokenAuthenticationService {

	static final long EXPIRATIONTIME = 864_000_000; // 10 days
	static final String SECRET = "ThisIsASecret";
	static final String TOKEN_PREFIX = "Bearer";
	static final String HEADER_STRING = "Authorization";
	
	@Autowired
	private CustomUserDetailsService userService;

	public void addAuthentication(HttpServletResponse res, String username) {
		Claims claims = Jwts.claims().setSubject(username);
		User user = userService.findUserByEmail(username);
		ArrayList<SimpleGrantedAuthority> roles = new ArrayList<>();
		user.getRoles().forEach(role ->{
			roles.add(new SimpleGrantedAuthority(role));
		});
        claims.put("scopes", roles);
		String JWT = Jwts.builder().setClaims(claims)
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();
		res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + JWT);
	}

	public Authentication getAuthentication(HttpServletRequest request) {
		String token = request.getHeader(HEADER_STRING);
		if (token != null && !token.equals(TOKEN_PREFIX)) {
			// parse the token.
			String userName = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody()
					.getSubject();
			User user = userService.findUserByEmail(userName);
			ArrayList<SimpleGrantedAuthority> roles = new ArrayList<>();
			user.getRoles().forEach(role ->{
				roles.add(new SimpleGrantedAuthority(role));
			});
			return user != null
					? new UsernamePasswordAuthenticationToken(userName, null,
							roles)
					: null;
		}
		return null;
	}

}
