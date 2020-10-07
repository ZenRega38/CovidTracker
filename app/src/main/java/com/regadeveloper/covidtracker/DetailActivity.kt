package com.regadeveloper.covidtracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.regadeveloper.covidtracker.model.CountriesItem
import com.regadeveloper.covidtracker.model.CountryInfo
import com.regadeveloper.covidtracker.network.ApiService
import com.regadeveloper.covidtracker.network.InfoService
import kotlinx.android.synthetic.main.activity_detail.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class DetailActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_COUNTRY = "EXTRA_COUNTRY"
        lateinit var saveCountryData: String
        lateinit var saveCountryFlag: String
    }

    private val sharedPreFile = "kotlinsharedpreference" //variable file data storage
    private lateinit var sharedPreference: SharedPreferences //for processing data
    private val perDayCases = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        sharedPreference = this.getSharedPreferences(sharedPreFile, Context.MODE_PRIVATE)
        val edit_data : SharedPreferences.Editor = sharedPreference.edit()

        //get data from parcilize from intent
        val data = intent.getParcelableExtra<CountriesItem>(EXTRA_COUNTRY)
        val formatter : NumberFormat = DecimalFormat("#,###")

        data.let {
            txt_countryName.text = data!!.country
            latest_update.text = data.date
            latest_totalCurrentConfirmed.text = formatter.format(data.totalConfirmed?.toDouble())
            latest_totalCurrentRecovered.text = formatter.format(data.totalRecovered?.toDouble())
            latest_totalCurrentDeaths.text = formatter.format(data.totalDeaths?.toDouble())
            latest_newConfirmed.text = formatter.format(data.newConfirmed?.toDouble())
            latest_newRecovered.text = formatter.format(data.newRecovered?.toDouble())
            latest_newDeaths.text = formatter.format(data.newDeaths?.toDouble())

            edit_data.putString(data.country, data.country) //for saving and put data
            edit_data.apply() //for saving data
            edit_data.commit() // for showing data

            val saveData = sharedPreference.getString(data.country, data.country)
            val saveFlag = sharedPreference.getString(data.countryCode, data.countryCode)
            saveCountryData = saveData.toString()
            saveCountryFlag = saveFlag.toString() + "/flat/64.png"

            if (saveFlag != null){

                Glide.with(this)
                    .load("https://www.countryflags.io/$saveCountryFlag")
                    .into(img_countryFlag)
            }else{
                Toast.makeText(this, "Image not Found", Toast.LENGTH_SHORT).show()
            }
            getChart()
        }
    }

    private fun getChart() {
        val okHttp = OkHttpClient().newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.covid19api.com/dayone/country/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(InfoService::class.java)
        api.getInfoService(saveCountryData).enqueue(object : Callback<List<CountryInfo>>{

            override fun onFailure(call: Call<List<CountryInfo>>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Error", Toast.LENGTH_SHORT).show()
            }

            @SuppressLint("SimpleDateFormat")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<CountryInfo>>,
                response: Response<List<CountryInfo>>
            ) {
                val getListDataCovid : List<CountryInfo> = response.body()!!
                if (response.isSuccessful){
                    val barEntries : ArrayList<BarEntry> = ArrayList()
                    val barEntries2 : ArrayList<BarEntry> = ArrayList()
                    val barEntries3 : ArrayList<BarEntry> = ArrayList()
                    val barEntries4 : ArrayList<BarEntry> = ArrayList()
                    var y = 0

                    while (y < getListDataCovid.size){
                        for (x in getListDataCovid){
                            val barEntry = BarEntry(y.toFloat(), x.Confirmed?.toFloat()?: 0f)
                            val barEntry2 = BarEntry(y.toFloat(), x.Active?.toFloat()?: 0f)
                            val barEntry3 = BarEntry(y.toFloat(), x.Recovered?.toFloat()?: 0f)
                            val barEntry4 = BarEntry(y.toFloat(), x.Deaths?.toFloat()?: 0f)

                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'")
                            val outputFormat = SimpleDateFormat("dd-MM-yyyy")

                            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                            val date: Date? = inputFormat.parse(x.Date!!)
                            val formattedDate: String = outputFormat.format(date!!)
                            perDayCases.add(formattedDate)

                            barEntries.add(barEntry)
                            barEntries2.add(barEntry2)
                            barEntries3.add(barEntry3)
                            barEntries4.add(barEntry4)
                            y++
                        }
                        val xAxis: XAxis = chart_data1.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(perDayCases)
                        chart_data1.axisLeft.axisMinimum = 0f
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.setCenterAxisLabels(true)
                        xAxis.isGranularityEnabled = true
                        val barDataSet = BarDataSet(barEntries, "Confirmed")
                        val barDataSet2 = BarDataSet(barEntries2, "Active")
                        val barDataSet3 = BarDataSet(barEntries3, "Recovered")
                        val barDataSet4 = BarDataSet(barEntries4, "Deaths")
                        //setColorBAr
                        barDataSet.setColors(Color.parseColor("#FFEB3B"))
                        barDataSet2.setColors(Color.parseColor("#2196F3"))
                        barDataSet3.setColors(Color.parseColor("#03DAC5"))
                        barDataSet4.setColors(Color.parseColor("#F44336"))

                        val data = BarData(barDataSet, barDataSet2, barDataSet3, barDataSet4)
                        chart_data1.data = data
                        //ukuran grafik atau bar
                        val barSpace = 0.02f
                        val groupSpace = 0.3f
                        val groupCount = 4f
                        data.barWidth = 0.15f
                        chart_data1.invalidate()
                        chart_data1.setNoDataTextColor(R.color.black)
                        chart_data1.setTouchEnabled(true)
                        chart_data1.description.isEnabled = false
                        chart_data1.xAxis.axisMinimum = 0f
                        chart_data1.setVisibleXRangeMaximum(
                            0f + chart_data1.barData.getGroupWidth(
                                groupSpace,
                                barSpace
                            ) * groupCount
                        )
                        chart_data1.groupBars(0f,groupSpace, barSpace)
                    }
                }
            }
        })
    }
}