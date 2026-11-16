package com.campusdual.cd2026mnd1g1.ws.controller;

import com.campusdual.cd2026mnd1g1.api.dto.*;
import com.campusdual.cd2026mnd1g1.api.service.RequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RequestController {
    private final RequestService requestService;
    public RequestController(RequestService requestService) { this.requestService = requestService; }
    @GetMapping("/categories")
    public ResponseEntity<List<RequestCategoryDto>> categories(@RequestParam String type) {
        return ResponseEntity.ok(requestService.findCategories(type));
    }
    @GetMapping("/categories/{categoryId}/subcategories")
    public ResponseEntity<List<RequestSubcategoryDto>> subcategories(@PathVariable Long categoryId) {
        return ResponseEntity.ok(requestService.findSubcategories(categoryId));
    }
    @PostMapping("/requests")
    public ResponseEntity<ServiceRequestDto> create(@RequestBody ServiceRequestCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.create(request));
    }
    @GetMapping("/requests")
    public ResponseEntity<List<ServiceRequestDto>> findAll() {
        return ResponseEntity.ok(requestService.findAll());
    }
    @GetMapping("/requests/{id}")
    public ResponseEntity<ServiceRequestDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.findById(id));
    }
}
