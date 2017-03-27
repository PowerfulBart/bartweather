package com.bart.bartweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/3/17.
 */

public class Basic {

    //由于JSON中的一些字段可能不太适合直接作为Java字段来命名
    // 使用@SerializedName注解的方式来让 JSON字段和Java字段之间建立映射关系
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
