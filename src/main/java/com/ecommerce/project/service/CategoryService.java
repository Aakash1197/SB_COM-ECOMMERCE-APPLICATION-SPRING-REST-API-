package com.ecommerce.project.service;

import com.ecommerce.project.dto.CategoryDTO;
import com.ecommerce.project.dto.CategoryReponse;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;

import java.util.List;

public interface CategoryService {
    CategoryReponse getAllCategories(Integer pagerNumber, Integer pageSize,String sortBy,String sortOrder)  ;
    CategoryDTO createCategory(CategoryDTO category)  ;
   CategoryDTO deleteByCategoryId(Long categoryId) ;
    CategoryDTO updateByCategoryId(CategoryDTO categoryDTO, Long categoryId) ;
}
