package com.ecommerce.project.service;

import com.ecommerce.project.dto.AddressDTO;

import com.ecommerce.project.entity.User;

import java.util.List;


public interface AddressService {
    AddressDTO addNewAddress( AddressDTO addressDTO, User user);

    List<AddressDTO> getUserSpecificAddresses(User user);

    List<AddressDTO> getAllAddresses();

    AddressDTO getAddress(Long addressId);
}
