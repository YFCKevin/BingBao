package com.yfckevin.bingBao.dto;

import java.util.ArrayList;
import java.util.List;

public class ReceiveRequestDTO {
    private String receiveDate;
    private String supplierId;
    private List<ReceiveProductDTO> selectedProducts = new ArrayList<>();

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public List<ReceiveProductDTO> getSelectedProducts() {
        return selectedProducts;
    }

    public void setSelectedProducts(List<ReceiveProductDTO> selectedProducts) {
        this.selectedProducts = selectedProducts;
    }

    @Override
    public String toString() {
        return "ReceiveRequestDTO{" +
                "receiveDate='" + receiveDate + '\'' +
                ", supplierId='" + supplierId + '\'' +
                ", selectedProducts=" + selectedProducts +
                '}';
    }
}
