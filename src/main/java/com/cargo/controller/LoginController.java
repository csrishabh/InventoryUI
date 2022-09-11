package com.cargo.controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cargo.document.AppResponse;
import com.cargo.document.User;
import com.cargo.service.CustomUserDetailsService;
import com.cargo.service.TokenAuthenticationService;
import com.cargo.utility.StringConstant;

@RestController
@RequestMapping("/backend")
public class LoginController {
	
	@Autowired
	private CustomUserDetailsService userService;
	
	@Autowired
    private AuthenticationManager authenticationManager;
	
	@Autowired
    private TokenAuthenticationService tokenService;
	
	
	@GetMapping(value = "/username")
    public ResponseEntity<User> currentUserNameSimple(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        User user = userService.findUserByEmail(principal.getName());
        return new ResponseEntity<User>(user, HttpStatus.CREATED);
    }
	
	@PostMapping(value = "/login")
    public AppResponse<Object> login(HttpServletRequest request, HttpServletResponse response, @RequestBody User loginUser) {
		AppResponse<Object> res = new AppResponse<>();
		HashMap<String, Object> data = new HashMap<>();
		try {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword()));
		tokenService.addAuthentication(response, loginUser.getUsername());
		String token = response.getHeader("Authorization");
		data.put("token", token);
		data.put("user", userService.findUserByEmail(loginUser.getUsername()));
		res.setSuccess(true);
		res.setData(data);
		}
		catch (BadCredentialsException ex) {
			res.setMsg(Arrays.asList(StringConstant.INVALID_PASSWORD));
			res.setSuccess(false);
		}
		catch (DisabledException e) {
			res.setMsg(Arrays.asList(StringConstant.USER_ACCOUNT_DISBALED));
			res.setSuccess(false);
		}
		catch (Exception e) {
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			res.setSuccess(false);
		}
		return res;	
		
    }
}
