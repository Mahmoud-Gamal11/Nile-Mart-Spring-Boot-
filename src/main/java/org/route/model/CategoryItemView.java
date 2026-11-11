package org.route.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryItemView {
    private String name;
    private String slug;
    private String parentName;
    private Integer displayOrder;
    private boolean active;
    private boolean subCategory;
}
