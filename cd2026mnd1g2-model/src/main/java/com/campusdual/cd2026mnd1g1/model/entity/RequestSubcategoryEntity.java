package com.campusdual.cd2026mnd1g1.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "request_subcategories")
public class RequestSubcategoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subcategoryId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id", nullable = false)
    private RequestCategoryEntity category;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private String icon;
    @Column(nullable = false) private Boolean active;
    public Long getSubcategoryId() { return subcategoryId; }
    public RequestCategoryEntity getCategory() { return category; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public Boolean getActive() { return active; }
}
