package com.ecommerce.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
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
    @NotBlank
    @Size(max=10,message = "Country name must be atleast 10 characters.")
    private String phone;

}
