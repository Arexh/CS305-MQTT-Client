package cn.edu.sustc.mqttclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HomeFragment extends Fragment {
    private final String FILE_NAME="topics.txt";
    public static Switch connectSwitch,publishSwitch;
    public static EditText text;
    private EditText mEditText;
    private Toast mToast;
    private View view;
    private ListView listView;
    private AppCompatImageButton setting_button;
    private ArrayList<String> list;
    private ArrayList<String> topics;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home,container,false);
        setting_button = view.findViewById(R.id.setting_button);
        mEditText = view.findViewById(R.id.subscribe);
        LimitsEditEnter(mEditText);
        listView = view.findViewById(R.id.list_subscribe);
        list=MainActivity.device_list;
        topics=load_topics();
        final HomeCustomAdapter customAdpter=new HomeCustomAdapter();
        listView.setAdapter(customAdpter);
        connectSwitch = view.findViewById(R.id.connectSwitch);
        publishSwitch = view.findViewById(R.id.publishSwitch);
        text=view.findViewById(R.id.address);
        final String temp;
        if((temp=load("serverIP"))==null){
            text.setText(MainActivity.serverURL);
        }else{
            text.setText(temp);
        }
        if(MainActivity.connection){
            connectSwitch.setChecked(true);
        }
        if(MainActivity.publish_jug){
            publishSwitch.setChecked(true);
        }
        Button write_btn=view.findViewById(R.id.write);
        write_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(topics==null||!topics.contains(MainActivity.device_list.get(position))){
                    save_("topics",MainActivity.device_list.get(position));
                    showToast("Subscribed");
                }else{
                    showToast("Already subscribed");
                }
            }
        });
        setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final NumberPicker numberPicker=new NumberPicker(getActivity());
                final String[] numbers=new String[]{"0.5","1","3","5"};
                numberPicker.setDisplayedValues(numbers);
                numberPicker.setMinValue(0);
                numberPicker.setMaxValue(3);
                String number=load("sendRate");
                if(number==null)
                    number="1";
                if(number.equals("0.5")){
                    numberPicker.setValue(0);
                }else if(number.equals("1")){
                    numberPicker.setValue(1);
                }else if(number.equals("3")){
                    numberPicker.setValue(2);
                }else{
                    numberPicker.setValue(3);
                }
                numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity()).setView(numberPicker);
                builder.setTitle("Sending rate(per seconds)").setIcon(R.drawable.ic_settings_black_24dp);
                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        save("sendRate", numbers[numberPicker.getValue()]);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }

        });
        connectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(!text.getText().toString().equals(MainActivity.serverURL)){
                        MainActivity.serverURL=text.getText().toString();
                        save("serverIP",text.getText().toString());
                    }
                    MainActivity.connect();
                    MainActivity.connectMAC();
                }else{
                    try {
                        MainActivity.publish_jug=false;
                        MainActivity.connection=false;
                        MainActivity.connection_MAC=false;
                        MainActivity.client_get_MAC.disconnect();
                        MainActivity.client.disconnect();
                        publishSwitch.setChecked(false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    showToast("Connection closed.");
                }
            }
        });
        publishSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(MainActivity.connection){
                        MainActivity.publishList();
                        MainActivity.publish_jug=true;
                        MainActivity.publishThread=new Thread(MainActivity.publish_process);
                        MainActivity.publishThread.start();
                        showToast("Publish start!");
                    }else{
                        publishSwitch.setChecked(false);
                        showToast("Please connect to MQTT server first.");
                    }
                }else{
                    MainActivity.publish_jug=false;
                    showToast("Publish stop!");
                }
            }
        });
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipeLayout);

        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);;
        //swipeRefreshLayout.setPadding(20, 20, 20, 20);
        //swipeRefreshLayout.setProgressViewOffset(true, 100, 200);
        //swipeRefreshLayout.setDistanceToTriggerSync(50);
        swipeRefreshLayout.setSize(10);
        swipeRefreshLayout.setProgressViewEndTarget(true, 100);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!MainActivity.connection_MAC)
                    showToast("Please connect to server before get device list!");
                new Handler().postDelayed(new Runnable() {//模拟耗时操作
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);//取消刷新
                    }
                },1000);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(customAdpter);
                    }
                });
            }
        });
        return view;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(view.getContext(), msg, Toast.LENGTH_LONG);
        } else {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToast.show();
    }
public void save() {
    String text = mEditText.getText().toString()+"\n";
    FileOutputStream fos = null;
    try {
        fos = getActivity().openFileOutput(FILE_NAME, Context.MODE_APPEND);
        fos.write(text.getBytes());
        mEditText.getText().clear();
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
    public void save(String fileName,String info) {
        if(info==null)
            return;
        String text = info+"\n";
        FileOutputStream fos = null;
        try {
            fos = getActivity().openFileOutput(fileName+".txt", Context.MODE_PRIVATE);
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
    public void save_(String fileName,String info) {
        if(info==null)
            return;
        String text = info+"\n";
        FileOutputStream fos = null;
        try {
            fos = getActivity().openFileOutput(fileName+".txt", Context.MODE_APPEND);
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
    public void delete() {
        String text = "";
        FileOutputStream fos = null;
        try {
            fos = getActivity().openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(text.getBytes());
            mEditText.getText().clear();
            showToast("Deleted!");
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
    public static void LimitsEditEnter(EditText et) {
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);  //过滤回车键输入
            }
        });
    }
    public ArrayList<String> load_list() {
        FileInputStream fis = null;
        ArrayList<String> re = null;
        try {
            fis = getActivity().openFileInput(FILE_NAME);
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
        return re;
    }
    public String load(String fileName) {
        FileInputStream fis = null;
        String re="";
        try {
            fis = getActivity().openFileInput(fileName+".txt");
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
    class HomeCustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return MainActivity.device_list.size();
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
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            boolean jug=false;
            try {
                Date date=simpleDateFormat.parse(MainActivity.hashMap_time.get(MainActivity.device_list.get(position)));
                long a = new Date().getTime();
                long b = date.getTime();
                int c = (int)((a - b) / 1000);
                if(c<=30)
                    jug=true;
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
            if(jug){
                id.setText("Mobile Phone " + position + "(Online)");
                ImageView imageView=convertView.findViewById(R.id.imageView);
                imageView.setColorFilter(Color.rgb(0,102,102));
            }else
                id.setText("Mobile Phone " + position + "(Offline)");
            description.setText(MainActivity.device_list.get(position));
            return convertView;
        }
    }
    public ArrayList<String> load_topics() {
        FileInputStream fis = null;
        ArrayList<String> re = null;
        try {
            fis = getActivity().openFileInput("topics.txt");
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
        return re;
    }
}
