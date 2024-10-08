package com.ecommerce.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressId implements Serializable {
    private Long user_id;
    private Long address_id;

    // Default constructor, equals, and hashCode methods
}
