package com.bart.bartweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/3/17.
 */

public class Basic {

    //使用@
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update mUpdate;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
