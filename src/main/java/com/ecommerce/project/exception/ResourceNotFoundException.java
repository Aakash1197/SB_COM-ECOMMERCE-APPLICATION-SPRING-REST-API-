package com.ecommerce.project.exception;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
public class ResourceNotFoundException extends RuntimeException {
    private String resourceName;
    private String field;
    private String fieldName;
    private Long fieldid;

    public ResourceNotFoundException(String message, String resourceName, String field, String fieldName) {
        super(String.format("%s not found  with %s : %s", resourceName, field, fieldName));
        this.resourceName = resourceName;
        this.field = field;
        this.fieldName = fieldName;
    }

    public ResourceNotFoundException(String resourceName, String field, Long fieldid) {
        super(String.format("%s not found  with %s : %d", resourceName, field, fieldid));
        this.resourceName = resourceName;
        this.fieldid = fieldid;
        this.field = field;
    }


}
