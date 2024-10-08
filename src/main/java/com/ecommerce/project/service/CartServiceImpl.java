package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartDTO;
import com.ecommerce.project.dto.ProductDTO;
import com.ecommerce.project.entity.Cart;
import com.ecommerce.project.entity.CartItem;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;


/*
FLOW CHART OF BELOW LOGIC

[Start]
            |
    v
[Create or Find Cart]
            |
    v
[Retrieve Product by ID]
            |
            +--[Product Found?]----> No ----> [Throw ResourceNotFoundException]
            |
    v
[Check Cart Item]
            |
            +--[Cart Item Exists?]----> Yes ----> [Throw APIException (Already in Cart)]
            |
    v
[Check Product Availability]
            |
            +--[Is Quantity 0?]----> Yes ----> [Throw APIException (Quantity Not Available)]
            |
            +--[Is Requested Quantity > Available?]----> Yes ----> [Throw APIException (Exceeds Available)]
            |
    v
[Create Cart Item]
            |
    v
[Save Cart Item]
            |
    v
[Update Product Quantity]
            |
    v
[Update Cart Total Price]
            |
    v
[Save Updated Cart]
            |
    v
[Map to CartDTO]
            |
    v
[Stream Cart Items to ProductDTO]
            |
    v
[Set Products in CartDTO]
            |
    v
[Return CartDTO]
            |
    v
[End]

*/

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {

        if(  productId==null || productId<=0 ){
            throw new APIException("Request Product id can't be a negative or equal to zero or null value!!");
        }
        else if(quantity==null ||quantity<=0){
            throw new APIException("Request Quantity can't be a negative or equal to zero  or null value!!");
        }

        //1.find the existing cart or create new one
        Cart userCart = createCart();

        //2.retrive the product information
        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, userCart.getCartId());

        if (cartItem != null) {
            throw new APIException("Product" + product.getProductName() + " " + "already exists in the cart!!.");
        }
        if (product.getQuantity() == 0) {
            throw new APIException("Product" + product.getProductName() + " " + "quantity is not available!!.");
        }

        if (product.getQuantity() <= quantity) {
            throw new APIException("Please make an order of the " + product.getProductName() +
                    " " + "less than or equal to the available quantity" + product.getQuantity() + "!!.");
        }
        CartItem settingCartItemValue = new CartItem();
        settingCartItemValue.setProduct(product);
        settingCartItemValue.setQuantity(quantity);
        settingCartItemValue.setCart(userCart);
        settingCartItemValue.setDiscount(product.getDiscount());
        settingCartItemValue.setProductPrice(product.getSpecial_price()*quantity);

        cartItemRepository.save(settingCartItemValue);
        //if order is successfull is placed then below order logic come into picture
        /* product.setQuantity(product.getQuantity()-quantity);*/
        product.setQuantity(product.getQuantity());

        logger.info("EXISTING PRICE OF PRODUCT :" + userCart.getTotalPrice());




        List<CartItem> afterUpdatedQtyInCartItemGettingTotalPriceOfCart=cartItemRepository.findCartItemByCartId(userCart.getCartId());
        if(afterUpdatedQtyInCartItemGettingTotalPriceOfCart.isEmpty()){
            throw new  ResourceNotFoundException("User Requested Cart Details  ","CartId",userCart.getCartId());
        }
        AtomicReference<Double> totalCartPrice= new AtomicReference<>(0.0);
        afterUpdatedQtyInCartItemGettingTotalPriceOfCart.forEach((perProductPrice)-> {
            logger.info("Looping Total cart price  :::::"+perProductPrice.getProductPrice());
            totalCartPrice.updateAndGet(v -> v + perProductPrice.getProductPrice());

        });
        logger.info("EXisting price  "+userCart.getTotalPrice());
        logger.info("new added price  "+totalCartPrice.get());
        userCart.setTotalPrice(
                (totalCartPrice.get()));
        cartRepository.save(userCart);

        CartDTO cartDto = modelMapper.map(userCart, CartDTO.class);
        List<CartItem> userSavedCartItems = userCart.getCartItems();

        Stream<ProductDTO> productsStrem = userSavedCartItems.stream().map((item) -> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
          /*  productDTO.setQuantity(item.getQuantity());*/
            return productDTO;
        });

        cartDto.setProducts(productsStrem.toList());
        return cartDto;


    }

    @Override
    public List<CartDTO> getAllCarts() {

        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty()) {
            throw new APIException("No Cart Exist. ");
        }

        List<CartDTO> cartDTOs=carts.stream().map(cart-> {
            CartDTO cartDto = modelMapper.map(cart, CartDTO.class);
            List<ProductDTO> productDTO=cart.getCartItems().stream().map(product ->
                    modelMapper.map(product, ProductDTO.class)).toList();
            cartDto.setProducts(productDTO);
            return cartDto;
        }).toList();
       return cartDTOs;
    }

    @Override
    public CartDTO getCartByCartIdAndEmailId(Long cartId, String emailId) {

       Cart cart=cartRepository.findCartByEmailIdAndCartId(emailId,cartId).orElseThrow(()->
               new ResourceNotFoundException("Cart","CartId",cartId));

            CartDTO cartDTO=modelMapper.map(cart, CartDTO.class);

            cart.getCartItems().forEach(cartItem->{
               cartItem.getProduct().setQuantity(cartItem.getQuantity());
            });
            List<ProductDTO> productDTO=cart.getCartItems().stream().map(product ->
                    modelMapper.map(product, ProductDTO.class)).toList();
            cartDTO.setProducts(productDTO);
            return cartDTO;



    }
    @Transactional
    @Override
    public CartDTO updateCartQtyByCartIdAndEmailId(Long cartId, String emailId,Integer quantity,Long productId) {
        if(  productId==null || productId<=0 ){
            throw new APIException("Request Product id can't be a negative or equal to zero or null value!!");
        }
        else if(quantity==null ||quantity<=0){
            throw new APIException("Request Quantity can't be a negative or equal to zero  or null value!!");
        }
        Cart cart=cartRepository.findCartByEmailIdAndCartId(emailId,cartId).orElseThrow(()->
                new ResourceNotFoundException("Cart","CartId",cartId));
        //2.retrive the product information
        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));

        CartItem cartItems=cartItemRepository.findCartItemByProductIdAndCartId(productId,cartId);
           if(cartItems==null)  {
              throw new ResourceNotFoundException("User doesn't have created any cart till now for,","CartId",cartId);
           }

        if (product.getQuantity() == 0) {
            throw new APIException("Product" + product.getProductName() + " " + "quantity is not available!!.");
        }

        if (product.getQuantity() <= quantity) {
            throw new APIException("Please make an order of the " + product.getProductName() +
                    " " + "less than or equal to the available quantity" + product.getQuantity() + "!!.");
        }


      CartServiceImpl.updateCartItemTotalPriceBasedOnNewQty(quantity, cartItems, cart, product);


           cartItems.setQuantity(quantity);
           cartItems.setProductPrice(product.getSpecial_price()*quantity);
        CartItem updatedQtyCart=cartItemRepository.save(cartItems);
        List<CartItem> afterUpdatedQtyInCartItemGettingTotalPriceOfCart=cartItemRepository.findCartItemByCartId(cartId);
        if(afterUpdatedQtyInCartItemGettingTotalPriceOfCart.isEmpty()){
            throw new  ResourceNotFoundException("User Requested Cart Details  ","CartId",cartId);
        }
        AtomicReference<Double> totalCartPrice= new AtomicReference<>(0.0);
        afterUpdatedQtyInCartItemGettingTotalPriceOfCart.forEach((perProductPrice)-> {
            logger.info("Looping Total cart price :::::"+perProductPrice.getProductPrice());
            totalCartPrice.updateAndGet(v -> v + perProductPrice.getProductPrice());

        });
        logger.info(":::: Total cart price :::::"+totalCartPrice.get());
       cart.setTotalPrice(totalCartPrice.get());
        cartRepository.save(cart);


        CartDTO cartDTO=modelMapper.map(updatedQtyCart.getCart(), CartDTO.class);
        updatedQtyCart.getCart().getCartItems().forEach(cartItem->{
            cartItem.getProduct().setQuantity(cartItem.getQuantity());

        });
        List<ProductDTO> productDTO=cart.getCartItems().stream().map(product1 ->
                modelMapper.map(product1, ProductDTO.class)).toList();
        cartDTO.setProducts(productDTO);
        return cartDTO;

    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        String msg=null;
        if(  productId==null || productId<=0 ){
            throw new APIException("Request Product id can't be a negative or equal to zero or null value!!");
        }
        else if(cartId==null ||cartId<=0){
            throw new APIException("Request Cart id can't be a negative or equal to zero  or null value!!");
        }

        Cart cart=cartRepository.findById(cartId).orElseThrow( ()->
                new ResourceNotFoundException("Cart","CartId",cartId));
        CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(productId,cartId);
        if(cartItem==null)  {
            throw new ResourceNotFoundException("User Requested Cart Details ","CartId",cartId);
        }


        cartItemRepository.deleteCartItemByProductIdAndCartId(productId,cartId);
        if(cartItemRepository.findCartItemByProductIdAndCartId(productId,cartId)==null){
            msg="User didn't have been created any cart :"+cartId+" till now for this product  :"+productId;
        }
        msg="User requested cartId:"+cartId+" and  product  :"+productId+"  "+"has been deleted successfully!!.";

        List<CartItem> afterDeletingUserRequestProductFromCart=cartItemRepository.findCartItemByCartId(cartId);
        logger.info("292  cartId:"+cartId+"            "+afterDeletingUserRequestProductFromCart.toString());
        if(afterDeletingUserRequestProductFromCart.isEmpty()){
            throw new APIException("User didn't have been created any cart :"+cartId+" till now for this product  :"+productId);
        }
        AtomicReference<Double> totalCartPrice= new AtomicReference<>(0.0);
        afterDeletingUserRequestProductFromCart.forEach((perProductPrice)-> {
            logger.info("Looping After Deleteed User Requested Product From User Cart Total cart price :::::"+perProductPrice.getProductPrice());
            totalCartPrice.updateAndGet(v -> v + perProductPrice.getProductPrice());

        });
        logger.info(":::: Remaining Total cart price :::::"+totalCartPrice.get());
        cart.setTotalPrice(totalCartPrice.get());
        cartRepository.save(cart);


     return msg;
    }

    private  static void  updateCartItemTotalPriceBasedOnNewQty(Integer quantity, CartItem cartItems, Cart cart, Product product) {
        if(cartItems.getQuantity()< quantity) {
            logger.info("PREVIOUS QTY IS LESS THAN OREDER QTY.");
            cart.setTotalPrice(cart.getTotalPrice() -
                    (product.getSpecial_price() * quantity));

        } else if (cartItems.getQuantity()> quantity) {
            logger.info("PREVIOUS QTY IS MORE THAN OREDER QTY.");
            cart.setTotalPrice(cart.getTotalPrice() +
                    (product.getSpecial_price() * quantity));

        }

        else {
            logger.info("QTY ARE SAME NO CHANGE.");
            cart.setTotalPrice(cart.getTotalPrice() );
        }
    }

    Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (userCart != null) {

            return userCart;
        }
        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        return cartRepository.save(cart);
    }


    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice()
                - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecial_price());

        cart.setTotalPrice(cartPrice
                + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepository.save(cartItem);
    }

}
