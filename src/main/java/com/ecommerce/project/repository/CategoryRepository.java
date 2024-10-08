package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
   // @Query(value="SELECT CATEGORY_NAME FROM CATEGORY WHERE CATEGORY_ID=:CATEGORY_ID AND CATEGORY_NAME=:CATEGORY_NAME")
  public  Optional<Category> findByCategoryName(String categoryName);
}
