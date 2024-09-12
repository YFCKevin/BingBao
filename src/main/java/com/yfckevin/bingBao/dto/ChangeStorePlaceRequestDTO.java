package com.yfckevin.bingBao.dto;

public class ChangeStorePlaceRequestDTO {
    private String receiveItemId;
    private String newStorePlace;

    public String getReceiveItemId() {
        return receiveItemId;
    }

    public void setReceiveItemId(String receiveItemId) {
        this.receiveItemId = receiveItemId;
    }

    public String getNewStorePlace() {
        return newStorePlace;
    }

    public void setNewStorePlace(String newStorePlace) {
        this.newStorePlace = newStorePlace;
    }
}
