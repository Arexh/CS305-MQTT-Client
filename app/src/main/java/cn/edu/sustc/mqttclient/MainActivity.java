package cn.edu.sustc.mqttclient;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static String TOPIC_SERVER_ADDRESS="192.168.1.100";
    public static int TOPIC_SERVER_PORT=8080;
    public final static int PUBLISH_INTERVAL=1000;
    public static String serverURL="tcp://192.168.1.100:61613";
    public static String TOPIC_HEAD="home/mobile/";
    public static String userName="admin";
    public static String password="password";
    private static Toast mToast = null;
    public static boolean connection_MAC = false;
    public static MqttAndroidClient client;
    public static MqttAndroidClient client_get_MAC;
    public static Context contextWrapper;
    public static String info_="";
    public static boolean connection=false;
    public static boolean publish_jug=false;
    public static String MAC_ADDRESS;
    public static List<Sensor> sensors;
    public static HashMap<Integer,Integer> hashMap=new HashMap<>();
    private SensorManager sensorManager;
    public static String[] sensorDate;
    public static boolean refresh=false;
    public static ArrayList<String> device_list;
    public static HashMap<String,String> hashMap_time;
    public static boolean detail_refresh=false;
    public static Activity activity;
    public static MainActivity mainActivity;
    public static Runnable sendMACAddress=new Runnable() {
        @Override
        public void run() {
            new Thread(publish_MAC_process).run();
        }
    };
    public static Runnable queryMAC=new Runnable() {
        @Override
        public void run() {
            device_list=new ArrayList<>();
            subsribeMAC();
            client_get_MAC.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                    }
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                    try{
                        String[] input=new String(message.getPayload()).split("&");
                        String MAC=input[0];
                        String Time=input[1];
                        if(!MAC.equals(MAC_ADDRESS)&&!device_list.contains(MAC)){
                            device_list.add(MAC);
                            hashMap_time.put(MAC,Time);
                        }
                        if(device_list.contains(MAC)){
                            hashMap_time.put(MAC,Time);
                        }
                    }catch (Exception e){

                    }
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }
    };
    public static Runnable publish_process=new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    if (!connection || !publish_jug) {
                        return;
                    } else {
                        for (int x = 0; x < sensors.size(); x++) {
                            String sensorName = sensors.get(x).getName();
                            String data = sensorDate[x] == null ? "Not activated" : sensorDate[x];
                            publish(sensorName, data);
                            Thread.sleep(20);
                        }
                    }
                    int interval_number=1000;
                    String interval=load("sendRate");
                    if(interval==null){
                        interval_number=PUBLISH_INTERVAL;
                    }else if(interval.equals("0.5")){
                        interval_number=500;
                    }else if(interval.equals("1")){
                        interval_number=1000;
                    }else if(interval.equals("3")){
                        interval_number=3000;
                    }else if(interval.equals("5")){
                        interval_number=5000;
                    }
                    Thread.sleep(interval_number);
                } catch (Exception e) {
                    System.out.println(e);
                    return;
                }
            }
        }
    };
    public static Runnable publish_MAC_process=new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    if (!connection_MAC) {
                        return;
                    } else {
                        publishInfo(MAC_ADDRESS);
                    }
                    Thread.sleep(10000);
                } catch (Exception e) {
                    System.out.println(e);
                    return;
                }
            }
        }
    };
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFrment = null;
                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            selectedFrment = new HomeFragment();
                            break;
                        case R.id.nav_list:
                            if(connection)
                                selectedFrment = new ListFragment();
                            else{
                                showToast("Please connect to server first.");
                                return false;
                            }
                            break;
                        case R.id.nav_info:
                            selectedFrment = new InfoFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                            ,selectedFrment).commit();
                    return true;
                }
            };
    public static Thread publishThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity=this;
        hashMap_time = new HashMap<>();
        device_list=new ArrayList<>();
        contextWrapper = this.getApplicationContext();
        BottomNavigationView bottomNav=findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                        new HomeFragment()).commit();
        MAC_ADDRESS=GetMAC.getMac(this);
        getSensorList(this);
        sensorDate=new String[hashMap.size()];
        setSensor(this);
        activity=this;
    }
    public static void publish(String sensorName,String sensorData){
        String topic = TOPIC_HEAD + MAC_ADDRESS + "/" +sensorName;
        String payload;
        if(sensorData==null){
            payload="Not activated";
        }else{
            payload=sensorData;
        }
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public static void publishInfo(String MAC){
        String topic = "device/"+MAC;
        String payload;
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now=new Date();
        payload=MAC+"&"+simpleDateFormat.format(now);
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client_get_MAC.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public static void publishList(){
        String topic = TOPIC_HEAD + MAC_ADDRESS + "/" + "LIST";
        String payload="";
        for(Sensor sensor:sensors){
            payload+=sensor.getName()+"&";
        }
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public static void connectMAC(){
        String clientId = MqttClient.generateClientId();
        client_get_MAC = new MqttAndroidClient(MainActivity.contextWrapper, serverURL, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(userName);
        options.setPassword(password.toCharArray());
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        try {
            IMqttToken token = client_get_MAC.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    connection_MAC  = true;
                    Thread mac=new Thread(MainActivity.sendMACAddress);
                    Thread q=new Thread(MainActivity.queryMAC);
                    mac.start();
                    q.start();
                    showToast("Connect to MAC successfully!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    showToast("Fail to connect MAC!");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public static void connect(){
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(MainActivity.contextWrapper, serverURL, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(userName);
        options.setPassword(password.toCharArray());
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    connection=true;
                    showToast("Connect Successfully!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    HomeFragment.connectSwitch.setChecked(false);
                    connection=false;
                    showToast("Fail to connect to server,please check.");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public static void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
    public void getSensorList(FragmentActivity activity){
        SensorManager mSensorManager;
        List<Sensor> sensorList;
        // 实例化传感器管理者
        mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        // 得到设置支持的所有传感器的List
        sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        sensors = sensorList;
        int count=0;
        for(Sensor sensor:sensorList){
            hashMap.put(sensor.getType(),count++);
        }
    }
    public void setSensor(FragmentActivity activity){
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        SensorEventListener sensorEventListener=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                int position=MainActivity.hashMap.get(event.sensor.getType());
                sensorDate[position]=java.util.Arrays.toString(event.values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        for(Sensor sensor:MainActivity.sensors){
            sensorManager.registerListener(sensorEventListener,sensorManager.getDefaultSensor(sensor.getType()),SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    public static String[] getAddress(){
        String[] re;
        String temp=load("serverIP");
        try{
            re=temp.split("//")[1].split(":");
            return re;
        }catch (Exception e){
            return new String[]{TOPIC_SERVER_ADDRESS,""+TOPIC_SERVER_PORT};
        }
    }
    public static String load(String fileName) {
        FileInputStream fis = null;
        String re="";
        try {
            fis = mainActivity.openFileInput(fileName+".txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            while ((text = br.readLine()) != null) {
                re+=text;
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return re;
    }
    public static void subsribeMAC(){
        int qos = 2;
        try {
            IMqttToken subToken = MainActivity.client_get_MAC.subscribe("device/#", qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
