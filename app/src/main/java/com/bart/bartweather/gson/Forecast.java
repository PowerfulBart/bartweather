package com.bart.bartweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/3/17.
 */


    //daily_forecast中包含的是一个数组，数组中的每一项都代表着未来一天的天气信息，针对这种情况，
    //我们只需要定义出单日天气的实体类就可以了，然后在声明实体类引用的时候使用集合类型来进行声明

public class Forecast {

    public String date;

    @SerializedName("cond")
    public More mMore;

    public class More{
        @SerializedName("txt_d")
        public String info;
    }

    @SerializedName("tmp")
    public Temperature mTemperature;

    public class Temperature{
        public String max;
        public String min;
    }
}
