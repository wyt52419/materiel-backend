package com.zgyw.materiel.bean;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "materiel_level")
@Data
public class MaterielLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 物料编码 */
    private String code;

    /** 物料型号 */
    private String model;

    /** 封装 */
    private String potting;

    /** 库存数量 */
    private Integer quantity;

    /** 价格 */
    private Double price;

    /** 品牌 */
    private String brand;

    /** 供应商 */
    private String supplier;

    /** 网址 */
    private String website;

    /** 备注 */
    private String remarks;

    /** 图片 */
    private String photo;

    /** 分类id */
    private Integer classifyId;

    public MaterielLevel() {
    }

    public MaterielLevel(String code, String model, String potting, Integer quantity, Double price, String brand, String supplier, String website, String remarks, Integer classifyId) {
        this.code = code;
        this.model = model;
        this.potting = potting;
        this.quantity = quantity;
        this.price = price;
        this.brand = brand;
        this.supplier = supplier;
        this.website = website;
        this.remarks = remarks;
        this.classifyId = classifyId;
    }
}