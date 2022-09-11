package com.cargo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cargo.document.AppResponse;
import com.cargo.document.User;
import com.cargo.repo.UserRepository;
import com.cargo.utility.StringConstant;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	private HashMap<String, User> cache  = new HashMap<>();
	
	public User findUserByEmail(String email) {
		if(cache.get(email)!=null) {
			return cache.get(email.toUpperCase());
		}
		else {
			User user = userRepository.findByUsernameIgnoreCase(email);
			cache.put(email.toUpperCase(), user);
			return user;
		}
	}
	
	public AppResponse<Void> saveUser(User user) {
		AppResponse<Void> res = new AppResponse<>();
		try {
			User temp = findUserByEmail(user.getUsername());
			if (temp == null) {
				user.setPassword(bCryptPasswordEncoder.encode(user.getUsername()));
				user.setEnabled(true);
				user.setRoles(user.getRoles());
				userRepository.save(user);
				res.setSuccess(true);
				res.setMsg(Arrays.asList(StringConstant.USER_CREATED_SUCCESS));
				return res;
			} else {
				res.setSuccess(false);
				res.setMsg(Arrays.asList(StringConstant.USER_ALREADY_EXIST));
				return res;
			}
		} catch (Exception e) {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return res;
		}

	}
	
	public AppResponse<Void> updateUser(User user) {
		AppResponse<Void> res = new AppResponse<>();
		try {
			User temp = findUserByEmail(user.getUsername());
			if (temp != null) {
				temp.setFullname(user.getFullname());
				temp.setMobileNo(user.getMobileNo());
				temp.setGstNo(user.getGstNo());
				temp.setAddress(user.getAddress());
				temp.setRoles(user.getRoles());
				temp.setEnabled(user.isEnabled());
				if(user.isEnabled()) {
					temp.setPassword(bCryptPasswordEncoder.encode(temp.getUsername()));
				}
				userRepository.save(temp);
				res.setSuccess(true);
				res.setMsg(Arrays.asList(StringConstant.USER_UPDATE_SUCCESS));
				return res;
			} else {
				res.setSuccess(false);
				res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
				return res;
			}
		} catch (Exception e) {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return res;
		}

	}
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByUsernameIgnoreCase(email);
	    if(user != null) {
	        List<GrantedAuthority> authorities = getUserAuthority(user.getRoles());
	        return buildUserForAuthentication(user, authorities);
	    } else {
	        throw new UsernameNotFoundException("username not found");
	    }
	}
	
	public AppResponse<String> resetPassword(HashMap<String, String> param) {

		AppResponse<String> response = new AppResponse<>();
		try {
			String currPass = param.get("currPass");
			String newPass = param.get("newPass");
			if (!StringUtils.isEmpty(currPass) && !StringUtils.isEmpty(newPass)) {
				String userId = SecurityContextHolder.getContext().getAuthentication().getName();
				User user = findUserByEmail(userId);
				if(bCryptPasswordEncoder.matches(currPass, user.getPassword())) {
					user.setPassword(bCryptPasswordEncoder.encode(newPass));
					userRepository.save(user);
					cache.put(userId, user);
					response.setSuccess(true);
					response.setMsg(Arrays.asList(StringConstant.PASS_CHANGE_SUCCESS));
				}
				else {
					response.setSuccess(false);
					response.setMsg(Arrays.asList(StringConstant.PASS_NOT_CORRECT));
				}
			} else {
				response.setSuccess(false);
				response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		return response;
	}
	
	private List<GrantedAuthority> getUserAuthority(Set<String> userRoles) {
	    Set<GrantedAuthority> roles = new HashSet<>();
	    userRoles.forEach((role) -> {
	        roles.add(new SimpleGrantedAuthority(role));
	    });

	    List<GrantedAuthority> grantedAuthorities = new ArrayList<>(roles);
	    return grantedAuthorities;
	}

	
	private UserDetails buildUserForAuthentication(User user, List<GrantedAuthority> authorities) {
	    return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),user.isEnabled(),true,true,true,authorities);
	}

}
