package com.ecommerce.project.controller;


import com.ecommerce.project.dto.AddressDTO;
import com.ecommerce.project.entity.Address;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AuthUtil   authUtil;

    @PostMapping("/address")
    public ResponseEntity<AddressDTO> addAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User user=authUtil.loggedInUser();

        return new ResponseEntity<>(addressService.addNewAddress(addressDTO,user), HttpStatus.CREATED);
    }

    @GetMapping("/address/users")
    public ResponseEntity<List<AddressDTO>> getUserSpecificAddress(){
        User user=authUtil.loggedInUser();
        return new ResponseEntity<>(addressService.getUserSpecificAddresses(user), HttpStatus.FOUND);
    }

    @GetMapping("/address")
    public ResponseEntity<List<AddressDTO>> getAllAddress(){
        return new ResponseEntity<>(addressService.getAllAddresses(), HttpStatus.FOUND);
    }

    @GetMapping("/address/{addressId}")
    public ResponseEntity<AddressDTO> getAddress(@PathVariable Long addressId){
        return new ResponseEntity<>(addressService.getAddress(addressId), HttpStatus.FOUND);
    }


}
