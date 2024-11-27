package com.yfckevin.bingBao.dto;

import com.yfckevin.bingBao.enums.Action;
import com.yfckevin.bingBao.enums.TraceState;

import java.util.ArrayList;
import java.util.List;

public class RecordDTO {
    private String id;
    private String operator;    // 操作者
    private String currentTime; //操作日期時間
    private String action;  //操作動作
    private String item;    //項目名
    private List<StoredRecord> storeRecordList = new ArrayList<>(); //食材放入冰箱細節
    private String actionDetail;    //操作細節
    private String traceId; //追蹤編號
    private String traceState;  //追蹤階段

    //用於追蹤匯入圖片
    private String tempMasterId;

    //用於追蹤食材跟庫存
    private List<String> productId = new ArrayList<>();
    private List<String> receiveItemId;

    //用於追蹤供應商
    private String supplierId;

    //用於追蹤待購清單
    private List<String> shoppingItemId;


    public String getTraceState() {
        return traceState;
    }

    public void setTraceState(String traceState) {
        this.traceState = traceState;
    }

    public String getTempMasterId() {
        return tempMasterId;
    }

    public void setTempMasterId(String tempMasterId) {
        this.tempMasterId = tempMasterId;
    }

    public void setProductId(List<String> productId) {
        this.productId = productId;
    }

    public void setReceiveItemId(List<String> receiveItemId) {
        this.receiveItemId = receiveItemId;
    }

    public List<String> getShoppingItemId() {
        return shoppingItemId;
    }

    public void setShoppingItemId(List<String> shoppingItemId) {
        this.shoppingItemId = shoppingItemId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public List<StoredRecord> getStoreRecordList() {
        return storeRecordList;
    }

    public void setStoreRecordList(List<StoredRecord> storeRecordList) {
        this.storeRecordList = storeRecordList;
    }

    public String getActionDetail() {
        return actionDetail;
    }

    public void setActionDetail(String actionDetail) {
        this.actionDetail = actionDetail;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public List<String> getProductId() {
        return productId;
    }

    public List<String> getReceiveItemId() {
        return receiveItemId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }
}
