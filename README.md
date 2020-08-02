# Android Translate Application


## About

This application allows users to take a photo of text and translate it into a different language. They can save these translated texts into their account which is handled via a Django API. This API is also used to
validate and handle queries made by the user. Once a user registers an account and logs in they will be issued a unique session token which communicates with the server to allow users to post data on their specific 
account. When data has been posted, a user can view a graph of the languages that were posted and their occurence from the home page of the app. Users can also see a list of texts they have posted from their individual account and delete any of these texts.

## Build

The django API is hosted onto pythonanywhere.com so that the app can be accessible without having to run the server on the computer via localhost, though the code for the server is 
still present in this repository for reference.

To build the application git clone this repository and ensure that the local.properties folder contains the correct path to your local install of Kotlin.
Run the project in Android Studio and it should build, it may requre that the app is cleaned and rebuilt before it runs


If there are still errors then be sure to click `file -> Invalidate Caches / Restart... -> Invalidate and Restart` on the Android Studio IDE

### Working Demo
 
 <img src="Documentation/AppDemo.gif" width="390" height="838" />

## Wiki

Check the wiki option on the side view of the project overview for information on each page.
