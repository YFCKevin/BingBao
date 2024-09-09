package com.yfckevin.bingBao.enums;

public enum StickerResourceType {
    STATIC("STATIC", "靜態圖片貼圖"),
    ANIMATION("ANIMATION", "動畫貼圖"),
    SOUND("SOUND", "含有聲音的貼圖"),
    ANIMATION_SOUND("ANIMATION_SOUND", "動畫加聲音的貼圖"),
    POPUP("POPUP", "彈出式貼圖"),
    POPUP_SOUND("POPUP_SOUND", "彈出加聲音的貼圖"),
    MESSAGE("MESSAGE", "可在聊天訊息中展示的貼圖"),
    RICH("RICH", "富貼圖");

    private final String value;
    private final String label;

    StickerResourceType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}

