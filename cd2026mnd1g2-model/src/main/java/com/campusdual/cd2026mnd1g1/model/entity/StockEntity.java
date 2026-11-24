package com.campusdual.cd2026mnd1g1.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock")
public class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, length = 160)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", nullable = false, unique = true)
    private RequestSubcategoryEntity subcategory;

    @Column(nullable = false)
    private Integer uds = 500;

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RequestSubcategoryEntity getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(RequestSubcategoryEntity subcategory) {
        this.subcategory = subcategory;
    }

    public Integer getUds() {
        return uds;
    }

    public void setUds(Integer uds) {
        this.uds = uds;
    }
}
