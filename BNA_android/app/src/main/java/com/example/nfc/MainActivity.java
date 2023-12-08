package com.example.nfc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.location.LocationManager;
import android.view.ViewGroup;

import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    TextView secondBus, firstBus, nextStation, previStation, currentStation, busNumber, secondBusShort, firstBusShort, busNumberShort;
    View busInfoBack, busInfoBackShort, subBarBack, subBarBack2;
    Button currentLocationBtn;
    LinearLayout busInfo, busInfoShort;

    private float offsetY, layoutLine;

    private NfcAdapter nfcAdapter = null;

    int nCurrentPermission = 0;
    static final int PERMISSIONS_REQUEST = 0x0000001;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private GPSTracker gpsTracker;
    private MapPOIItem currentLocation;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    boolean checkLocation;
    private RequestQueue requestQueue;
    private String url = "";
    private String[] payloadList;


//    final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

    @SuppressLint({"CutPasteId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Uri uri = getIntent().getData();
            String data = uri.getQueryParameter("data");
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

            payloadList = data.split(",");
            getBusInfo(payloadList);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        MapView mapView = new MapView(this);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.kakao_map);
        mapViewContainer.addView(mapView);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        currentLocation = new MapPOIItem();

        busInfo = (LinearLayout) findViewById(R.id.bus_info);
        busInfoShort = (LinearLayout) findViewById(R.id.bus_info_short);
        busInfoBack = (View) findViewById(R.id.bus_info_back);
        busInfoBackShort = (View) findViewById(R.id.bus_info_back_short);

        subBarBack = (View) findViewById(R.id.sub_bar_back);
        subBarBack2 = (View) findViewById(R.id.sub_bar_back2);

        busNumber = (TextView) findViewById(R.id.bus_number);
        busNumberShort = (TextView) findViewById(R.id.bus_number_short);
        currentStation = (TextView) findViewById(R.id.current_station);
        previStation = (TextView) findViewById(R.id.previ_station);
        nextStation = (TextView) findViewById(R.id.next_station);

        firstBus = (TextView) findViewById(R.id.first_bus);
        firstBusShort = (TextView) findViewById(R.id.first_bus_short);
        secondBus = (TextView) findViewById(R.id.second_bus);
        secondBusShort = (TextView) findViewById(R.id.second_bus_short);

        currentLocationBtn = (Button) findViewById(R.id.currentLocation);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        startService(new Intent(this, Foreground.class));
        checkLocation = false;
        currentLocationBtn.setOnClickListener(v -> {
            if (!checkLocation && !checkLocationServicesStatus()) {
                showDialogForLocationServiceSetting();
                checkLocation = true;
            } else {
                checkRunTimePermission();
                currentLocation(mapView);
            }

        });

        // main bar 움직이는 부분
        busInfo.setY(750);
        busInfo.setOnTouchListener(new View.OnTouchListener() { // bar 움직일 때
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if (action == event.ACTION_DOWN) {   //처음 눌렸을 때
                    offsetY = event.getRawY() - busInfo.getY();
                } else if (action == event.ACTION_MOVE) {    //누르고 움직였을 때
                    layoutLine = event.getRawY() - offsetY;
                    if(layoutLine < 0)
                        layoutLine = 0;
                    else if(layoutLine > 750)
                        layoutLine = 750;

                    busInfo.setY(layoutLine);

                    System.out.println("getRawY : " + event.getRawY());
                    System.out.println("offsetY : " + offsetY);
                    System.out.println("setY : " + layoutLine);
                } else if(action == event.ACTION_UP){
                    if(layoutLine <= 375) {
                        layoutLine = 0;
                    } else if(layoutLine > 375){
                        layoutLine = 750;
                    }
                    if(layoutLine >= 750) {
                        clickVisibility(v);
                    }
                }
                return true;
            }
        });
        busInfoShort.setOnTouchListener(new View.OnTouchListener() { // bar 움직일 때
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if (action == event.ACTION_DOWN) {   //처음 눌렸을 때
                    clickVisibility(v);
                    offsetY = event.getRawY() - busInfo.getY();
                } else if (action == event.ACTION_MOVE) {    //누르고 움직였을 때
                    layoutLine = event.getRawY() - offsetY;
                    if(layoutLine < 0)
                        layoutLine = 0;
                    else if(layoutLine > 750)
                        layoutLine = 750;

                    busInfo.setY(layoutLine);

                } else if(action == event.ACTION_UP){
                    if(layoutLine <= 375) {
                        layoutLine = 0;
                    } else if(layoutLine > 375){
                        layoutLine = 750;
                    }
                    if(layoutLine >= 750) {
                        clickVisibility(v);
                    }
                }
                return true;
            }
        });
    }

    // main xml

    public void clickVisibility(View view){
        turnoff();
        switch (view.getId()){
            case R.id.bus_info:
                Log.e("name","실행됨");
                busInfoShort.setVisibility(View.VISIBLE);
                break;
            case R.id.bus_info_short:
                Log.e("name","실행됨");
                busInfo.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void turnoff(){
        busInfo.setVisibility(View.INVISIBLE);
        busInfoShort.setVisibility(View.INVISIBLE);
    }

    // 예비 아두이노 코드

    private void processCommand(Intent intent) {
        String Congestion[] = new String[2];
        int n[] = new int[2];

        if(intent != null) {
            String stNm = intent.getStringExtra("stNm");
            String rtNm = intent.getStringExtra("rtNm");
            String arrmsg1 = intent.getStringExtra("arrmsg1");
            String arrmsg2 = intent.getStringExtra("arrmsg2");
            String brdrdeNum1 = intent.getStringExtra("brdrdeNum1");
            String brdrdeNum2 = intent.getStringExtra("brdrdeNum2");

            n[0] = Integer.parseInt(brdrdeNum1);
            n[1] = Integer.parseInt(brdrdeNum2);

            if(n[0] >= 5) {
                Congestion[0] = "혼잡";
            }
            else if(n[0] >= 4) {
                Congestion[0] = "보통";
            }
            else if(n[0] >= 3) {
                Congestion[0] = "여유";
            }
            else {
                Congestion[0] = "데이터 없음";
            }
            if(n[1] >= 5) {
                Congestion[1] = "혼잡";
            }
            else if(n[1] >= 4) {
                Congestion[1] = "보통";
            }
            else if(n[1] >= 3) {
                Congestion[1] = "여유";
            }
            else {
                Congestion[1] = "데이터 없음";
            }

            busNumber.setText(rtNm + " 번 버스");
            currentStation.setText("노원경찰서.혜성여고");
            previStation.setText("세이브존스포츠센터");
            nextStation.setText("동천학교");
            firstBus.setText(arrmsg1 + "(" + Congestion[0] + ")");
            secondBus.setText(arrmsg2 + "(" + Congestion[1] + ")");

        }
    }

    // NFC border
    // 리스트 부분 띄어주기
    private Dialog busInfoDialog;
    private void showBusInfo(List<String> busNameList) {
        if (busInfoDialog != null && busInfoDialog.isShowing()) {
            return;
        }
        busInfoDialog = new Dialog(this);
        busInfoDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        busInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        busInfoDialog.setContentView(R.layout.bus_info_dialog);
        busInfoDialog.show();

        RadioGroup busInfoRadioGroup = busInfoDialog.findViewById(R.id.busInfoRadioGroup);
        for (int i = 0; i < busNameList.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(busNameList.get(i));
            radioButton.setTextSize(16);
            radioButton.setId(i);
            busInfoRadioGroup.addView(radioButton);
        }

        Button busSelectBtn = busInfoDialog.findViewById(R.id.busSelectBtn);
        busSelectBtn.setOnClickListener(view -> {
            try {
                int selectedIdx = busInfoRadioGroup.getCheckedRadioButtonId();
                String busName = busNameList.get(selectedIdx);
                Log.e("sdf", busName);

                getDataFlask();

                // selectBus(busName);
                FancyToast.makeText(this, "성공적으로 버스 정보를 가져왔어요!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                busInfoDialog.dismiss();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                FancyToast.makeText(this, "버스를 선택해주세요!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
    }
    // 정거장 리스트 부분
    @Override
    protected void onResume() {
        try {
            if (nfcAdapter != null) {
                Bundle options = new Bundle();
                options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

                nfcAdapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {
                            @Override
                            public void onTagDiscovered(Tag tag) {
                                try {
                                    if (tag != null){
                                        Ndef ndef = Ndef.get(tag);
                                        ndef.connect();

                                        NdefMessage ndefMessage = ndef.getNdefMessage();

                                        NdefRecord record = ndefMessage.getRecords()[0];

                                        String payload = new String(record.getPayload(), 1, record.getPayload().length - 1, Charset.forName("UTF-8"));
                                        payload = payload.substring(2);
                                        Log.e("payload", payload);

                                        payloadList = payload.split(",");

                                        for(int i = 0; i < payloadList.length;i++){
                                            Log.e("testing : ", payloadList[i]);
                                        }

                                        getBusInfo(payloadList);

                                        ndef.close();
                                    }
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        },
                        NfcAdapter.FLAG_READER_NFC_A |
                                NfcAdapter.FLAG_READER_NFC_B |
                                NfcAdapter.FLAG_READER_NFC_F |
                                NfcAdapter.FLAG_READER_NFC_V |
                                NfcAdapter.FLAG_READER_NFC_BARCODE |
                                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                        options
                );
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        super.onResume();
    }

    // connect to Flask Sever
    private void getDataFlask(){
        StringRequest request = new StringRequest(
                Request.Method.GET,
                "http://3.104.255.16:5000/get_mod?arsId=" + payloadList[1] + "&mod=" + payloadList[2],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("sdf", response);
                        try {
                            String[] nextPreiousBusStation = response.split(",");

                            for(String station: nextPreiousBusStation) {
                                Log.e("asf",station);
                            }

                            previStation.setText(nextPreiousBusStation[1]);
                            nextStation.setText(nextPreiousBusStation[3]);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);

        request = new StringRequest(
                Request.Method.GET,
                "http://3.104.255.16:5000/get_ars_id_info?mod=" + payloadList[2] + "&arsId=" + payloadList[1],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("sdf", response);
                        try {
                            String[] mainBar = response.split(",");

                            for(String station: mainBar) {
                                Log.e("asf",station);
                            }
                            currentStation.setText(payloadList[0]);
                            busNumber.setText(mainBar[0]);
                            firstBus.setText(mainBar[1]);
                            secondBus.setText(mainBar[2]);

                            busNumberShort.setText(mainBar[0]);
                            firstBusShort.setText(mainBar[1]);
                            secondBusShort.setText(mainBar[2]);
                            // 혼잡도는 나중에 구현하기

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                            List<String> busList = new ArrayList<>(Arrays.asList(dataList));
//                                            showBusInfo(busList);
                                }
                            });

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    // url로 연결

    public void getBusInfo(String[] payloadList) {
        StringRequest request = new StringRequest(
                Request.Method.GET,
                "http://3.104.255.16:5000/get_routes?station_name=" + payloadList[0],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("sdf", response);
                        try {
                            JSONObject jsonObject =  new JSONObject(response);
                            String data = jsonObject.getString("result_routes").replace("\"", "").replace("[", "").replace("]", "");
                            String[] dataList = data.split(",");

                            for (String bus : dataList) {
                                Log.e("sdf", bus);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    List<String> busList = new ArrayList<>(Arrays.asList(dataList));
                                    showBusInfo(busList);
                                }
                            });

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);
    }

    // 위치 정보
    public void currentLocation(MapView mapView) {
        mapView.removePOIItem(currentLocation);
        gpsTracker = new GPSTracker(MainActivity.this);
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();
        MoveView(longitude, latitude, mapView);

        currentLocation.setItemName("현재 위치");
        currentLocation.setTag(0);

        currentLocation.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));
        currentLocation.setCustomImageResourceId(R.drawable.current_location_maker);
        currentLocation.isCustomImageAutoscale();
        currentLocation.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        mapView.addPOIItem(currentLocation);

    }
    public void MoveView(double longitude, double latitude,MapView mapView){
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true);
        mapView.setZoomLevel(1, true);
        mapView.zoomIn(true);
        mapView.zoomOut(true);
    }

    // 위치 권한 설정 부분

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                } else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission() {

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}