package com.ecommerce.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_address")
@IdClass(UserAddressId.class)
@Setter
@Getter
public class UserAddress {

    @Id
    private Long address_id;

    @Id
    private Long user_id;

}
