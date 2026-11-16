package com.campusdual.cd2026mnd1g1.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cabins")
public class CabinEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cabinId;
    @Column(nullable = false, unique = true)
    private Integer cabinNumber;
    public Long getCabinId() { return cabinId; }
    public Integer getCabinNumber() { return cabinNumber; }
    public void setCabinNumber(Integer cabinNumber) { this.cabinNumber = cabinNumber; }
}
