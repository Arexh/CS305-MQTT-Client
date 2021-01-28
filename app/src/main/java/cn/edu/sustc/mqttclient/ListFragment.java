package cn.edu.sustc.mqttclient;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ListFragment extends Fragment {
    private final String FILE_NAME="topics.txt";
    private ArrayList<String> list_;
    private ArrayList<String> al = new ArrayList<String>();
    private myListView lv;
    private static View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.list_view,container,false);
        lv = view.findViewById(R.id.lv);
        list_=load(FILE_NAME);
        if(list_!=null){
            for(int x=0;x<list_.size();x++){
                ArrayList<String> re=load(list_.get(x)+"_nick.txt");
                if(re==null||re.size()==0){
                    al.add("Unnamed");
                }else{
                    al.add(re.get(0));
                }
            }
        }
        myAdapter myadapter = new myAdapter(getActivity(), al,list_,getActivity());
        lv.setAdapter(myadapter);
        MainActivity.client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String[] input=topic.split("/");
                if(input[3].equals("LIST")){
                    String temp=new String(message.getPayload());
                    save(input[2],temp);
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        if(list_!=null){
            for(String re:list_){
                subcribeList(re);
            }
        }
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Fragment selectedFrment = new DetailFragment(list_.get(position));
//                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
//                        ,selectedFrment).commit();
//            }
//        });
        return view;
    }
    public class CustomAdpter extends BaseAdapter{

        private ArrayList<String> list;

        CustomAdpter(ArrayList<String> list){
            this.list=list;
        }

        @Override
        public int getCount() {
            return list.size();
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
            convertView = getLayoutInflater().inflate(R.layout.data_layout,null);
            TextView id = convertView.findViewById(R.id.textView_id);
            TextView description = convertView.findViewById(R.id.textView_description);
            id.setText("Mobile Phone " + position);
            description.setText(list.get(position));
            return convertView;
        }
    }
    public static void subcribeList(String info){
        String topic = "home/mobile/"+info+"/LIST";
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
    public ArrayList<String> load(String fileName) {
        FileInputStream fis = null;
        ArrayList<String> re = null;
        try {
            fis = getActivity().openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            re=new ArrayList<>();
            String text;
            while ((text = br.readLine()) != null) {
                re.add(text);
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
    public void delete(String fileName,String MAC){
        FileInputStream fis = null;
        ArrayList<String> re = null;
        try {
            fis = getActivity().openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            re=new ArrayList<>();
            String text;
            while ((text = br.readLine()) != null) {
                if(text.equals(MAC)){
                    continue;
                }
                re.add(text);
            }
            String temp="";
            for(String str:re){
                temp+=str+"\n";
            }
            save("topics",temp);
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
    }
    public void save(String MAC,String text) {
        FileOutputStream fos = null;
        try {
            fos = getActivity().openFileOutput(MAC+".txt", Context.MODE_PRIVATE);
            fos.write(text.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void saveNiki(String MAC,String Niki) {
        FileOutputStream fos = null;
        try {
            fos = getActivity().openFileOutput(MAC+"_niki.txt", Context.MODE_PRIVATE);
            fos.write(Niki.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
