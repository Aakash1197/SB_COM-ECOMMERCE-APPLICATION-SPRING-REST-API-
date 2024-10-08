package com.ecommerce.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(max=50,message = "Street name must be atleast 50 characters.")
    private String street;
    @NotBlank
    @Size(max=50,message = "Building name must be atleast 50 characters.")
    private String buildingName;
    @NotBlank
    @Size(max=40,message = "City name must be atleast 40 characters.")
    private String city;
    @NotBlank
    @Size(max=20,message = "State name must be atleast 20 characters.")
    private String state;

    @NotBlank
    @Size(max=6,message = "Pincode name must be atleast 6 characters.")
    private String pincode;
    @NotBlank
    @Size(max=20,message = "Country name must be atleast 20 characters.")
    private String country;
    private String phone;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User users;

    public Address(String street, String buildingName, String city,
                   String state, String pincode,
                   String country, String phone) {
        this.street = street;
        this.buildingName = buildingName;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.country = country;
        this.phone = phone;
    }
}
