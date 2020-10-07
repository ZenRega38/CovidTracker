package com.regadeveloper.covidtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.regadeveloper.covidtracker.adapter.AdapterCountry
import com.regadeveloper.covidtracker.model.CountriesItem
import com.regadeveloper.covidtracker.model.ResponseCountry
import com.regadeveloper.covidtracker.network.ApiService
import com.regadeveloper.covidtracker.network.RetrofitBuilder.retrofit
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {

    private var ascending = true
    companion object{
        lateinit var adapterCountry : AdapterCountry
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapterCountry.filter.filter(newText)
                return false
            }

        })

        swipe_refresh.setOnRefreshListener {
            getCountry()
            swipe_refresh.isRefreshing = false
        }

        initializedView()
        getCountry()
    }

    private fun initializedView() {
        btn_sequence.setOnClickListener {
            sequenceWithoutInternet(ascending)
            ascending = !ascending
        }
    }

    private fun sequenceWithoutInternet(ascending: Boolean) {
        rv_country.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            if (ascending){
                (layoutManager as LinearLayoutManager).reverseLayout = true
                (layoutManager as LinearLayoutManager).stackFromEnd = true
                Toast.makeText(this@MainActivity, "Z - A", Toast.LENGTH_SHORT).show()
            }else{
                (layoutManager as LinearLayoutManager).reverseLayout = false
                (layoutManager as LinearLayoutManager).stackFromEnd = false
                Toast.makeText(this@MainActivity, "A - Z", Toast.LENGTH_SHORT).show()
            }
            adapter = adapter
        }
    }

    private fun getCountry(){
        val api = retrofit.create(ApiService::class.java)
        api.getAllCountry().enqueue(object : Callback<ResponseCountry>{
            override fun onFailure(call: Call<ResponseCountry>, t: Throwable) {
                progress_bar.visibility = View.GONE
            }

            override fun onResponse(call: Call<ResponseCountry>, response: Response<ResponseCountry>)
            {
                if (response.isSuccessful){
                    val  getListDataCovid = response.body()!!.global
                    val formatter : NumberFormat = DecimalFormat("#,###")
                    txt_confirmed_globe.text = formatter.format(getListDataCovid?.totalConfirmed?.toDouble())
                    txt_recovered_globe.text = formatter.format(getListDataCovid?.totalRecovered?.toDouble())
                    txt_death_globe.text = formatter.format(getListDataCovid?.totalDeaths?.toDouble())

                    rv_country.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        progress_bar.visibility = View.GONE
                        adapterCountry = AdapterCountry(
                            response.body()!!.countries as ArrayList<CountriesItem>
                        ){ negara -> itemClicked(negara)}
                        adapter = adapterCountry
                    }
                }else{
                    progress_bar?.visibility = View.GONE
                }
            }

        })
    }

    private fun itemClicked(negara: CountriesItem) {
        val moveWithData = Intent(this@MainActivity, DetailActivity::class.java)
        moveWithData.putExtra(DetailActivity.EXTRA_COUNTRY, negara)
        startActivity(moveWithData)
    }
}