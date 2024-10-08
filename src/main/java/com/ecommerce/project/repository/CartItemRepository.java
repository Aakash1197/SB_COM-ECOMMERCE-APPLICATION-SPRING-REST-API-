package com.ecommerce.project.repository;

import com.ecommerce.project.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("SELECT ci from CartItem ci where ci.product.productId=?1 and ci.cart.cartId=?2")
    CartItem findCartItemByProductIdAndCartId(Long productId, Long cartId);

    @Query("SELECT ci from CartItem ci where ci.cart.cartId=?1")
    List<CartItem> findCartItemByCartId(Long cartId);

   @Modifying
    @Query("delete from CartItem ci where   ci.product.productId=?1 and ci.cart.cartId=?2")
    void deleteCartItemByProductIdAndCartId( Long productId,Long cartId);
}
