package com.ecommerce.project.dto;

import com.ecommerce.project.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO // implements Comparable<CategoryDTO>
 {
    private Long categoryId;
    @NotBlank
    @Size(min = 5, message ="Category name atleast 5 characters")
    private String categoryName;





   /* @Override
    public int compareTo(CategoryDTO o1) {
        if (o1.getCategoryId() == o1.getCategoryId()) {
            return 0;
        } else if (o1.getCategoryId() > o1.getCategoryId()) {
            return -1;
        } else if (o1.getCategoryId() < o1.getCategoryId()) {
            return 1;
        }
        return 0;
    }*/

    public static Category convertCategoryDTOToCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setCategoryId(categoryDTO.getCategoryId());
        category.setCategoryName(categoryDTO.getCategoryName());
        return category;
    }
}
