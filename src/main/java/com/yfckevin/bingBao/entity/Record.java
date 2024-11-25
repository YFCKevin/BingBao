package com.yfckevin.bingBao.entity;

import com.yfckevin.bingBao.dto.StoredRecord;
import com.yfckevin.bingBao.enums.Action;
import com.yfckevin.bingBao.exception.ResultStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "record")
public class Record {
    @Id
    private String id;
    private String operator;    // 操作者
    private String currentTime; //操作日期時間
    private Action action;  //操作動作
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

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
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
