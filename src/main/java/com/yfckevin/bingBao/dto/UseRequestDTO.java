package com.yfckevin.bingBao.dto;

public class UseRequestDTO {
    private String receiveItemId;
    private int usedAmount;

    public String getReceiveItemId() {
        return receiveItemId;
    }

    public void setReceiveItemId(String receiveItemId) {
        this.receiveItemId = receiveItemId;
    }

    public int getUsedAmount() {
        return usedAmount;
    }

    public void setUsedAmount(int usedAmount) {
        this.usedAmount = usedAmount;
    }
}
