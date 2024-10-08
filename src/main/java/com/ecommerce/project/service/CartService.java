package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartDTO;

import java.util.List;

public interface CartService {
    public CartDTO addProductToCart(Long productId,Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getCartByCartIdAndEmailId(Long cartId, String emailId);

    CartDTO updateCartQtyByCartIdAndEmailId(Long cartId, String emailId,Integer quantity,Long productId);

    String deleteProductFromCart(Long cartId, Long productId);

    void updateProductInCarts(Long cartId, Long productId);
}
