package com.cargo.repo;

import com.cargo.document.Cart;

public interface CartRepoCustom {
	
	
	Cart getCartByUserId(String userId);

}
