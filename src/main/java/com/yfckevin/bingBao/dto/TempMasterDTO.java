package com.yfckevin.bingBao.dto;

import com.yfckevin.bingBao.entity.TempDetail;

import java.util.ArrayList;
import java.util.List;

public class TempMasterDTO {
    private String id;
    private List<TempDetailDTO> tempDetails = new ArrayList<>();
    private String coverPath;   //使用者上傳的照片路徑
    private String creationDate;
    private String modificationDate;
    private String deletionDate;
    private String creator;
    private String modifier;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TempDetailDTO> getTempDetails() {
        return tempDetails;
    }

    public void setTempDetails(List<TempDetailDTO> tempDetails) {
        this.tempDetails = tempDetails;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(String deletionDate) {
        this.deletionDate = deletionDate;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }
}
