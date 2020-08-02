package com.example.myapplication

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.example.myapplication.RecyclerView.queryList
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.API.RetrofitClient
import com.example.myapplication.API.SharedPrefManager
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.common.graph.Graph
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_ocr.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Exception
import java.util.*


class OCRActivity : AppCompatActivity() {


    /**
     * Initialising block of global variables
     */

    lateinit var ocrImage: ImageView
    lateinit var resultEditText: EditText
    lateinit var translateButton: Button
    lateinit var image_uri: Uri
    private val IMAGE_PICK_GALLERY_CODE = 1000
    private val IMAGE_PICK_CAMERA_CODE = 1001
    private val CAMERA_REQUEST_CODE = 200
    private val STORAGE_REQUEST_CODE = 400
    var PERMISSION_REQ_CODE: Int = 100
    var isPermissionsGranted: Boolean = false
    var permissions: Array<String> = arrayOf(WRITE_EXTERNAL_STORAGE, CAMERA)
    lateinit var logoutButton: Button

    lateinit var uriFinale: Uri

    private var cameraPermission: Array<String> = arrayOf(WRITE_EXTERNAL_STORAGE, CAMERA)
    private var storagePermission: Array<String> = arrayOf(WRITE_EXTERNAL_STORAGE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr)

        resultEditText = findViewById(R.id.ocrResult)
        ocrImage = findViewById(R.id.ocrImageView)
        translateButton = findViewById(R.id.goToTranslate)


        //set an onclick listener on the button to trigger the @pickImage() method
        selectImageBtn.setOnClickListener {
            imageSelector()
        }

        //set an onclick listener on the button to trigger the @processImage method
        processImageBtn.setOnClickListener {
            processImage(processImageBtn)
        }

        gotograph.setOnClickListener {
           val intent = Intent(this, GraphActivity::class.java)
            startActivity(intent)
        }

        translateButton.setOnClickListener {

            val translate = resultEditText.text.toString().trim()
            val intent = Intent(this, TranslateActivity::class.java)
            intent.putExtra("translate", translate)
            startActivity(intent)
        }

        logoutButton = findViewById(R.id.logoutButton)
        gotorecords.setOnClickListener {
            val intent = Intent(this, queryList::class.java)
            startActivity(intent)
        }
        logoutButton.setOnClickListener {
            logout()
        }
    }












    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                CropImage.activity(data!!.data)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this)
            }
        }
            if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                val result = CropImage.getActivityResult(data)
                if(resultCode == Activity.RESULT_OK)
                    try {

                    val bitmap = BitmapFactory.decodeFile(result.uri.path)
                    ocrImage.setImageBitmap(bitmap)
                } catch(e: Exception){
                        Toast.makeText(this, "Error with CROP", Toast.LENGTH_LONG).show()
                    }
            }
    }



    /**
     *  Function to handle the log out, we send the authentication token for the user
     *  to authenticate that this is the specific user we want to log out
     *
     *  This is called in a separate thread as we are making a network call.
     */
    private fun logout() {
        val token = ("Token "+ SharedPrefManager.getInstance(applicationContext).fetchAuthToken())
        println(token)
        RetrofitClient.getInstanceToken(token)?.api?.logout()

            ?.enqueue(object: Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                    println("No response from server")
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>
                ) {
                    println("got response ")
                    if (response.code() == 200) {
                        println("Respose code is ${response.code()}")
                        if (response.body() != null) {
                            println("sending translation")
                            SharedPrefManager.getInstance(this@OCRActivity).clear()
                            val intent = Intent(this@OCRActivity, MainActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(applicationContext, "Logged out", Toast.LENGTH_SHORT)
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


    /**
     * Calling the firebase vision API to grab the string from the text
     */
    @SuppressLint("SetTextI18n")
    fun processImage(v: View) {
        if (ocrImage.drawable != null) {
            resultEditText.setText("")
            v.isEnabled = false

            val bitmap = (ocrImage.drawable as BitmapDrawable).bitmap
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    v.isEnabled = true
                    processResultText(firebaseVisionText)
                }
                .addOnFailureListener { e ->
                    v.isEnabled = true
                    resultEditText.setText(e.toString())
                }

        } else {
            Toast.makeText(this, "Select an Image First", Toast.LENGTH_LONG).show()
        }

    }


    @SuppressLint("SetTextI18n")
    private fun processResultText(resultText: FirebaseVisionText) {
        if (resultText.textBlocks.size == 0) {
            resultEditText.setText("No Text Found")
            return
        }

        var output = ""
        for (block in resultText.textBlocks) {
            val blockText = block.text
            resultEditText.append(blockText + "\n")
            output += "$blockText "
        }

    }


    /**
     * Start the CropImage library to allow the user to select camera or photos from gallery
     * the @param CropImage.activity opens up this screen and is called once camera permissions
     * have been accepted.
     */
    private fun imageSelector() {
        if (!checkCameraPermission()) {
            requestCameraPermission()
        } else {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)
        }
    }




    fun pickImage() {
        if (!checkStoragePermission()) {
            requestStoragePermission()
        } else {
            println("Image")
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                IMAGE_PICK_GALLERY_CODE
            )
        }
    }

    private fun requestStoragePermission() {

        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE
        )
    }

    /**
     * check if permission to access storage has been granted
     * @return
     */
    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            storagePermission[0]
        ) == PackageManager.PERMISSION_GRANTED


    }

    /**
     * request permission to access phones camera
     */
    private fun requestCameraPermission() {


        ActivityCompat.requestPermissions(
            this,
            cameraPermission, CAMERA_REQUEST_CODE
        )
    }

    /**
     * check permission to use camera has been granted
     * @return
     */
    private fun checkCameraPermission(): Boolean {
        /* check camera permission and return result
        in order to get higg quality image have to save
        image to external storage first
        before inserting to image view
         */
        val result: Boolean = ContextCompat.checkSelfPermission(
            this,
            cameraPermission[0]
        ) == PackageManager.PERMISSION_GRANTED
        val result1: Boolean = ContextCompat.checkSelfPermission(
            this,
            cameraPermission[1]
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }


    /**
     * handling the permissions
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val cameraAccepted = grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED
                val writeStorageAccepted = grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED
                if (cameraAccepted && writeStorageAccepted) {
                    System.out.println("Request Camera")
                    imageSelector()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val writeStorageAccepted = grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED
                if (writeStorageAccepted) {
                    System.out.println("Request Image")
                    pickImage()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}



