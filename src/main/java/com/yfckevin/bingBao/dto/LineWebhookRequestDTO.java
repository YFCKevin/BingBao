package com.yfckevin.bingBao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yfckevin.bingBao.enums.EventType;
import com.yfckevin.bingBao.enums.MessageType;
import com.yfckevin.bingBao.enums.StickerResourceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LineWebhookRequestDTO 用於封裝來自 LINE Webhook 的請求數據。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineWebhookRequestDTO {

    private String destination;   // destination 表示該事件的目標 LINE Channel 的 ID。
    private List<Event> events;   // events 是一個事件列表，每個事件對應一次 Webhook 請求中的一個活動。

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "LineWebhookRequestDTO{" +
                "destination='" + destination + '\'' +
                ", events=" + events +
                '}';
    }

    /**
     * Event 類別封裝了單個事件的信息。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Event {
        private EventType type;   // type 表示事件的類型，例如 "message"。
        private Message message;   // message 封裝了有關訊息的詳細信息。
        private String webhookEventId;   // webhookEventId 是該 Webhook 事件的唯一標識符。
        private DeliveryContext deliveryContext;   // deliveryContext 封裝了與訊息傳遞相關的上下文資料。
        private long timestamp;   // timestamp 是事件發生的時間戳記，以毫秒為單位的 Unix 時間表示。
        private Source source;   // source 包含了事件的來源信息，即誰發送了這個訊息。
        private String replyToken;   // replyToken 是用來回覆這條訊息的標識符。
        private String mode;   // mode 表示事件的模式，例如 "active"。
        private Postback postback; // 新增這一行

        public EventType getType() {
            return type;
        }

        public void setType(EventType type) {
            this.type = type;
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public String getWebhookEventId() {
            return webhookEventId;
        }

        public void setWebhookEventId(String webhookEventId) {
            this.webhookEventId = webhookEventId;
        }

        public DeliveryContext getDeliveryContext() {
            return deliveryContext;
        }

        public void setDeliveryContext(DeliveryContext deliveryContext) {
            this.deliveryContext = deliveryContext;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public Source getSource() {
            return source;
        }

        public void setSource(Source source) {
            this.source = source;
        }

        public String getReplyToken() {
            return replyToken;
        }

        public void setReplyToken(String replyToken) {
            this.replyToken = replyToken;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public Postback getPostback() {
            return postback;
        }

        public void setPostback(Postback postback) {
            this.postback = postback;
        }

        @Override
        public String toString() {
            return "Event{" +
                    "type=" + type +
                    ", message=" + message +
                    ", webhookEventId='" + webhookEventId + '\'' +
                    ", deliveryContext=" + deliveryContext +
                    ", timestamp=" + timestamp +
                    ", source=" + source +
                    ", replyToken='" + replyToken + '\'' +
                    ", mode='" + mode + '\'' +
                    ", postback=" + postback +
                    '}';
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Postback {
        private String data;
        private Map<String, String> params = new HashMap<>();

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        @Override
        public String toString() {
            return "Postback{" +
                    "data='" + data + '\'' +
                    ", params=" + params +
                    '}';
        }
    }

    /**
     * Message 類別封裝了訊息的詳細信息。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private MessageType type;   // type 表示訊息的類型，例如 "text"。
        private String id;
        private String quoteToken;   // quoteToken 是與訊息關聯的引言標識符。

        //文字
        private String text;

        // 貼圖
        private String stickerId;
        private String packageId;
        private StickerResourceType stickerResourceType;
        private List<String> stickerKeywords;

        //圖片、音檔、影片
        private Map<String, String> contentProvider = new HashMap<>();
        private long duration;

        //地理位置
        private String title;   //地理位置標題
        private String address; //地理位置地址
        private double latitude;    //緯度
        private double longitude;   //經度


        public MessageType getType() {
            return type;
        }

        public void setType(MessageType type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getQuoteToken() {
            return quoteToken;
        }

        public void setQuoteToken(String quoteToken) {
            this.quoteToken = quoteToken;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getStickerId() {
            return stickerId;
        }

        public void setStickerId(String stickerId) {
            this.stickerId = stickerId;
        }

        public String getPackageId() {
            return packageId;
        }

        public void setPackageId(String packageId) {
            this.packageId = packageId;
        }

        public StickerResourceType getStickerResourceType() {
            return stickerResourceType;
        }

        public void setStickerResourceType(StickerResourceType stickerResourceType) {
            this.stickerResourceType = stickerResourceType;
        }

        public List<String> getStickerKeywords() {
            return stickerKeywords;
        }

        public void setStickerKeywords(List<String> stickerKeywords) {
            this.stickerKeywords = stickerKeywords;
        }

        public Map<String, String> getContentProvider() {
            return contentProvider;
        }

        public void setContentProvider(Map<String, String> contentProvider) {
            this.contentProvider = contentProvider;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "type='" + type + '\'' +
                    ", id='" + id + '\'' +
                    ", quoteToken='" + quoteToken + '\'' +
                    ", text='" + text + '\'' +
                    ", stickerId='" + stickerId + '\'' +
                    ", packageId='" + packageId + '\'' +
                    ", stickerResourceType=" + stickerResourceType +
                    ", stickerKeywords=" + stickerKeywords +
                    ", contentProvider=" + contentProvider +
                    ", duration=" + duration +
                    ", title='" + title + '\'' +
                    ", address='" + address + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    '}';
        }
    }

    /**
     * DeliveryContext 類別封裝了與訊息傳遞相關的上下文信息。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeliveryContext {
        private boolean isRedelivery;   // isRedelivery 表示這個訊息是否是重新傳送的。

        public boolean isRedelivery() {
            return isRedelivery;
        }

        public void setRedelivery(boolean redelivery) {
            isRedelivery = redelivery;
        }

        @Override
        public String toString() {
            return "DeliveryContext{" +
                    "isRedelivery=" + isRedelivery +
                    '}';
        }
    }

    /**
     * Source 類別封裝了事件的來源信息。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String type;   // type 表示事件源的類型，例如 "user"。
        private String userId;   // userId 是發送訊息的用戶的唯一標識符。

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public String toString() {
            return "Source{" +
                    "type='" + type + '\'' +
                    ", userId='" + userId + '\'' +
                    '}';
        }
    }
}
