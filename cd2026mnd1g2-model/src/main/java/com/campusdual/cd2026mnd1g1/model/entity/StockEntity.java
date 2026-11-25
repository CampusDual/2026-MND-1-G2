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

    @Column(name = "normalized_name", nullable = false, length = 160)
    private String normalizedName;

    @Column(length = 255)
    private String description;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "subcategory_id", unique = true)
    private RequestSubcategoryEntity subcategory;

    @Column(name = "stock_type", nullable = false, length = 20)
    private String stockType;

    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock;

    @Column(name = "warehouse_units", nullable = false)
    private Integer warehouseUnits;

    @Column(name = "laundry_units", nullable = false)
    private Integer laundryUnits;

    @Column(nullable = false)
    private Integer uds;

    @Column(nullable = false)
    private Boolean active;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "creation_key", length = 160, unique = true)
    private String creationKey;

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getStockType() {
        return stockType;
    }

    public void setStockType(String stockType) {
        this.stockType = stockType;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public Integer getWarehouseUnits() {
        return warehouseUnits;
    }

    public void setWarehouseUnits(Integer warehouseUnits) {
        this.warehouseUnits = warehouseUnits;
    }

    public Integer getLaundryUnits() {
        return laundryUnits;
    }

    public void setLaundryUnits(Integer laundryUnits) {
        this.laundryUnits = laundryUnits;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getVersion() {
        return version;
    }

    public String getCreationKey() {
        return creationKey;
    }

    public void setCreationKey(String creationKey) {
        this.creationKey = creationKey;
    }
}
