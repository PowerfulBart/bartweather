package com.bart.bartweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2017/3/17.
 */

public class Weather {

    public String status;

    public Basic mBasic;

    public AQI mAQI;

    public Now mNow;

    public Suggestion mSuggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> mForecastList;

}
