package kankan.wheel.demo.extended;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

/**
 * 适配滚轮View
 */
public class WheelAdapter<T> extends AbstractWheelTextAdapter {
    private int normalColor;
    private int specialColor;
    private List<T> data;
    private WheelView mWheelView;
    private int currentIndex;

    public WheelAdapter(WheelView wheelView, Context context, List<T> data) {
        super(context);
        this.data = data;
        mWheelView = wheelView;
        init(context);
    }

    /**
     * @param context
     * @param itemResource     一种包含一个TextView布局文件时使用实例化项目视图的资源ID
     * @param itemTextResource 在指定布局里的TextView的id
     */
    public WheelAdapter(WheelView wheelView, Context context, int itemResource, int itemTextResource, List<T> data) {
        super(context, itemResource, itemTextResource);
        this.data = data;
        mWheelView = wheelView;
        init(context);
    }

    private void init(Context context) {
        normalColor = Color.DKGRAY;
        specialColor = Color.BLUE;
    }

    @Override
    protected void configureTextView(TextView view) {
        if(mWheelView.getCurrentItem() == currentIndex) {
            view.setTextColor(specialColor);
            view.setTextSize(22);
        } else {
            view.setTextColor(normalColor);
            view.setTextSize(18);
        }
    }

    @Override
    protected CharSequence getItemText(int index) {
        return data.get(index).toString();
    }

    @Override
    public View getItem(int index, View convertView, ViewGroup parent) {
        currentIndex = index;
        return super.getItem(index, convertView, parent);
    }

    @Override
    public int getItemsCount() {
        return data.size();
    }
}
