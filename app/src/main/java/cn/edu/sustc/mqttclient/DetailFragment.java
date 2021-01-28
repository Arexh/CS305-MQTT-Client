package cn.edu.sustc.mqttclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class DetailFragment extends AppCompatActivity {
    static String info;
    static Toast mToast;
    static private ListView listView;
    static private TextView[] textViews;
    static private String[] key;
    static private String[] values;
    static public Activity activity;
    static private HashMap<String,Integer> hashMap;
    private static Handler info_hendler;
    private static Thread thread;
    private static CustomAdpter_info customAdpter;
    public static String info_,sensorName,example;
//    public DetailFragment(String info){
//        this.info=info;
//    }

    @Override
    protected void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_info);
        MainActivity.detail_refresh=true;
        listView = findViewById(R.id.list_view_info);
        activity=this;
        hashMap=new HashMap<>();
        customAdpter=new CustomAdpter_info();
        info=MainActivity.info_;
        String input=load(info);
        if(input!=null){
            key=input.split("&");
            for(int x=0;x<key.length;x++){
                hashMap.put(key[x],x);
            }
            values=new String[key.length];
            textViews=new TextView[key.length];
            for(int x=0;x<key.length;x++){
                textViews[x]=new TextView(this);
            }
        }else{
            key=new String[0];
        }
        listView.setAdapter(customAdpter);
        new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                    }
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        try{
                            String[] input=topic.split("/");
                            values[hashMap.get(input[3])]=new String(message.getPayload());
                        }catch (Exception e){

                        }
                    }
                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });
                subcribe();
            }
        }).start();
        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(MainActivity.detail_refresh){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(values!=null){
                                for(int x=0;x<values.length;x++){
                                    String data=values[x];
                                    if(data!=null&&textViews[x]!=null){
                                        textViews[x].setText(data);
                                    }
                                }
                            }
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    class CustomAdpter_info extends BaseAdapter {

        @Override
        public int getCount() {
            return key.length+1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.info_layout,null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(position!=0){
                        info_ = info;
                        sensorName = key[position-1];
                        example =values[position-1];
                        Intent intent = new Intent();
                        intent.setClass(DetailFragment.activity, GraphFragment.class);
                        DetailFragment.activity.startActivity(intent);
                    }
                }
            });
            TextView id = convertView.findViewById(R.id.sensor_name);
            TextView description = convertView.findViewById(R.id.sensor_value);
            if(position==0){
                id.setText("MAC address: ");
                description.setText(info);
            }else{
                id.setText(key[position-1]+":");
                textViews[position-1]=description;
                if(values[position-1]==null){
                    description.setText("Not activated");
                }else description.setText(values[position-1]);
            }
            return convertView;
        }
    }
    public void onStop() {
        unscribe();
        MainActivity.refresh=false;
        super.onStop();
    }

    @Override
    protected void onStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                    }
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        try{
                            String[] input=topic.split("/");
                            values[hashMap.get(input[3])]=new String(message.getPayload());
                        }catch (Exception e){

                        }
                    }
                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });
                subcribe();
            }
        }).start();
        super.onStart();
    }

    public void subcribe(){
        String topic = "home/mobile/"+info+"/#";
        int qos = 2;
        try {
            IMqttToken subToken = MainActivity.client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    showToast("Connect successfully!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    showToast("Fail to connect, please check the network");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void unscribe(){
        final String topic = "home/mobile/"+info+"/#";
        try {
            IMqttToken unsubToken = MainActivity.client.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        } else {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToast.show();
    }
    public String load(String MAC) {
        String fileName=MAC+".txt";
        FileInputStream fis = null;
        ArrayList<String> re = null;
        try {
            fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            re=new ArrayList<>();
            String text;
            while ((text = br.readLine()) != null) {
                re.add(text);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
        if(re!=null){
            return re.get(0);
        }
        return null;
    }

}
