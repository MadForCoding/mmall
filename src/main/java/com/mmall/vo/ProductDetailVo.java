package com.mmall.vo;

import com.mmall.pojo.Product;

import java.math.BigDecimal;
import java.util.Date;

public class ProductDetailVo extends Product {
    private String imageHost;

    private Integer parentCategoryId;

    private String createTimeString;

    private String updateTimeString;

    public ProductDetailVo() {

    }

    public String getCreateTimeString() {
        return createTimeString;
    }

    public void setCreateTimeString(String createTimeString) {
        this.createTimeString = createTimeString;
    }

    public String getUpdateTimeString() {
        return updateTimeString;
    }

    public void setUpdateTimeString(String updateTimeString) {
        this.updateTimeString = updateTimeString;
    }


    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
}
