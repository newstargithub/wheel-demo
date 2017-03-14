package kankan.wheel.demo.extended;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.WheelViewAdapter;


public class AreaActivity extends Activity {

    private WheelView wheel_province;
    private WheelView wheel_city;
    private WheelView wheel_area;
    private Context mContext;
    private List<CityDTO> mProvinceList;
    private List<CityDTO> mCityList;
    private List<CityDTO> mCountyList;
    private CityDTO mProvince, mCity, mCounty;
    private int mLastProvinceIndex, mLastCityIndex, mLastCountyIndex = 0;
    private TextView tv_select_city;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area);
        mContext = this;
        tv_select_city = (TextView) findViewById(R.id.tv_select_city);
        wheel_province = (WheelView) findViewById(R.id.province);
        wheel_city = (WheelView) findViewById(R.id.city);
        wheel_area = (WheelView) findViewById(R.id.area);
        wheel_province.addScrollingListener(wheelScrollListener);
        wheel_city.addScrollingListener(wheelScrollListener);
        wheel_area.addScrollingListener(wheelScrollListener);
        wheel_province.addChangingListener(wheelChangedListener);
        wheel_city.addChangingListener(wheelChangedListener);
        setWheelView(wheel_province);
        setWheelView(wheel_city);
        setWheelView(wheel_area);
        database = openDatabase();
        getProvince();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(database != null) {
            database.close();
        }
    }

    private void setWheelView(WheelView wheelView) {
        wheelView.setVisibleItems(5);
//        wheelView.setWheelForeground(R.drawable.icon_tips_line);
//        wheelView.setWheelBackground(R.drawable.white);
        wheelView.setDrawShadows(true);
//        wheelView.setShadowColor(0xFFFFFFFF, 0x7FFFFFFF, 0x00FFFFFF);
        setEmptyViewAdapter(wheelView);
    }

    private void setEmptyViewAdapter(WheelView wheelView) {
        WheelViewAdapter viewAdapter = new WheelAdapter<CityDTO>(wheelView, mContext, R.layout.item_select_location,
                R.id.tv_text, new ArrayList<CityDTO>()) {
        };
        wheelView.setViewAdapter(viewAdapter);
    }

    public void getProvince() {
        if (mProvinceList == null) {
            mProvince = null;
            mProvinceList = getAllProvince();
        }
        updateProvince(mProvinceList, mLastProvinceIndex);
    }

    private List<CityDTO> getAllProvince() {
        //SELECT * FROM area WHERE area.father IS NULL;
        String selection = "father IS NULL";
        Cursor cursor = database.query("area", null, selection, null, null, null, null);
        List<CityDTO> list = getCityList(cursor);
        cursor.close();
        return list;
    }

    private SQLiteDatabase openDatabase() {
        File dir = mContext.getDir("database", Context.MODE_PRIVATE);
        if(dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "address.db");
        if(file.length() == 0) {
            try {
                InputStream inputStream = new BufferedInputStream(mContext.getAssets().open("address.db"));
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                byte[] buffer = new byte[2048];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String path = file.getAbsolutePath();
        return SQLiteDatabase.openOrCreateDatabase(path, null);
    }

    private List<CityDTO> getCityList(Cursor cursor) {
        List<CityDTO> list = new ArrayList<>();
        CityDTO item;
        while (cursor.moveToNext()) {
            item = new CityDTO();
            item.id = cursor.getInt(cursor.getColumnIndex("id"));
            item.areaID = cursor.getString(cursor.getColumnIndex("areaID"));
            item.father = cursor.getString(cursor.getColumnIndex("father"));
            item.area = cursor.getString(cursor.getColumnIndex("area"));
            item.areaType = cursor.getString(cursor.getColumnIndex("areaType"));
            list.add(item);
        }
        return list;
    }

    private void updateProvince(final List<CityDTO> list, int currentItem) {
        WheelViewAdapter viewAdapter = new WheelAdapter<>(wheel_province, mContext, R.layout.item_select_location,
                R.id.tv_text, list);
        wheel_province.setViewAdapter(viewAdapter);
        wheel_province.setCurrentItem(currentItem);
        getCity(list.get(currentItem));
    }

    private void getCity(CityDTO province) {
        if (mCityList != null && !mCityList.isEmpty() && province.areaID.equals(mCityList.get(0).father)) {
            //已有市的数据
        } else {
            clearCity();
            clearCounty();
            //获取新的市的数据
            mCityList = getChildCity(province.areaID);
        }
        updateCity(mCityList, mLastCityIndex);
    }

    private List<CityDTO> getChildCity(String areaID) {
        String selection = "father = ?";
        String[] selectionArgs = {areaID};
        Cursor cursor = database.query("area", null, selection, selectionArgs, null, null, null);
        List<CityDTO> list = getCityList(cursor);
        cursor.close();
        return list;
    }

    private void updateCity(final List<CityDTO> cityList, int currentItem) {
        WheelViewAdapter viewAdapter = new WheelAdapter<>(wheel_city, mContext, R.layout.item_select_location,
                R.id.tv_text, cityList);
        wheel_city.setViewAdapter(viewAdapter);
        wheel_city.setCurrentItem(currentItem);
        if (cityList != null && !cityList.isEmpty()) {
            CityDTO city = cityList.get(currentItem);
            getCounty(city);
        } else {
            clearCounty();
            updateCounty(mCountyList, mLastCountyIndex);
        }
    }

    private void getCounty(CityDTO city) {
        if (mCountyList != null && !mCountyList.isEmpty() && city.areaID.equals(mCountyList.get(0).father)) {
            //已有区的数据
        } else {
            clearCounty();
            mCountyList = getChildCity(city.areaID);
        }
        updateCounty(mCountyList, mLastCountyIndex);
    }

    private void updateCounty(List<CityDTO> countyList, int currentItem) {
        WheelViewAdapter viewAdapter = new WheelAdapter<>(wheel_area, mContext, R.layout.item_select_location,
                R.id.tv_text, countyList);
        wheel_area.setViewAdapter(viewAdapter);
        wheel_area.setCurrentItem(currentItem);
        onCompleteOperation();
    }

    private void onCompleteOperation() {
        int provinceItem = wheel_province.getCurrentItem();
        int cityItem = wheel_city.getCurrentItem();
        int countyItem = wheel_area.getCurrentItem();
        if (mProvinceList != null && !mProvinceList.isEmpty()) {
            mLastProvinceIndex = provinceItem;
            mProvince = mProvinceList.get(provinceItem);
        }
        if (mCityList != null && !mCityList.isEmpty()) {
            mLastCityIndex = cityItem;
            mCity = mCityList.get(cityItem);
        }
        if (mCountyList != null && !mCountyList.isEmpty()) {
            mLastCountyIndex = countyItem;
            mCounty = mCountyList.get(countyItem);
        }
        String location = getLocationString(mProvince, mCity, mCounty);
        tv_select_city.setText(location);
    }

    /** 得到省市县的显示字符串 */
    public static String getLocationString(CityDTO province, CityDTO city, CityDTO area){
        StringBuilder sb = new StringBuilder();
        if (province != null) {
            sb.append(province.area);
        }
        if (city != null) {
            sb.append(city.area);
        }
        if (area != null) {
            sb.append(area.area);
        }
        return sb.toString();
    }

    private void clearCity() {
        mLastCityIndex = 0;
        mCity = null;
        if (mCityList != null) {
            mCityList.clear();
        }
        setEmptyViewAdapter(wheel_city);
    }

    private void clearCounty() {
        mLastCountyIndex = 0;
        mCounty = null;
        if (mCountyList != null) {
            mCountyList.clear();
        }
        setEmptyViewAdapter(wheel_area);
    }

    private OnWheelChangedListener wheelChangedListener = new OnWheelChangedListener() {
        @Override
        public void onChanged(WheelView wheel, int oldValue, int newValue) {

        }
    };

    private OnWheelScrollListener wheelScrollListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {

        }

        @Override
        public void onScrollingFinished(WheelView wheel) {
            int current = wheel.getCurrentItem();
            if (wheel == wheel_province) {
                if (mProvinceList != null && current >= 0 && current < mProvinceList.size()) {
                    getCity(mProvinceList.get(current));
                }
            } else if (wheel == wheel_city) {
                if (mCityList != null && current >= 0 && current < mCityList.size()) {
                    getCounty(mCityList.get(current));
                }
            } else if (wheel == wheel_area) {
                onCompleteOperation();
            }
        }
    };
}
