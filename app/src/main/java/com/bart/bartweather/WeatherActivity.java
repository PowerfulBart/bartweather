package com.bart.bartweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bart.bartweather.gson.Forecast;
import com.bart.bartweather.gson.Weather;
import com.bart.bartweather.util.HttpUtil;
import com.bart.bartweather.util.Utility;
import com.bumptech.glide.Glide;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/*
在活动中请求天气数据，以及将数据展示到界面上
 */
public class WeatherActivity extends AppCompatActivity {

    private ImageView bingPicImg;

    private ScrollView weatherLayout;

    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;
    private TextView pm25Text;

    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    public static final String TAG ="Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //实现背景图和状态栏融合到一起的效果
        //这个功能是Android 5.0 级以上系统才支持的
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化控件
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);

        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);

        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);

        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);

        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash);
        sportText = (TextView) findViewById(R.id.sport_text);
//        Log.d(String.valueOf(WeatherActivity.this), "控件创建完毕: ");

        //尝试从本地缓存中读取天气数据
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        Log.d(String.valueOf(WeatherActivity.this), weatherString+"  我是weatherString");
        if (weatherString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            Log.d(String.valueOf(WeatherActivity.this), weather + "   onCreate: weatherMsg");
            showWeatherInfo(weather);
//            Log.d(String.valueOf(WeatherActivity.this), "onCreate: showWeatherInfo");
        }else{
            //无缓存时去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            Log.d(String.valueOf(WeatherActivity.this), weatherId + "  weatherId在这里 "); //weatherId = null
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);

        }

        //加载背景图片bing_pic
        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
    }

    /*
    根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId){
//        Log.d(String.valueOf(WeatherActivity.this), weatherId + "  requestWeather之weatherId ");
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=7993a8ed0cfc43ba8bbaafd2ba8b6ef8";
        //http://guolin.tech/api/weather?cityid=
        //https://free-api.heweather.com/v5/weather?city=
        Log.d(String.valueOf(WeatherActivity.this), weatherUrl + "requestWeather: weatherUrl");
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        Log.d(String.valueOf(WeatherActivity.this), "获取天气信息失败 onFailed ");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("WeatherActivity", "onResponse: beforeRequestWeather");
                final  String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){
                            Log.d(String.valueOf(WeatherActivity.this), "run: weather != null && \"ok\".equals(weather.status");
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            Log.d(String.valueOf(WeatherActivity.this), "run: apply");
                            showWeatherInfo(weather);
                        }else {
                            Log.d("WeatherActivity", "run: Im here 2");
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                            Log.d(String.valueOf(WeatherActivity.this), "获取天气信息失败:onSuccess ");
                        }
                    }
                });
            }
        });
        loadBingPic();
    }

    /*
    处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather){
        Log.d(TAG, "showWeatherInfo excute");
        String cityName = weather.mBasic.cityName;
        String updateTime = weather.mBasic.mUpdate.updateTime.split(" ")[1]; //？

        String degree = weather.mNow.temperature + "℃";
        String weatherInfo = weather.mNow.mMore.info;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);

        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.mForecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.mMore.info);
            maxText.setText(forecast.mTemperature.max);
            minText.setText(forecast.mTemperature.max);
            forecastLayout.addView(view);
        }
        if (weather.mAQI != null){
            aqiText.setText(weather.mAQI.mAQICity.aqi);
            pm25Text.setText(weather.mAQI.mAQICity.pm25);
        }
        String comfort = "舒适度: " + weather.mSuggestion.mComfort.info;
        String carWash = "洗车指数: " + weather.mSuggestion.mCarWash.info;
        String sport = "运动建议: " + weather.mSuggestion.mSport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    /*
    加载必应每日一图
     */
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);

                    }
                });
            }
        });
    }
}
