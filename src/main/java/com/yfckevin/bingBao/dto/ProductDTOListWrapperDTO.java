package com.yfckevin.bingBao.dto;

import java.util.List;

public class ProductDTOListWrapperDTO {
    private String tempMasterId;
    private List<ProductDTO> productDTOList;

    public List<ProductDTO> getProductDTOList() {
        return productDTOList;
    }

    public void setProductDTOList(List<ProductDTO> productDTOList) {
        this.productDTOList = productDTOList;
    }

    public String getTempMasterId() {
        return tempMasterId;
    }

    public void setTempMasterId(String tempMasterId) {
        this.tempMasterId = tempMasterId;
    }

    @Override
    public String toString() {
        return "ProductDTOListWrapperDTO{" +
                "tempMasterId='" + tempMasterId + '\'' +
                ", productDTOList=" + productDTOList +
                '}';
    }
}
