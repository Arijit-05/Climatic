package com.royz.weather

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatDelegate
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.util.Calendar


class WeatherActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var textViewCity: TextView
    private lateinit var textViewTemp: TextView
    private lateinit var feelsLikeText: TextView
    private lateinit var getWeatherBtn: FloatingActionButton
    private lateinit var weatherImageView: ImageView
    private lateinit var windSpeedTxt: TextView
    private lateinit var humidityTxt: TextView
    private lateinit var pressureTxt: TextView
    private lateinit var visibilityTxt: TextView
    private lateinit var searchFab: FloatingActionButton
    private lateinit var citySearchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var relativeLayout: RelativeLayout
    private lateinit var descText: TextView
    private val apiKey = "c94bc25e208769e0ab136e7ba146a1cd"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weather)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        textViewCity = findViewById(R.id.city_name)
        textViewTemp = findViewById(R.id.weather_degree)
        getWeatherBtn = findViewById(R.id.temp_btn)
        feelsLikeText = findViewById(R.id.feels_like_txt)
        weatherImageView = findViewById(R.id.weather_sun)
        windSpeedTxt = findViewById(R.id.wind_speed)
        pressureTxt = findViewById(R.id.pressure_val)
        humidityTxt = findViewById(R.id.humidity_txt)
        visibilityTxt = findViewById(R.id.visibility)
        descText = findViewById(R.id.desc_txt)
        searchFab = findViewById(R.id.search_fab)
        citySearchInput = findViewById(R.id.city_search_input)
        searchButton = findViewById(R.id.search_button)
        relativeLayout = findViewById(R.id.main)

        getWeatherBtn.setOnClickListener {
            fetchLocationAndWeather()
            updateWeatherIcon()
        }

        searchFab.setOnClickListener {
            toggleSearchBar()
        }

        searchButton.setOnClickListener {
            val cityName = citySearchInput.text.toString()
            if (cityName.isNotEmpty()) {
                fetchWeatherDataByCityName(cityName)
            } else {
                Toast.makeText(this, "Enter a valid city name", Toast.LENGTH_SHORT). show()
            }
        }

        relativeLayout.setOnClickListener {
            hideSearchBar()
        }
    }

    private fun updateWeatherIcon() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour in 6..17) {
            weatherImageView.setImageResource(R.drawable.sun)
        } else {
            weatherImageView.setImageResource(R.drawable.moon)
        }
    }

    private fun fetchLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                fetchWeatherData(latitude, longitude)
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey&units=metric"
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val jsonObject = JSONObject(response)
                val cityName = jsonObject.getString("name")
                val main = jsonObject.getJSONObject("main")
                val temperature = main.getDouble("temp")
                val feelsLike = main.getDouble("feels_like")
                val pressure = main.getDouble("pressure")
                val humidity = main.getDouble("humidity")
                val wind = jsonObject.getJSONObject("wind")
                val visibility = jsonObject.getDouble("visibility")
                val windSpeed = wind.getDouble("speed")
                val weatherArray = jsonObject.getJSONArray("weather")
                val weatherDescription = weatherArray.getJSONObject(0).getString("description")

                textViewCity.text = cityName
                textViewTemp.text = "${temperature.toInt()}°"
                feelsLikeText.text = "Feels like ${feelsLike.toInt()}°C"
                pressureTxt.text = "${pressure.toInt()} hPa"
                humidityTxt.text = "${humidity.toInt()} %"
                windSpeedTxt.text = "${windSpeed.toInt()} km/h"
                visibilityTxt.text = "${(visibility/1000).toInt()} km"
                descText.text = weatherDescription
            },
            { error ->
                Toast.makeText(this, "Error fetching weather data", Toast.LENGTH_SHORT).show()
            }
        )
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                fetchLocationAndWeather()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchWeatherDataByCityName(cityName: String) {
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey&units=metric"
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val jsonObject = JSONObject(response)
                val city = jsonObject.getString("name")
                val main = jsonObject.getJSONObject("main")
                val temperature = main.getDouble("temp")
                val feelsLike = main.getDouble("feels_like")
                val pressure = main.getDouble("pressure")
                val humidity = main.getDouble("humidity")
                val wind = jsonObject.getJSONObject("wind")
                val visibility = jsonObject.getDouble("visibility")
                val windSpeed = wind.getDouble("speed")
                val weatherArray = jsonObject.getJSONArray("weather")
                val weatherDescription = weatherArray.getJSONObject(0).getString("description")

                textViewCity.text = city
                textViewTemp.text = "${temperature.toInt()}°"
                feelsLikeText.text = "Feels like ${feelsLike.toInt()}°C"
                pressureTxt.text = "${pressure.toInt()} hPa"
                humidityTxt.text = "${humidity.toInt()} %"
                windSpeedTxt.text = "${windSpeed.toInt()} km/h"
                visibilityTxt.text = "${(visibility / 1000).toInt()} km"
                descText.text = weatherDescription

                hideSearchBar()
            },
            { error ->
                Toast.makeText(this, "Invalid city name", Toast.LENGTH_SHORT).show()
            }
        )
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)
    }

    private fun toggleSearchBar() {
        if (citySearchInput.visibility == View.GONE) {
            citySearchInput.visibility = View.VISIBLE
            searchButton.visibility = View.VISIBLE

            // Animating the search bar
            citySearchInput.requestFocus()
            citySearchInput.alpha = 0f
            searchButton.alpha = 0f
            citySearchInput.animate().alpha(1f).setDuration(100).start()
            searchButton.animate().alpha(1f).setDuration(100).start()

            //Showing keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(citySearchInput, InputMethodManager.SHOW_IMPLICIT)
        } else {
            hideSearchBar()
        }
    }

    private fun hideSearchBar() {
        citySearchInput.visibility = View.GONE
        searchButton.visibility = View.GONE
        citySearchInput.text.clear()

        // Animate the search bar disappearing
        citySearchInput.animate().alpha(0f).setDuration(300).withEndAction {
            citySearchInput.visibility = View.GONE
        }.start()
        searchButton.animate().alpha(0f).setDuration(300).withEndAction {
            searchButton.visibility = View.GONE
        }.start()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(citySearchInput.windowToken, 0)
    }
}

