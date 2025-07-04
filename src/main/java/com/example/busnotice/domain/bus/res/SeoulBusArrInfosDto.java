package com.example.busnotice.domain.bus.res;

import lombok.Data;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class SeoulBusArrInfosDto {

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

        private long stId;
        private String stNm;
        private String arsId;
        private long busRouteId;
        private String rtNm;
        private String busRouteAbrv;
        private String sectNm;
        private double gpsX;
        private double gpsY;
        private double posX;
        private double posY;
        private int stationTp;
        private String firstTm;
        private String lastTm;
        private int term;
        private int routeType;
        private String nextBus;
        private int staOrd;
        private long vehId1;
        private String plainNo1;
        private int sectOrd1;
        private String stationNm1;
        private int traTime1;
        private int traSpd1;
        private int isArrive1;
        private String repTm1;
        private int isLast1;
        private int busType1;
        private long vehId2;
        private String plainNo2;
        private int sectOrd2;
        private String stationNm2;
        private int traTime2;
        private int traSpd2;
        private int isArrive2;
        private String repTm2;
        private int isLast2;
        private int busType2;
        private String adirection;
        private String arrmsg1;
        private String arrmsg2;
        private String arrmsgSec1;
        private String arrmsgSec2;
        private String nxtStn;
        private int rerdieDiv1;
        private int rerdieDiv2;
        private int rerideNum1;
        private int rerideNum2;
        private int isFullFlag1;
        private int isFullFlag2;
        private String deTourAt;
        private int congestion1;
        private int congestion2;

        public BusArrInfosDto.Item toGeneralItem() {
            ArrivalInfo arrivalInfo = parseArrivalMessage(this.arrmsg1);
            if (arrivalInfo == null) {
                return null;
            }
            String routeType = "";
            switch (this.routeType) {
                case 1:
                    routeType = "공항";
                    break;
                case 2:
                    routeType = "마을";
                    break;
                case 3:
                    routeType = "간선";
                    break;
                case 4:
                    routeType = "지선";
                    break;
                case 5:
                    routeType = "순환";
                    break;
                case 6:
                    routeType = "광역";
                    break;
                case 7:
                    routeType = "인천";
                    break;
                case 8:
                    routeType = "경기";
                    break;
                case 9:
                    routeType = "폐지";
                    break;
                case 0:
                    routeType = "공용";
                    break;
                default:
                    routeType = "알수없음"; // 예외 처리
            }
            return new BusArrInfosDto.Item(
                    arrivalInfo.arrprevstationcnt, // arrprevstationcnt
                    arrivalInfo.arrtime, // arrtime
                    this.arsId, // nodeid
                    this.stNm, // nodenm
                    "", // routeid
                    this.rtNm, //routeno
                    routeType, // routetp
                    "" // vehicletp
            );
        }

        private ArrivalInfo parseArrivalMessage(String arrivalMsg) {
            System.out.println("arrivalMsg = " + arrivalMsg);
            // "[우회]" 문자열 제거 후 좌우 공백 제거
            arrivalMsg = arrivalMsg.replace("[우회]", "").trim();

            if (arrivalMsg.equals("곧 도착")) {
                return new ArrivalInfo(30, 0);
            }
            if (arrivalMsg.equals("운행종료") || arrivalMsg.equals("출발대기")) {
                return null;
            }

            Pattern pattern = Pattern.compile("\\s*(\\d+)분\\s*(\\d+)초후\\s*\\[(\\d+)번째 전\\]\\s*");
            Matcher matcher = pattern.matcher(arrivalMsg);

            if (matcher.find()) {
                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));
                int stopNumber = Integer.parseInt(matcher.group(3));
                int totalSeconds = (minutes * 60) + seconds;

                System.out.println("stopNumber = " + stopNumber);
                System.out.println("totalSeconds = " + totalSeconds);
                return new ArrivalInfo(totalSeconds, stopNumber);
            }

            throw new IllegalArgumentException("입력 문자열 형식이 올바르지 않습니다.");
        }


        @Data
        public class ArrivalInfo {

            private final int arrtime;
            private final int arrprevstationcnt;
        }
    }
}
