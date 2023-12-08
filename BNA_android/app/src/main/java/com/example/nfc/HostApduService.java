package com.example.nfc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;

import androidx.core.app.NotificationCompat;

import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class HostApduService extends android.nfc.cardemulation.HostApduService {

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        byte[] ret = stringToByteArray("6800");
        String apdu = byteArrayToString(commandApdu);

        if (apdu.equals("00A4040014F01020304050600")) {
            return stringToByteArray("9000");
        }
        if (apdu.startsWith("0001")) {
            if (apdu.length() != 14) {
                return ret;
            }
            String stationNumber = apdu.substring(4, 14);
            Log.e("stationNumber", stationNumber);

            // AsyncTask 실행
            new BusInfoTask().execute(stationNumber);

            return stringToByteArray("9000");
        }

        return ret;
    }

    // nfc off
    @Override
    public void onDeactivated(int reason) {
        // Your deactivation code here
    }

    private static byte[] stringToByteArray(String data) {
        final String CHARS = "0123456789ABCDEF";
        byte[] ret = new byte[data.length() / 2];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) (CHARS.indexOf(data.charAt(i * 2)) << 4);
            ret[i] |= (byte) CHARS.indexOf(data.charAt(i * 2 + 1));
        }
        return ret;
    }

    private static String byteArrayToString(byte[] data) {
        final String CHARS = "0123456789ABCDEF";
        StringBuilder ret = new StringBuilder();

        for (byte datum : data) {
            ret.append(CHARS.charAt((datum & 0xf0) >> 4));
            ret.append(CHARS.charAt(datum & 0x0f));
        }
        return ret.toString();
    }

    private class BusInfoTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String routeId = params[0];
            Log.e("routeId", routeId);

            if (routeId.length() > 0) {
                routeId = routeId.substring(0, routeId.length() - 1);
                Log.e("routeId(int)", routeId);
            }

            String apiKey = "WVI7ZtvAVX6hik3qi1Y37dBT8JHku9C%2BWhfM2MKgmcnMqJvckqqUdOpGAf9EpWdzA5gsaTyth86%2FJnvo10Xxwg%3D%3D";
            String url = "http://ws.bus.go.kr/api/rest/arrive/getArrInfoByRouteAll?serviceKey=" + apiKey + "&busRouteId=" + routeId;

            try {
                URL apiUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/xml");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }

                // XML 파싱
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(conn.getInputStream(), null);

                // 변수들 초기화
                String stNm = null;
                String arrmsg1 = null;
                String arrmsg2 = null;
                String brdrdeNum1 = null;
                String brdrdeNum2 = null;
                String rtNm = null;
                String bus_li = "100";

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            String tagName = parser.getName();
                            if ("stNm".equals(tagName)) {
                                stNm = parser.nextText();
                            } else if ("arrmsg1".equals(tagName)) {
                                arrmsg1 = parser.nextText();
                            } else if ("arrmsg2".equals(tagName)) {
                                arrmsg2 = parser.nextText();
                            } else if ("brdrde_Num1".equals(tagName)) {
                                brdrdeNum1 = parser.nextText();
                            } else if ("brdrde_Num2".equals(tagName)) {
                                brdrdeNum2 = parser.nextText();
                            } else if ("rtNm".equals(tagName)) {
                                rtNm = parser.nextText();
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if ("itemList".equals(parser.getName()) && "노원경찰서.혜성여고".equals(stNm)) {
                                // 원하는 값 로그로 출력
                                makeNotification(arrmsg1);

                                try {
                                    Thread.sleep(3000);
                                } catch (Exception e) {

                                }

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("stNm", stNm);
                                intent.putExtra("rtNm", rtNm);
                                intent.putExtra("arrmsg1", arrmsg1);
                                intent.putExtra("arrmsg2", arrmsg2);
                                intent.putExtra("brdrdeNum1", brdrdeNum1);
                                intent.putExtra("brdrdeNum2", brdrdeNum2);

                                startActivity(intent);

                                Log.e("stNm", stNm);
                                Log.e("rtNm", rtNm);
                                Log.e("arrmsg1", arrmsg1);
                                Log.e("arrmsg2", arrmsg2);
                                Log.e("brdrde_Num1", brdrdeNum1);
                                Log.e("brdrde_Num2", brdrdeNum2);

                                conn.disconnect();
                                return null;
                            }
                            break;
                    }
                    eventType = parser.next();
                }

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void makeNotification(String msg) {
        String chanelID = "CHANNEL_ID_NOTIFICATION";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), chanelID);
        builder.setSmallIcon(R.drawable.img)
                .setContentTitle("BNA")
                .setContentText(msg)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data", "Some value to be passed here");

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    notificationManager.getNotificationChannel(chanelID);
            if(notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(chanelID,
                        "Some description", importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        notificationManager.notify(0, builder.build());
    }
}