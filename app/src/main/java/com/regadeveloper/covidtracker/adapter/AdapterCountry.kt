package com.regadeveloper.covidtracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.regadeveloper.covidtracker.R
import com.regadeveloper.covidtracker.model.CountriesItem
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.list_country.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class AdapterCountry(private val country: ArrayList<CountriesItem>, private val clickListener: (CountriesItem) -> Unit) :
    RecyclerView.Adapter<CountryViewHolder>(), Filterable {

    var countryfirstlist = ArrayList<CountriesItem>()
    init {
        countryfirstlist = country
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_country, parent, false)
        return CountryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return countryfirstlist.size
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(countryfirstlist[position], clickListener)
    }

    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val  charSearch = constraint.toString()
                countryfirstlist = if (charSearch.isEmpty()){
                    country
                } else {
                    val resultList = ArrayList<CountriesItem>()
                    for (row in country){
                        val search = row.country?.toLowerCase(Locale.ROOT) ?: ""
                        if (search.contains(charSearch.toLowerCase(Locale.ROOT))){
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResult = FilterResults()
                filterResult.values = countryfirstlist
                return filterResult
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                countryfirstlist = results?.values as  ArrayList<CountriesItem>
                notifyDataSetChanged()
            }
        }
    }
}

class CountryViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
    fun bind(negara : CountriesItem, clickListener: (CountriesItem) -> Unit){
        val country : TextView = itemView.tv_countryName
        val country_Flag : CircleImageView = itemView.img_flag_circle
        val country_totalCase : TextView = itemView.tv_countryTotalCases
        val country_totalRecovered : TextView = itemView.tv_countryTotalRecovered
        val country_totalDeath : TextView = itemView.tv_countryTotalDeath

        val formatter: NumberFormat = DecimalFormat("#,###")

        country.tv_countryName.tv_countryName.text = negara.country
        country_totalCase.tv_countryTotalCases.text = formatter.format(negara.totalConfirmed?.toDouble())
        country_totalRecovered.tv_countryTotalRecovered.text = formatter.format(negara.totalRecovered?.toDouble())
        country_totalDeath.tv_countryTotalDeath.text = formatter.format(negara.totalDeaths?.toDouble())

        Glide.with(itemView).load("https://www.countryflags.io/" + negara.countryCode + "/flat/64.png")
            .into(country_Flag)

        country.setOnClickListener { clickListener(negara) }
        country_Flag.setOnClickListener { clickListener(negara) }
        country_totalCase.setOnClickListener { clickListener(negara) }
        country_totalDeath.setOnClickListener { clickListener(negara) }
        country_totalRecovered.setOnClickListener { clickListener(negara) }
    }

}