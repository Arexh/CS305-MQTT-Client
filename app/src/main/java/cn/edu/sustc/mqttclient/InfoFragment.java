package cn.edu.sustc.mqttclient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class InfoFragment extends Fragment {
    private TextView[] textViews;
    private ListView listView;
    private static Handler info_hendler;
    private Thread thread;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info,container,false);
        MainActivity.refresh=true;
        listView = view.findViewById(R.id.list_view_info);
        textViews=new TextView[MainActivity.sensors.size()];
        for(int x=0;x<MainActivity.sensors.size();x++){
            textViews[x]=new TextView(getActivity());
        }
        CustomAdpter_info customAdpter=new CustomAdpter_info();
        listView.setAdapter(customAdpter);
        info_hendler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        for(int x=0;x<MainActivity.hashMap.size();x++){
                            String data=MainActivity.sensorDate[x];
                            if(data!=null){
                                textViews[x].setText(data);
                            }
                        }
                }
                return false;
            }
        });
        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(MainActivity.refresh){
                    info_hendler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        return view;
    }
    class CustomAdpter_info extends BaseAdapter {

        @Override
        public int getCount() {
            return MainActivity.hashMap.size()+1;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.info_layout,null);
            TextView id = convertView.findViewById(R.id.sensor_name);
            TextView description = convertView.findViewById(R.id.sensor_value);
            if(position==0){
                id.setText("Your MAC address: ");
                description.setText(MainActivity.MAC_ADDRESS);
            }else{
                id.setText(MainActivity.sensors.get(position-1).getName()+":");
                textViews[position-1]=description;
                if(MainActivity.sensorDate[position-1]==null){
                    description.setText("Not activated");
                }else description.setText(MainActivity.sensorDate[position-1]);
            }
            return convertView;
        }
    }
    public void onStop() {
        MainActivity.refresh=false;
        super.onStop();
    }
}
