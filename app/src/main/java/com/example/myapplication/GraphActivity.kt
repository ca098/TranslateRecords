package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.API.RetrofitClient
import com.example.myapplication.API.SharedPrefManager
import com.example.myapplication.Models.languageResponse
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.activity_graph.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Graph Activity class
 * This activity fetches languages data from the client, parses this data, and uses
 * it to display a bar chart
 */
@Suppress("DEPRECATION")
class GraphActivity : AppCompatActivity() {

    /**
     * This function sets the content view to the activity graph xml file, then
     * calls the getLanguages function
     */
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        getLanguages()

    }

    /**
     * This function sends a get request to the api to retrieve a jsonArray of all languages
     * in the database and the amount of times each language appears. for example: if 'English'
     * occurs in the database 3 times and 'Spanish' occurs in the database 2 times we get a response
     * of: [{English: 3},{Spanish: 2}]
     *
     * This data is then parsed into two seperate lists. The first list stores the string values
     * of all of the languages, and the second list stores the numerical occurence values.
     *
     * These two lists are then sent to the setBarChart function as parameters.
     */
    private fun getLanguages(){
        val token = ("Token "+ SharedPrefManager.getInstance(applicationContext).fetchAuthToken())
        RetrofitClient.getInstanceToken(token)?.api?.getQueries()?.enqueue(object: Callback<languageResponse>{
            override fun onFailure(call: Call<languageResponse>, t: Throwable) {
                println(t.message)
            }

            override fun onResponse(
                call: Call<languageResponse>,
                response: Response<languageResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()

                    val arrayOfOccurrence = mutableListOf<Int>()
                    val arrayOfLanguages = mutableListOf<String>()

                    if (body != null) {
                        for(i in 0 until body.listoflangs.size()) {
                            val format: String = body.listoflangs[i].toString()
                                .replace("{\"","")
                                .replace("\":"," ")
                                .replace("}","")

                            val language = format.replace("[^A-Za-z]".toRegex(), "")
                            arrayOfLanguages.add(language)

                            val num = format.replace("[^0-9]".toRegex(), "")
                            arrayOfOccurrence.add(num.toInt())
                        }

                        for(i in 0 until arrayOfLanguages.size) {
                            println(arrayOfLanguages[i])
                            println(arrayOfOccurrence[i])
                        }

                        setBarChart(arrayOfOccurrence, arrayOfLanguages)
                    }

               }    else {
                    println("Response not successful, code is: " + response.code())
                  }
            }
        })
    }


    /**
     * @param numberList - this is a list of the amount of times each language has been
     *                     posted to the server
     * @param languageList - this is a list of the languages that have been posted to the server
     *
     * The index values of each list correspond to each other, for instance, if 'English' has
     * had 5 occurences in the server, and is at index 1 in  languagelist, index 1 in numberlist
     * will be 5.
     *
     * This function then sets a bar chart of each language and its occurence value
     */
    private fun setBarChart(numberList : List<Int>, languageList : List<String>) {
        val entries = ArrayList<BarEntry>()

        var count = 1
        for(element in numberList) {
            println(element)
            entries.add(BarEntry(count.toFloat(), element.toFloat()))
            count++
        }

        val barDataSet = BarDataSet(entries, " ")
        val xAxis: XAxis = bargraph.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE

        val formatter: ValueFormatter =
            object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return languageList[value.toInt() - 1]
                }
            }

        xAxis.granularity = 1f
        xAxis.valueFormatter = formatter

        val data = BarData(barDataSet)
        bargraph.data = data // set the data and list of lables into chart
        barDataSet.color = resources.getColor(R.color.colorPrimary)
        bargraph.setFitBars(true)
        bargraph.description.text = " "
//        bargraph.axisLeft.textColor = R.color.login_form_details; // left y-axis
//        bargraph.xAxis.textColor = R.color.login_form_details;
//        bargraph.legend.textColor = R.color.login_form_details;
        bargraph.animateY(3000)
    }

}



