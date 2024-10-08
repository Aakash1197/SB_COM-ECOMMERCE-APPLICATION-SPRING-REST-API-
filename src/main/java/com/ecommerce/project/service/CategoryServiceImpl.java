package com.ecommerce.project.service;

import com.ecommerce.project.dto.CategoryDTO;
import com.ecommerce.project.dto.CategoryReponse;
import com.ecommerce.project.entity.Category;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.util.Collections;

///import java.util.Arrays;

@Service
public class CategoryServiceImpl implements CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    //  private  List<CategoryDTO> categories=new ArrayList<>();

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryReponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {


        //COMMENTED BELOW LINE AS OF OLDER CLASS LEVEL ARRAY LIST  OBJECT CREATION
        // List<CategoryDTO> categoriesDTO = new ArrayList<>();
        //      try{
        logger.info("CATEGORY DB COUNT  :" + categoryRepository.count());
        if (categoryRepository.count() == 0) {
            throw new APIException("CATEGORIES HAS NOT BEEN CREATED TILL NOW!!!");
        }

        logger.info("ALL categoryId       FECHTED PK  :" + categoryRepository.findAll());


        logger.info(" SRTING CODE CODE START ");
        Sort sortByAndOrder =
                sortOrder.equalsIgnoreCase("asc")
                ?(Sort.by(sortBy).ascending()):(Sort.by(sortBy).descending());

        logger.info(" SRTING CODE CODE ENDED ");


        logger.info(" PAGANATION AND SORTING CODE START ");
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize,sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> catgory = categoryPage.getContent();
        logger.info(" PAGANATION AND SORTING CODE END ");

        //ADDED BELOW LINE AS OF NEWER CONVERSION OF CATEGORY TO CATEGORYDTO MODEL
        List<CategoryDTO> convertedCategoryEntityToCategoryDTO = catgory.stream().map(category ->
                modelMapper.map(category, CategoryDTO.class)).toList();

        CategoryReponse categoryReponse = new CategoryReponse();
        categoryReponse.setContent(convertedCategoryEntityToCategoryDTO);
        logger.info(" SETTING ALL PAGINATION META DATA STERTED ");
        categoryReponse.setPageNumber(categoryPage.getNumber());
        logger.info(" PAGE NUMBER :" + categoryPage.getNumber());
        categoryReponse.setPageSize(categoryPage.getSize());
        logger.info(" PAGE SIZE :" + categoryPage.getSize());
        categoryReponse.setTotalElements(categoryPage.getTotalElements());
        logger.info(" TOTAL PAGE ELEMENT :" + categoryPage.getTotalElements());
        categoryReponse.setTotalPages(categoryPage.getTotalPages());
        logger.info(" TOTAL PAGE  :" + categoryPage.getTotalPages());
        categoryReponse.setLastPage(categoryPage.isLast());
        logger.info(" IS IT LAST PAGE?  :" + categoryPage.isLast());
        logger.info(" SETTING ALL PAGINATION META DATA ENDED ");
        //COMMENTED BELOW LINE AS OF OLDER CONVERSION OF CATEGORY TO CATEGORYDTO MODEL
        // catgory.forEach(e -> categoriesDTO.add(Category.convertCategoryToCategoryDTO(e)));


        // return categoriesDTO;
        return categoryReponse;
    }


    @Override
    public CategoryDTO createCategory(CategoryDTO category) {

        //COMMENTED BELOW LINE AS OF OLDER CONVERSION FROM DTO ENTITY
        //  Category cat = CategoryDTO.convertCategoryDTOToCategory(category);

        //CREATED BELOW LINE AS OF NEWER VERSION OF CONVERSION FROM DTO ENTITY
        Category convertedCategoryDTOToCategoryEntity = modelMapper.map(category, Category.class);

        //COMMENTED BELOW LINE AS OF OLDER CONVERSION TO CHECKING CATEGORY_NM IS AVILABLE OR NOT
        //  Optional<Category> categoryNameAvailableOrNot = categoryRepository.findByCategoryName(cat.getCategoryName());

        //CREATED BELOW LINE AS OF NEWER VERSION OF CONVERSION TO CHECKING CATEGORY_NM IS AVILABLE OR NOT
        Optional<Category> categoryNameAvailableOrNot = categoryRepository.findByCategoryName(convertedCategoryDTOToCategoryEntity.getCategoryName());


        logger.info("CATEGORY NAME EXISTENCE CHECK ", categoryNameAvailableOrNot.toString());
        int count = categoryNameAvailableOrNot.stream().toList().size();
        logger.info("CATEGORY NAME COUNT CHECK FROM DB  ", count);
        if (count > 0 || categoryNameAvailableOrNot.isPresent()) {
            // throw new APIException("CATEGORY WITH NAME   "+categoryNameAvailableOrNot.get().stream().collect(Collectors.toMap(Category::getCategoryId,Category::getCategoryName))+" ALREADY EXISTS  ");
            throw new APIException("CATEGORY WITH NAME " + categoryNameAvailableOrNot.get().getCategoryId() + " " + categoryNameAvailableOrNot.get().getCategoryName() + " ALREADY EXISTS  ");
        }
        //COMMENTED BELOW LINE AS OF OLDER VERSION DATA SAVE IN DB
        //  categoryRepository.save(cat);
        //ADDED BELOW LINE AS OF NEWER VERSION DATA SAVE IN DB
        return modelMapper.map(categoryRepository.save(convertedCategoryDTOToCategoryEntity), CategoryDTO.class);

    }

    @Override
    public CategoryDTO deleteByCategoryId(Long categoryId) {
        //     try {
        logger.info("categoryId       EXISTANCE CHECK FOR DELETE   : {}", categoryId + "   " + categoryRepository.existsById(categoryId));
      /*  CategoryDTO categoryDTO=     categories.stream().filter(
                    c-> c.getCategoryId() == categoryId
            ).findFirst().orElse(null);*/
        Category idExistOrNot = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY", "CATEGORY_ID", categoryId));

        // categories.remove(categoryDTO);
        categoryRepository.deleteById(idExistOrNot.getCategoryId());
        logger.info("categoryId       DELETE PK   : {}", idExistOrNot.getCategoryId());
        return modelMapper.map(idExistOrNot, CategoryDTO.class);
            

     /*   } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("CATEGORY","CATEGORY_ID",categoryId);
        }*/

    }

    @Override
    public CategoryDTO updateByCategoryId(CategoryDTO categoryDTOC, Long categoryId) {
        Category savedCategoryCheck;

        logger.info("categoryId       EXISTANCE CHECK FOR UPDATE  : {}", categoryDTOC.toString() + "   " + categoryRepository.existsById(categoryId));


        Optional<Category> category = categoryRepository.findById(categoryId);
        logger.info("categoryTO OPTIONAL   EXISTANCE CHECK FOR UPDATE  : {}", "   " + category);
        savedCategoryCheck = category.orElseThrow(() -> new ResourceNotFoundException("CATEGORY", "CATEGORY_ID", categoryId));

        Category convertedCategoryDTOToCategoryEntity = modelMapper.map(categoryDTOC, Category.class);
        convertedCategoryDTOToCategoryEntity.setCategoryId(savedCategoryCheck.getCategoryId());
        convertedCategoryDTOToCategoryEntity.setCategoryName(convertedCategoryDTOToCategoryEntity.getCategoryName());

        logger.info("categoryId       Updated PK   : {}", categoryId);

        //savedCategoryCheck = categoryRepository.save(savedCategoryCheck);
        //  return Category.convertCategoryToCategoryDTO(savedCategoryCheck);

        return modelMapper.map(categoryRepository.save(convertedCategoryDTOToCategoryEntity), CategoryDTO.class);


    }
}
