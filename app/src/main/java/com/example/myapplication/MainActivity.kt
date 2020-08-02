package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import android.widget.Toast
import com.example.myapplication.API.RetrofitClient
import com.example.myapplication.API.SharedPrefManager
import com.example.myapplication.API.loginClient
import com.example.myapplication.Models.DefaultResponse
import com.example.myapplication.Models.loginData
import com.example.myapplication.Models.loginResponse
import kotlinx.coroutines.delay
import okhttp3.ResponseBody


lateinit var username: EditText
lateinit var password: EditText


/**
 * MainActivity Class
 *
 * This activity is used to login the user.
 * The username and password fields from the activity_main.xml are extracted and sent to the
 * django api. This class can also access the register user page.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        username = findViewById(R.id.usernameText)
        password = findViewById(R.id.editTextPassword)

        icon.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=ykwqXuMPsoc"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.google.android.youtube")
            startActivity(intent)
        }

        registerPage.setOnClickListener {
            println("Before val intent")
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            println("in register page listener")
        }

        btn_login.setOnClickListener {
            userLogin()
        }
    }

    /**
     * Function to log the user in.
     * Firstly the username and password fields are extracted and stored as strings.
     * Then, validation checks are performed on the fields to ensure they are in the correct
     * format to be sent to the api. This ensures that error checking is done on the client
     * side thus, reducing the risk of sending erroneous data to the server, only to receive
     * a failure.
     *
     * If the server sends back a successful response code, we know that we have been logged into
     * the server. The server will also send a json response containing the authentication token
     * and the users id. These will be stored into a cache of memory for the current logged in user
     * and will be required to perform api calls whilst we are logged in.
     * If the server informs us that we have logged in then the OCR activity page is loaded.
     *
     * Otherwise, we display a token error message.
     */
    private fun userLogin() {

        var loggedIn: Boolean

        val username = usernameText.text.toString()
        val password = editTextPassword.text.toString().trim()

        if (username.isEmpty()) {
            usernameText.error = "Username required"
            usernameText.requestFocus()

        }

        if (password.isEmpty()) {
            editTextPassword.error = "Password is empty"
            editTextPassword.requestFocus()

        }
        if (password.length < 5) {
            editTextPassword.error = "Password length is insufficient"
            editTextPassword.requestFocus()

        } else {

            val intent = Intent(this, OCRActivity::class.java)
            loginClient.RetrofitClient.instance.loginUser(loginData(username, password))
                .enqueue(object : Callback<loginResponse> {
                    override fun onFailure(call: Call<loginResponse>, t: Throwable) {
                        Toast.makeText(applicationContext, "Failure", Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(
                        call: Call<loginResponse>,
                        response: Response<loginResponse>
                    ) {
                        if (response.code() == 200) {
                            if (response.body() != null) {
                                val loginResponse = response.body()

                                loggedIn = true

                                Toast.makeText(
                                    applicationContext,
                                    R.string.loginSuccess,
                                    Toast.LENGTH_SHORT
                                ).show()

                                println("Token generated: " + loginResponse!!.token)

                                if (loggedIn) {
                                    startActivity(intent)
                                    usernameText.setText("")
                                    editTextPassword.setText("")
                                }

                                SharedPrefManager.getInstance(applicationContext).saveAuthToken(
                                    loginResponse.token
                                )
                                SharedPrefManager.getInstance(applicationContext).saveID(
                                    loginResponse.id
                                )
                            }

                        } else {
                            Toast.makeText(
                                applicationContext, "Error logging in", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

            })
    }

    }}











