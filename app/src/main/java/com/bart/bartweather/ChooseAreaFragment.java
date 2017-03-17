package com.bart.bartweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bart.bartweather.db.City;
import com.bart.bartweather.db.County;
import com.bart.bartweather.db.Province;
import com.bart.bartweather.util.HttpUtil;
import com.bart.bartweather.util.Utility;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/16.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog mProgressDialog;
    private Button backButton;
    private TextView titleText;
    private ListView mListView;

    private ArrayAdapter<String> mAdapter;
    private List<String> dataList = new ArrayList<>();

    /*
    省、市、县 列表
     */
    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCountyList;

    /*
    选中的 省 市 县
     */
    private Province selectProvince;
    private City selectCity;
    private County selectCounty;

    /*
    当前选中的级别
     */
    private int currentLevel;

    /*
    onCreateView()获取控件实例
    初始化ArrayAdapter，并将它设置为ListView的适配器
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        backButton = (Button)view.findViewById(R.id.back_button);
        titleText = (TextView)view.findViewById(R.id.title_text);
        mListView = (ListView)view.findViewById(R.id.list_view);
        mAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,dataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    /*
    onActivityCreated()给ListView和Button设置点击事件
     */

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectProvince = mProvinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY){
                    selectCity = mCityList.get(position);
                    queryCounties();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel == LEVEL_PROVINCE){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /*
    查询全国所有的省，优先从数据库查询，如果没有再到服务器上去查询。
     */
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        //调用LitePal的查询接口从数据库中读取省级数据，读到的话直接显示到界面
        if (mProvinceList.size() > 0){
            dataList.clear();
            for (Province province : mProvinceList){
                dataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0); //listview.setselection(position)，表示将列表移动到指定的Position处。
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromService(address,"province");
        }
    }

    /*
    查询全国所有的市，优先从数据库查询，如果没有再到服务器上去查询。
    */
    private void queryCities(){
        titleText.setText(selectProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        //获得选中省下的市的数据
        mCityList = DataSupport.where("provinceid = ?",String.valueOf(selectProvince.getId())).find(City.class);

        if (mCityList.size() > 0){
            dataList.clear();
            for (City city : mCityList){
                dataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int provinceCode = selectProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromService(address,"city");
        }
    }

    /*
    查询全国所有的县，优先从数据库查询，如果没有再到服务器上去查询。
    */
    private void queryCounties(){
        titleText.setText(selectCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        mCountyList = DataSupport.where("cityid = ?",String.valueOf(selectCity.getId())).find(County.class);//选中市

        if (mCountyList.size() > 0){
            dataList.clear();
            for (County county : mCountyList){
                dataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectProvince.getProvinceCode();
            int cityCode = selectCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromService(address,"county");
        }
    }

    /*
    根据传入的地址和类型从服务器上查询省市县数据
     */
    private void queryFromService(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                try {
                    if ("province".equals(type)){
                        result = Utility.handleProvinceResponse(responseText);
                    }else if ("city".equals(type)){
                        result = Utility.handleCityResponse(responseText,selectProvince.getId());
                    }else if ("county".equals(type)){
                        result = Utility.handleCountyResponse(responseText,selectCity.getId());
                    }
                    if (result){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
                                //解析处理完数据后再次调用queryXxx来重新加载省市县数据
                                if ("province".equals(type)){
                                    queryProvinces();
                                }else if ("city".equals(type)){
                                    queryCities();
                                }else if ("county".equals(type)){
                                    queryCounties();
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
    显示进度对话框
     */
    private void showProgressDialog(){
        if (mProgressDialog == null){
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    /*
    关闭进度对话框
     */
    private void closeProgressDialog(){
        if (mProgressDialog != null){
            mProgressDialog.dismiss();
        }
    }
}
