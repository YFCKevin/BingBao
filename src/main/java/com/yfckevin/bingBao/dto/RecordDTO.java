package com.yfckevin.bingBao.dto;

import com.yfckevin.bingBao.enums.Action;

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
}
