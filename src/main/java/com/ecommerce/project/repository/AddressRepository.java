package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("SELECT c FROM Address c WHERE c.users.userId=?1")
    Optional<List<Address>> findAddressByUserId(Long userId);
}
