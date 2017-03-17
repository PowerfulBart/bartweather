package com.bart.bartweather.util;

import android.text.TextUtils;

import com.bart.bartweather.db.City;
import com.bart.bartweather.db.County;
import com.bart.bartweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/16.
 * deal with data like JSON
 */


public class Utility {
    /*
    解析处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response) throws JSONException {
        if (!TextUtils.isEmpty(response)){
            JSONArray allProvince = new JSONArray(response);
            for (int i = 0; i < allProvince.length(); i++){
                JSONObject provinceObjects = allProvince.getJSONObject(i);
                Province province = new Province();
                province.setProvinceName(provinceObjects.getString("name"));
                province.setProvinceCode(provinceObjects.getInt("id"));
                province.save();
            }
            return true;
        }
        return false;
    }

    /*
    解析处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response,int provinceId) throws JSONException {
        if (!TextUtils.isEmpty(response)){
            JSONArray allCity = new JSONArray(response);
            for (int i = 0 ; i < allCity.length(); i++){
                JSONObject cityObjects = allCity.getJSONObject(i);
                City city = new City();
                city.setCityName(cityObjects.getString("name"));
                city.setCityCode(cityObjects.getInt("id"));
                city.setProvinceId(provinceId);
                city.save();
            }
            return true;
        }
        return false;
    }

    /*
    解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response,int cityId) throws JSONException {
        if (!TextUtils.isEmpty(response)){
            JSONArray allCounty = new JSONArray(response);
            for (int i = 0 ; i < allCounty.length(); i++){
                County county = new County();
                JSONObject countyObjects = allCounty.getJSONObject(i);
                county.setCityId(cityId);
                county.setCountyName("name");
                county.setWeatherId("id");
                county.save();
            }
            return true;
        }
        return false;
    }
}
