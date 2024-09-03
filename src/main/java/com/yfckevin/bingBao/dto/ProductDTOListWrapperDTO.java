package com.yfckevin.bingBao.dto;

import java.util.List;

public class ProductDTOListWrapperDTO {
    private List<ProductDTO> productDTOList;

    public List<ProductDTO> getProductDTOList() {
        return productDTOList;
    }

    public void setProductDTOList(List<ProductDTO> productDTOList) {
        this.productDTOList = productDTOList;
    }

    @Override
    public String toString() {
        return "ProductDTOListWrapperDTO{" +
                "productDTOList=" + productDTOList +
                '}';
    }
}
