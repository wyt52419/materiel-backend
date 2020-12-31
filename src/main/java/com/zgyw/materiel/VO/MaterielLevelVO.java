package com.zgyw.materiel.VO;

import lombok.Data;

@Data
public class MaterielLevelVO {

    private Integer id;

    /** 物料编码 */
    private String code;

    /** 商品名称 */
    private String name;

    /** 物料型号 */
    private String model;

    /** 封装 */
    private String potting;

    /** 品牌 */
    private String brand;

    /** 价格 */
    private Double price;

    /** 库存数量 */
    private Integer quantity;

    /** 供应商 */
    private String supplier;

    /** 网址 */
    private String website;

    /** 备注 */
    private String remarks;
}