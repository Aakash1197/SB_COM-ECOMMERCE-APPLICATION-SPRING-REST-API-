package com.ecommerce.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@ToString
public class Product implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    @NotBlank
    @Size(min = 6, message = "Product description must have atleast 6 characters.")
    private String description;

    //@Digits(integer = 2, fraction = 2, message = "Discount must have 2 digit integer with 2 digit fraction value")
    @Positive(message = "Discount value must not been negative!!.")
    @Min(value=1, message = "discount must not be within 2 digit!!.")
    @Max(value=99, message = "discount must not be  within 2 digit!!.")
    private Double  discount;

    private String image;

    @NotNull
   // @Digits(integer = 0,fraction = 0, message = "Price must not be 0 rupees!!")
    @Positive(message = "Price value must not negative!!.")
    @Min(value=0, message = "price must not be zero!!.")
    private Double  price;
    @NotBlank
    @Size(min = 3, message = "Product name must have atleast 3 characters.")
    private String productName;
    @NotNull
    @Positive(message = "Quantity value must not negative!!.")
    @Min(value=0, message = "Quantity must not be 0 rupees!!.")
    private Integer quantity;
    @NotNull
    //@Digits(integer = 0,fraction = 0, message = "Special price must not be 0 rupees!!")
    @Positive(message = "special_price value must not negative!!.")
    @Min(value=0, message = "special_price must not be zero!!.")
    private Double  special_price;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;
 /*   private Double seller_id;*/
    /*
    1.In Product owner table we are storing the seller_id column of User details.
     */
    @ManyToOne
    @JoinColumn(name="seller_id")
    private User user;

    @OneToMany(mappedBy = "product",cascade = {CascadeType.PERSIST, CascadeType.REMOVE},fetch = FetchType.EAGER)
    private List<CartItem> cartItem=new ArrayList<>();



//commented at 1st of OCT 2024
 /*   @Override
    public int hashCode() {
        return Objects.hash(productId, description, discount, image, price, productName, quantity, special_price, category);
    }*/


}
