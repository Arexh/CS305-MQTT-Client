package cn.edu.sustc.mqttclient;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class GraphFragment extends AppCompatActivity {
    private DynamicLineChartManager dynamicLineChartManager2;
    private List<Float> list = new ArrayList<>(); //数据集合
    private List<String> names = new ArrayList<>(); //折线名字集合
    private List<Integer> colour = new ArrayList<>();//折线颜色集合
    private String info;
    private String sensorName;
    private String example;
    private float max;
    private float min;
    private int[] colors=new int[]{Color.CYAN,Color.GREEN,Color.BLUE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        info=DetailFragment.info_;
        sensorName=DetailFragment.sensorName;
        example=DetailFragment.example;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_graph);
        LineChart mChart2 = (LineChart) findViewById(R.id.dynamic_chart2);
        TextView textView=findViewById(R.id.SensorName);
        textView.setText(sensorName);
        String a=example;
        if(a!=null){
            a=a.replace(" ","");
            a=a.replace("[","");
            a=a.replace("]","");
            String[] input=a.split(",");
            max=Float.MIN_VALUE;
            min=Float.MAX_VALUE;
            int length=input.length;
            for(String read:input){
                float number=Float.parseFloat(read);
                max=Math.max(number,max);
                min=Math.min(number,min);
            }
            //折线名字
            for(int x=0;x<length;x++){
                names.add("Data"+x);
                colour.add(colors[x%colors.length]);
            }
            //折线颜色
            if(example!=null){
                subscribe();
            }
            dynamicLineChartManager2 = new DynamicLineChartManager(mChart2, names, colour);

            dynamicLineChartManager2.setYAxis(max*=2, min/=10, 10);

            MainActivity.client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String message_string=new String(message.getPayload());
                    message_string=message_string.replace(" ","");
                    message_string=message_string.replace("[","");
                    message_string=message_string.replace("]","");
                    String[] input=message_string.split(",");
                    float max_=Float.MIN_VALUE;
                    float min_=Float.MAX_VALUE;
                    for(String read:input){
                        float number=Float.parseFloat(read);
                        max_=Math.max(number,max_);
                        min_=Math.min(number,min_);
                        list.add(number);
                    }
                    dynamicLineChartManager2.setYAxis(max_*2, min_/2, 10);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dynamicLineChartManager2.addEntryFloat(list);
                            list.clear();
                        }
                    });
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }
    }

    public void subscribe(){
        String topic = "home/mobile/"+this.info+"/"+sensorName;
        int qos = 2;
        try {
            IMqttToken subToken = MainActivity.client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
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
    public void unscribe(){
        final String topic = "home/mobile/"+info+"/"+sensorName;
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
    public void onStop() {
        unscribe();
        super.onStop();
    }
}
