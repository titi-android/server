package com.example.busnotice.domain.bus.res;

import java.util.List;
import lombok.Data;

@Data
public class SeoulBusInfosDto {

    private ComMsgHeader comMsgHeader;
    private MsgHeader msgHeader;
    private MsgBody msgBody;

    @Data
    public static class ComMsgHeader {

        private String requestMsgID;
        private String responseMsgID;
        private String responseTime;
        private String returnCode;
        private String successYN;
        private String errMsg;
    }

    @Data
    public static class MsgHeader {

        private String headerMsg;
        private int headerCd;
        private int itemCount;
    }

    @Data
    public static class MsgBody {

        private List<BusRoute> itemList;
    }

    @Data
    public static class BusRoute {

        private String busRouteId;
        private String busRouteNm;
        private String busRouteAbrv;
        private double length;
        private int busRouteType;
        private String stBegin;
        private String stEnd;
        private int term;
        private int nextBus;
        private String firstBusTm;
        private String lastBusTm;
        private String firstBusTmLow;
        private String lastBusTmLow;
    }
}
// TEST