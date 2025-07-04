package com.example.busnotice.domain.busStop.res;

import lombok.Data;

import java.util.List;

@Data
public class SeoulBusStopsDto {

    private ComMsgHeader comMsgHeader;
    private MsgHeader msgHeader;
    private MsgBody msgBody;

    @Data
    public static class ComMsgHeader {

        private String errMsg;
        private String requestMsgID;
        private String responseMsgID;
        private String responseTime;
        private String successYN;
        private String returnCode;
    }

    @Data
    public static class MsgHeader {

        private String headerMsg;
        private int headerCd;
        private int itemCount;
    }

    @Data
    public static class MsgBody {

        private List<Item> itemList;
    }

    @Data
    public static class Item {

        private String stId;
        private String stNm;
        private double tmX;
        private double tmY;
        private double posX;
        private double posY;
        private String arsId;
    }
}