package cn.edu.sustc.mqttclient;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class myListView extends ListView {

    private myAdapter md;

    public myListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
     * 拦截手指触摸
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (md.lockView) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN
                    || ev.getAction() == MotionEvent.ACTION_MOVE) {
                // 让滑动出删除按钮的那个itemView退回去
                if (md.mView != null) {
                    md.scrollTo(md.mView, HorizontalScrollView.FOCUS_LEFT);
                    md.mView = null;
                }
                return true;
            }
            if (ev.getAction() == MotionEvent.ACTION_UP) {
                md.lockView = false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        md = (myAdapter) adapter;
        super.setAdapter(adapter);
    }

}
