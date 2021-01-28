package cn.edu.sustc.mqttclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class myAdapter extends BaseAdapter {
    private Context mc;
    private int width;
    public HorizontalScrollView mView;
    public boolean lockView = false;
    private ArrayList<String> al,MAC;
    private FragmentActivity activity;

    public myAdapter(Context mc, ArrayList<String> al, ArrayList<String> MAC, FragmentActivity activity) {
        //得到屏幕宽度
        WindowManager wm = (WindowManager) mc
                .getSystemService(mc.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();

        this.mc = mc;
        this.al = al;
        this.MAC = MAC;
        this.activity=activity;
    }

    private class myTouchListener implements OnTouchListener {

        private int startX;
        private int startY;
        private boolean jug=false;
        private boolean jug_=false;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
//                    // 为了平移方便，把view强转
//                    final HorizontalScrollView view = (HorizontalScrollView) v;
//                    // 抬手位置
//                    int endX = (int) event.getX();
//                    int endY = (int) event.getY();
//
//                    // 如果向左划
//                    if (!jug && startX - endX > 0) {
//                        // 1.view设置移动效果
//                        scrollTo(view, HorizontalScrollView.FOCUS_RIGHT);
//                        mView = view;
//                        jug=true;
//                        Log.d("移动", "左移");
//                    } else {
//                        jug=false;
//                        scrollTo(view, HorizontalScrollView.FOCUS_LEFT);
//                    }

                    break;
                case MotionEvent.ACTION_DOWN:
                    if(jug_){
                        jug_=false;
                        scrollTo((HorizontalScrollView)v, HorizontalScrollView.FOCUS_LEFT);
                    }else{
                        jug_=true;
                        scrollTo((HorizontalScrollView)v, HorizontalScrollView.FOCUS_RIGHT);
                    }
//                    //如果手指按下并且有的侧滑没关闭，就关闭
//                    if (mView != null) {
//                        scrollTo(mView, HorizontalScrollView.FOCUS_LEFT);
//                        lockView = true;
//                        mView = null;
//                        return true;
//                    }
//                    // 手指按下后就会记录位置
//                    startX = (int) event.getX();
//                    startY = (int) event.getY();
//                    break;
                case MotionEvent.ACTION_MOVE:

                    break;

                default:
                    break;
            }
            return false;
        }



    }
    public void scrollTo(final HorizontalScrollView view, final int way) {
        view.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                // 2.使用HorizontalScrollView特有函数进行平移一个页面，把button隐藏
                view.pageScroll(way);

            }
        });
    }
    private class viewHolder {
        public TextView tv;
        public Button bt;
        public Button bt_left;
        public Button bt_detail;
        public TextView MAC;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            viewHolder vh = new viewHolder();
            convertView = (HorizontalScrollView) View.inflate(mc,
                    R.layout.horizontal_scroll_view, null);
            // 设置滑动事件，设置后就能滑动了
            final int position_=position;
            convertView.setOnTouchListener(new myTouchListener());
            vh.tv = (TextView) convertView.findViewById(R.id.tv_text);
            vh.MAC=convertView.findViewById(R.id.MAC);
            vh.bt_detail=convertView.findViewById(R.id.bt_del2);
            vh.MAC.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
//            vh.MAC.setLayoutParams(new LinearLayout.LayoutParams(width,LinearLayout.LayoutParams.MATCH_PARENT));
//            // 设置textview的宽度，把button挤出屏幕
//            vh.tv.setLayoutParams(new LinearLayout.LayoutParams(width,
//                    LinearLayout.LayoutParams.MATCH_PARENT));

            // 常规操作
            vh.bt_detail = convertView.findViewById(R.id.bt_del3);//de
            vh.bt = convertView.findViewById(R.id.bt_del);//un
            vh.bt_left= convertView.findViewById(R.id.bt_del2);//re
            convertView.setTag(vh);
            vh.bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    al.remove(position_);
                    System.out.println(MAC.get(position_));
                    delete("topics.txt",MAC.remove(position_));
                    notifyDataSetChanged();
                }
            });
            vh.bt_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.activity, DetailFragment.class);
                    MainActivity.info_=MAC.get(position);
                    MainActivity.activity.startActivity(intent);
                }
            });
            vh.bt_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final EditText et = new EditText(activity);
                    et.setPadding(100,50,100,50);
                    et.setSingleLine(true);
                    new AlertDialog.Builder(activity).setTitle("Please Input Nickname:")
                            .setIcon(R.drawable.ic_edit_black_24dp)
                            .setView(et)
                            .setPositiveButton("Cancel",null).setNegativeButton("OK",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //按下确定键后的事件
                            save(MAC.get(position)+"_nick",et.getText().toString());
                            al.set(position,et.getText().toString());
                            notifyDataSetChanged();
                            Toast.makeText(activity, "Edit Successfully!",Toast.LENGTH_LONG).show();
                        }
                    }).show();
                }
            });
        }

        // 常规操作
        viewHolder vh = (viewHolder) convertView.getTag();
        String str = al.get(position);
        vh.tv.setText(str);
        vh.MAC.setText(this.MAC.get(position));
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return al.get(position);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return al.size();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
    public void delete(String fileName,String MAC){
        FileInputStream fis = null;
        ArrayList<String> re ;
        try {
            fis = activity.openFileInput(fileName);
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
    public void save(String fileName,String text) {
        FileOutputStream fos = null;
        try {
            fos = activity.openFileOutput(fileName+".txt", Context.MODE_PRIVATE);
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
}
