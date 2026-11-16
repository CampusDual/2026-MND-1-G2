package com.campusdual.cd2026mnd1g1.api.dto;

import java.util.List;

public record RequestCategoryDto(Long categoryId, String name, String type, String description, String icon,
                                  boolean active, List<RequestSubcategoryDto> subcategories) {
}
