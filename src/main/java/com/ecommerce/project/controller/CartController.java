package com.ecommerce.project.controller;


import com.ecommerce.project.dto.CartDTO;
import com.ecommerce.project.entity.Cart;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.util.AuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    @Autowired
    private CartService cartService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserRepository userRepository;
    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductsToCart(@PathVariable("productId") Long productId,
                                                     @PathVariable Integer quantity) {

        return new ResponseEntity<CartDTO>(cartService.addProductToCart(productId,quantity), HttpStatus.CREATED);

    }

    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>>  getAllCarts(){

     return new ResponseEntity<List<CartDTO>>  ( cartService.getAllCarts(),HttpStatus.FOUND);

    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getUserSpecificCarts(){
            String emailId= authUtil.loggedInEmail();

        User user= userRepository.findUserByEmailIdIgnoreCase(emailId).orElseThrow(()->
                new APIException("EmailId Not found!!."));


        return new ResponseEntity<>  ( cartService.getCartByCartIdAndEmailId(user.getCart().getCartId(),emailId),HttpStatus.FOUND);

    }

    @PutMapping("/carts/users/cart/product/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> updateExistingUserQtyCart(@PathVariable Long productId,@PathVariable Integer quantity){
        String emailId= authUtil.loggedInEmail();

        User user= userRepository.findUserByEmailIdIgnoreCase(emailId).orElseThrow(()->
                new APIException("EmailId Not found!!."));


        return new ResponseEntity<>  ( cartService.updateCartQtyByCartIdAndEmailId(user.getCart().getCartId(),emailId,quantity,productId),HttpStatus.FOUND);

    }

    @DeleteMapping("/carts/users/cart/{cartId}/product/{productId}")
    public ResponseEntity<String> removeProductFromCart(@PathVariable Long cartId,@PathVariable Long productId){

        return new ResponseEntity<>  ( cartService.deleteProductFromCart(cartId,productId),HttpStatus.OK);

    }



}
