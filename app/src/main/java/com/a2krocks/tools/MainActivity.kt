package com.a2krocks.tools

import android.Manifest.permission
import android.Manifest.permission.*
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.a2krocks.tools.databinding.ActivityMainBinding
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
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.weatherTextView.visibility = View.GONE
        binding.imageView.visibility = View.GONE

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10f
        ) {
            if (it != null ) { getWeatherDetails(getString(R.string.api), it.latitude, it.longitude) }
        }

        themeManager()

    }

    private fun themeManager() {
        val wallpaperManager: WallpaperManager = WallpaperManager.getInstance(this)
        if (ActivityCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
           requestPermission()
        }
        val isWallpaperDark =     isColorDark(Palette.from(wallpaperManager.drawable.toBitmap()).generate().getDominantColor(Color.WHITE))

        if (isWallpaperDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
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
                binding.weatherTextView.visibility = View.VISIBLE
                binding.imageView.visibility = View.VISIBLE

            }

            override fun onFailure(throwable: Throwable) {
                Log.v("TAG", throwable.message.toString())
            }

        })


    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE), 1)
    }


}