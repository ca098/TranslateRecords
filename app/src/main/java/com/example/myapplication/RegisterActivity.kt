package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.API.RetrofitClient
import com.example.myapplication.API.SharedPrefManager
import com.example.myapplication.Models.DefaultResponse
import com.example.myapplication.Models.createUserData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.register_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * RegisterActivity class
 *
 * This class gets the data from the text fields on the register_user.xml page
 * and sends this data to the django api to register a new account
 * This class validates the fields before the data is sent to the server to ensure
 * that the majority of the error checking is done client side before sending off to the server.
 *
 */
class RegisterActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        println("before on create super")
        super.onCreate(savedInstanceState)
        println("Im here")
        setContentView(R.layout.register_user)


        btn_register.setOnClickListener{


            val email = editTextEmail2.text.toString().trim()
            val username = editUsername.text.toString().trim()
            val password = editTextPassword3.text.toString().trim()
            val password2 = editTextPasswordConfirm.text.toString().trim()

            if (email.isEmpty()) {
                editTextEmail2.error = "Email required"
                editTextEmail2.requestFocus()
            }

            if (password.isEmpty()) {
                editTextPassword.error = "Password required"
                editTextPassword.requestFocus()

            }

            if (username.isEmpty()) {
                editUsername.error = "Username required"
                editUsername.requestFocus()
            }

            if (password != password2) {
                editTextPassword.error = "passwords must match"
                editTextPassword.requestFocus()
            }

            if (password.length < 5) {
                editTextPassword.error = "Password too short"
                editTextPassword.requestFocus()

            }



            RetrofitClient.instance?.api?.createUser(createUserData(email, username, password, password2))
                ?.enqueue(object : Callback<DefaultResponse> {
                    override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                        Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                        println("No response from server")
                    }

                    /**
                     * Here the onResonse function is overeided to retrive the response from the
                     * server. Validation on the response of the server is performed here.
                     * If we get a valid response then we return to the MainActivity page.
                     *
                     * Otherwise, we display a toast with an error message.
                     *
                     */
                    override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>
                    ) {
                        println("Got response ")
                        if (response.code() == 200) {
                            println("Response code is 200")
                            if (response.body() != null) {
                                println("Going to register page")
                                val intent = Intent(this@RegisterActivity, MainActivity::class.java)

                                startActivity(intent)
                                Toast.makeText(applicationContext, "success", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            Toast.makeText(
                                applicationContext, "Error", Toast.LENGTH_SHORT).show()
                        }
                    }

                })
        }
    }
}

