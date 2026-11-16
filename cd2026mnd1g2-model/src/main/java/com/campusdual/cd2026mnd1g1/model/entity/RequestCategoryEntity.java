package com.campusdual.cd2026mnd1g1.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "request_categories")
public class RequestCategoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 20) private String type;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private String icon;
    @Column(nullable = false) private Boolean active;
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<RequestSubcategoryEntity> subcategories = new ArrayList<>();
    public Long getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public Boolean getActive() { return active; }
    public List<RequestSubcategoryEntity> getSubcategories() { return subcategories; }
}
