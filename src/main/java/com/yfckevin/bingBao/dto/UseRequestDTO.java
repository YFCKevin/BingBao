package com.yfckevin.bingBao.dto;

public class UseRequestDTO {
    private String receiveItemId;
    private int usedAmount;
    private String memberName;

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

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public String toString() {
        return "UseRequestDTO{" +
                "receiveItemId='" + receiveItemId + '\'' +
                ", usedAmount=" + usedAmount +
                ", memberName='" + memberName + '\'' +
                '}';
    }
}
