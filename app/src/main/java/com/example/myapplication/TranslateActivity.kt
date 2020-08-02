package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.StrictMode
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.API.RetrofitClient
import com.example.myapplication.API.SharedPrefManager
import com.example.myapplication.Models.queryData
import com.example.myapplication.Models.queryResponse
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.neovisionaries.i18n.LanguageCode
import kotlinx.android.synthetic.main.activity_translate.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*


class TranslateActivity : AppCompatActivity() {

    private var translate: Translate? = null
    lateinit var input: TextView
    lateinit var langDetected: TextView
    lateinit var postQuery: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)
        input = findViewById(R.id.inputToTranslate)
        langDetected = findViewById(R.id.langDetected)
        postQuery = findViewById(R.id.postQuery)



        inputToTranslate.movementMethod = ScrollingMovementMethod()

        val translate: String? = intent.getStringExtra("translate")
        input.text = translate

        if (translate != null) {
            detectLang(translate)
        }

        /**
         *  Set the language codes to an ISO-639-1 format to then be used in the
         *  default Locale() system class to convert to display language
         */
        val languageCodes = arrayListOf("en", "fr", "es", "it", "de", "pt", "nl", "pl", "fi", "bg", "hu",
            "ar", "fa", "id", "hi", "ja", "ru", "sv", "tr", "th", "vi")

        val fullLanguageText = arrayListOf<String>()

        for(lan in languageCodes) {
            fullLanguageText.add(Locale(lan).displayLanguage)
        }


        /**
         * Populate spinner field with languages converted from ISO-639-1 to display language.
         * Also set the text colour of the spinner to white
         */
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, fullLanguageText)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        language_selector.adapter = adapter


        language_selector.background.setColorFilter(resources.getColor(R.color.login_form_details), PorterDuff.Mode.SRC_ATOP)

        language_selector.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                position: Int, id: Long) {
                (parent.getChildAt(0) as TextView).setTextColor(Color.WHITE)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        postQuery.setOnClickListener {
            postData()
        }


        /**
         * Logic for the translate button, once clicked we call the translate() function
         * to open up a new thread for a network call
         */

        translateButton.setOnClickListener {

            val chosenLanguage = LanguageCode.findByName(language_selector.selectedItem.toString())[0].name

            if(chosenLanguage.isEmpty()) {
                language_selector.prompt = "Field is empty"
                language_selector.requestFocus()
                return@setOnClickListener
            }

            if (checkInternetConnection()) {
                //If there is internet connection, get translate service and start translation:
                getTranslateService()
                translate(chosenLanguage)

            } else {

                //If not, display "no connection" warning:
                translatedTv!!.text = resources.getString(R.string.no_connection)
            }
        }
    }

    /**
     * The detectLang() function calls the google detect language api with the text retrived from the
     * firebase API
     */

    @SuppressLint("SetTextI18n")
    private fun detectLang(output: String){
        val languageIdentifier = FirebaseNaturalLanguage.getInstance().languageIdentification

        println(output)
        languageIdentifier.identifyLanguage(output)
            .addOnSuccessListener { lang ->
                if (lang !== "und") {
                    val displayLanguage = Locale(lang).displayLanguage
                    langDetected.text = "Language detected = $displayLanguage"
                    println("Language = $displayLanguage")
                } else {
                    Toast.makeText(this, "Can't Detect Language", Toast.LENGTH_LONG).show()

                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    /**
     * Passing API token for the translate API
     */
    private fun getTranslateService() {

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            resources.openRawResource(R.raw.narhwal2ddb1459490f).use { `is` ->
                val myCredentials = GoogleCredentials.fromStream(`is`)
                val translateOptions =
                    TranslateOptions.newBuilder().setCredentials(myCredentials).build()
                translate = translateOptions.service
            }
        } catch (ioe: IOException) {
            ioe.printStackTrace()

        }

    }

    private fun translate(language : String) {
        //Get input text to be translated:
        val originalText: String = inputToTranslate!!.text.toString()
        val translation = translate!!.translate(
            originalText,
            Translate.TranslateOption.targetLanguage(language),
            Translate.TranslateOption.model("base")
        )

        //Translated text and original text are set to TextViews:
        translatedTv!!.text = translation.translatedText

    }


    private fun checkInternetConnection(): Boolean {

        //Check internet connection:
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        //Means that we are connected to a network (mobile or wi-fi)
        return activeNetwork?.isConnected == true

    }


    /**
     * This posts the data recieved from the translate API, the initial text, chosen langauge and the
     * outputted text is posted to the django API
     *
     * The stored token is sent with the request ot make it authenticated
     */
    private fun postData(){
        println("Post Data method")
        val initialText = translatedTv.text.toString().trim()
        val language = language_selector.selectedItem.toString()
        val translatedText = inputToTranslate.text.toString().trim()


        var token = ("Token "+ SharedPrefManager.getInstance(applicationContext).fetchAuthToken())
        println(token)

        RetrofitClient.getInstanceToken(token)?.api?.queries(queryData(translatedText, language, initialText)
        )
            ?.enqueue(object: Callback<queryResponse> {
                override fun onFailure(call: Call<queryResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                    println("No response from server")
                }

                override fun onResponse(call: Call<queryResponse>, response: Response<queryResponse>
                ) {
                    println("got response ")
                    if (response.code() == 201) {
                        println("respomnse code is 201")
                        if (response.body() != null) {
                            println("sending translation")
                            val intent = Intent(this@TranslateActivity, OCRActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(applicationContext, "success", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            applicationContext, "Error", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            })
    }

    }
