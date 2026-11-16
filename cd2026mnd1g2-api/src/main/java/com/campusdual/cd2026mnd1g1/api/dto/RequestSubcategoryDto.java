package com.campusdual.cd2026mnd1g1.api.dto;

public record RequestSubcategoryDto(Long subcategoryId, Long categoryId, String name, String description,
                                    String icon, boolean active) {
}
