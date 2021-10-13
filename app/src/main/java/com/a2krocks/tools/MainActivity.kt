package com.a2krocks.tools

import android.Manifest.permission
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kwabenaberko.openweathermaplib.constant.Units
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper
import com.kwabenaberko.openweathermaplib.implementation.callback.CurrentWeatherCallback
import com.kwabenaberko.openweathermaplib.model.currentweather.CurrentWeather
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {


    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                getWeatherDetails(getString(R.string.api), it.latitude, it.longitude)
            }
        }


    }

    /**
     * @param api Api for OpenWeatherMap
     * @param cityName Name of the city.
     */
    private fun getWeatherDetails(api: String, lat: Double, lon: Double) {
        val helper = OpenWeatherMapHelper(api)
        helper.setUnits(Units.METRIC)
        helper.getCurrentWeatherByGeoCoordinates(lat, lon, object : CurrentWeatherCallback {
            override fun onSuccess(currentWeather: CurrentWeather) {
                Log.v("WeatherApp",
                    "Coordinates: " + currentWeather.getCoord()
                        .getLat() + ", " + currentWeather.getCoord().getLon() + "\n"
                            + "Weather Description: " + currentWeather.getWeather().get(0)
                        .getDescription() + "\n"
                            + "Temperature: " + currentWeather.getMain().getTempMax() + "\n"
                            + "Wind Speed: " + currentWeather.getWind().getSpeed() + "\n"
                            + "City, Country: " + currentWeather.getName() + ", " + currentWeather.getSys()
                        .getCountry()
                )
                val weatherTextView = findViewById<TextView>(R.id.weather_textView)
                weatherTextView.text = currentWeather.main.temp.roundToInt().toString() + getString(
                    R.string.metric_unit)
                Glide.with(applicationContext)
                    .load("https://openweathermap.org/img/wn/" + currentWeather.weather.get(0).icon + "@2x.png")
                    .into(findViewById(R.id.imageView))

            }

            override fun onFailure(throwable: Throwable) {
                Log.v("TAG", throwable.message.toString())
            }

        })


    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(ACCESS_COARSE_LOCATION), 1)
    }


}