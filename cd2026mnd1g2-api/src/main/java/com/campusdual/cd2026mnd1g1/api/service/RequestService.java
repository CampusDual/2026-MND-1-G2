package com.campusdual.cd2026mnd1g1.api.service;

import com.campusdual.cd2026mnd1g1.api.dto.RequestCategoryDto;
import com.campusdual.cd2026mnd1g1.api.dto.RequestSubcategoryDto;
import com.campusdual.cd2026mnd1g1.api.dto.ServiceRequestCreateDto;
import com.campusdual.cd2026mnd1g1.api.dto.ServiceRequestDto;

import java.util.List;

public interface RequestService {

    List<RequestCategoryDto> findCategories(String type);

    List<RequestSubcategoryDto> findSubcategories(Long categoryId);

    ServiceRequestDto create(ServiceRequestCreateDto request);

    List<ServiceRequestDto> findAll();

    ServiceRequestDto findById(Long id);
}
