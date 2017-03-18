package com.bart.bartweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/3/17.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More mMore;

    public class More{
        @SerializedName("txt")
        public String info;
    }
}
