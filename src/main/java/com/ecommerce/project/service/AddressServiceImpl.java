package com.ecommerce.project.service;

import com.ecommerce.project.dto.AddressDTO;
import com.ecommerce.project.entity.Address;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.AddressRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public AddressDTO addNewAddress(AddressDTO addressDTO, User user) {
        boolean addressObjectExistenceCheck=false;
        Address address=modelMapper.map(addressDTO, Address.class);
        List<Address>  listOfUserAddress=user.getAddresses();
        listOfUserAddress.add(address);
        //setting address to the user
        user.setAddresses(listOfUserAddress);
        //setting user to the address
        address.setUsers(user);
        if(addressRepository.count()<1) {

            for (Address object : addressRepository.findAll()) {
                if (object.getCity().equals(addressDTO.getCity()) && object.getPhone().equals(addressDTO.getPhone())
                        && object.getPincode().equals(addressDTO.getPincode()) && object.getStreet().equals(addressDTO.getStreet())
                        && object.getCountry().equals(addressDTO.getCountry()) && object.getState().equals(addressDTO.getState())
                        && object.getBuildingName().equals(addressDTO.getBuildingName())) {

                    addressObjectExistenceCheck = true;
                    break;
                }
            }

            if (addressObjectExistenceCheck) {
                throw new APIException("Address already available!!.");
            }
        }

        Address savedAddress=addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserSpecificAddresses(User user) {
       List<Address> allUserList= addressRepository.findAddressByUserId(user.getUserId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Address","UserId", user.getUserId())
                );


        return allUserList.stream().map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        if(addressRepository.count()!=0) {
            throw new APIException("Address has not been  created till now!!.");
        }
      List<Address> availableAddress=  addressRepository.findAll();
        return availableAddress.stream().map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddress(Long addressId) {
        Address address=addressRepository.findById(addressId).orElseThrow(()->
                new ResourceNotFoundException("Address","AddressId", addressId));


        return modelMapper.map(address, AddressDTO.class);
    }
}
