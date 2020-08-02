package com.example.myapplication.RecyclerView

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.API.RetrofitClient
import com.example.myapplication.API.SharedPrefManager
import com.example.myapplication.Models.queryListResponse
import com.example.myapplication.Models.queryResponse
import com.example.myapplication.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_query_list.*
import retrofit2.Call
import retrofit2.Callback

import retrofit2.Response

/**
 * Class to retrieve all queries for a given user, and send the list to the recycler adapter
 * so the user can then view all of them
 */
class queryList : AppCompatActivity() {

    private lateinit var queryAdapter: RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query_list)
//        setSupportActionBar(toolbar)

        initRecyclerView()
        addDataSet()



    }

    /**
     * Pass in the token for authentication, and the User ID to the URL Path so get the queries for
     * given user ID.
     * Retrieve the JSON array from the server and parse it into separate lists
     * Then create a singular list and send it to recycler view
     */
    private fun addDataSet(){


        var token = ("Token "+ SharedPrefManager.getInstance(applicationContext).fetchAuthToken())
        val id = SharedPrefManager.getInstance(applicationContext).getID()
        if (id != null) {
            RetrofitClient.getInstanceToken(token)?.api?.getQueries(id)?.enqueue(object: Callback<queryListResponse> {
                override fun onFailure(call: Call<queryListResponse>, t: Throwable) {
                    Log.d("query", t.message)
                    System.out.println("Failure")
                }

                override fun onResponse(
                    call: Call<queryListResponse>,
                    response: Response<queryListResponse>
                ) {
                    if(response.code() == 200){
                        if(response.body() != null){

                            val queryListResponse = response.body()!!.queries
//                            val queryList = response.body()!!
                            println("query list response")
                            System.out.println(queryListResponse)
                            val gson = Gson()
                            val queryJson = gson.toJson(queryListResponse)
                            val array = queryListResponse.toTypedArray()

                            val date_created  = ArrayList<String>()
                            val language  = ArrayList<String>()
                            val initialText  = ArrayList<String>()
                            val translatedText  = ArrayList<String>()
                            val id  = ArrayList<String>()
                            val owner  = ArrayList<String>()

                            val list = ArrayList<queryResponse>()


                            for(i in 0 until array.size) {


                                date_created.add(array[i].date_created)
                                language.add(array[i].language)
                                initialText.add(array[i].initialText)
                                translatedText.add(array[i].translatedText)
                                id.add(array[i].id)
                                owner.add(array[i].owner)
                            }

                            for(i in 0 until array.size) {
                                list.add(queryResponse(initialText = initialText[i],
                                    language = language[i],
                                    translatedText = translatedText[i],
                                    date_created = date_created[i],
                                    id = id[i], owner = owner[i]))
                            }
                            println(list)

                            queryAdapter.submitList(list)
                            println("sending list")

                        }
                    } else {
                        System.out.println(response.code())
                        Toast.makeText(
                            applicationContext, "Error", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })

        }
    }


    private  fun initRecyclerView(){
        recycler_view.apply{
            layoutManager = LinearLayoutManager(this@queryList)
            queryAdapter = RecyclerAdapter()
            setBackgroundColor(R.drawable.bg_login)
            adapter = queryAdapter
        }


    }
}
