package uk.co.imoqen.nourish.recipediscovery

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.imoqen.nourish.network.ApiClient
import uk.co.imoqen.nourish.network.LlmError
import uk.co.imoqen.nourish.network.LlmRequest
import uk.co.imoqen.nourish.network.LlmResponse

@Composable
fun RecipeFinderScreen(
) {
    //LoginScreen()
    Column() {
        /*val loggedInUser = Firebase.auth.currentUser!!
        Text("Recipe Finder Screen, hello ${loggedInUser.email}")

        val data = rememberSaveable { mutableStateOf("") }
        val responseError = rememberSaveable { mutableStateOf("") }
        val userInput = rememberSaveable { mutableStateOf("") }

        OutlinedTextField(
            value = userInput.value,
            onValueChange = { newValue: String ->
                userInput.value = newValue
            },
            placeholder = {
                Text("What would you like to cook?")
            },
            modifier = Modifier.fillMaxWidth()
        )
        // will fail if: NO INTERNET PERMISSIONS GRANTED OR POOR INTERNET CONNECTION. need to handle exception, not currently being handled.
        // permission has been added to the android manifest to get internet permission.

        // tutorials followed: https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet#6
        // https://medium.com/@imkuldeepsinghrai/api-calls-with-retrofit-in-android-kotlin-a-comprehensive-guide-e049e19deba9
        Button(
            onClick = {
                if (userInput.value == "") { responseError.value = "Prompt cannot be blank."
                } else {
                    val request =
                        LlmRequest("Test request. simply reply with the following text plus test appended if you received this: ${userInput.value}")
                    val loggedInToken = FirebaseAuth.getInstance().currentUser!!.getIdToken(true)
                        .addOnSuccessListener { tokenResult ->
                            Log.i("true token", "true: ${tokenResult.token}")
                        }
                    val token = loggedInUser.getIdToken(true).addOnSuccessListener { tokenResult ->
                        // if token is obtained, send request with that token
                        val token = tokenResult.token
                        Log.i("token success:", "the token is: $token")
                        if (token != null) {
                            val call = ApiClient.apiService.generateRecipe(request, "Bearer $token")

                            // callback, call, response etc are all retrofit2. not javax. had issue where used javax
                            call.enqueue(object : Callback<LlmResponse> {
                                override fun onResponse(
                                    call: Call<LlmResponse>,
                                    response: Response<LlmResponse>
                                ) {
                                    if (response.isSuccessful) {
                                        val post = response.body()
                                        if (post != null) {
                                            data.value =
                                                post.text // gets the text field from the response
                                            //responseError.value = post.error.toString()
                                        }
                                    } else {
                                        val error = response.errorBody()?.string()
                                        // extracts the error json from the error string using the LlmError template
                                        val errorJson = Gson().fromJson(error, LlmError::class.java)
                                        responseError.value = errorJson.error
                                    }
                                }

                                override fun onFailure(call: Call<LlmResponse>, t: Throwable) {
                                    Log.i("api failure", "a failure occurred")
                                }
                            })
                        }
                    }
                        .addOnFailureListener {
                            // otherwise return error
                            Log.i("error", "error while getting token")
                        }
                }
            }
        ) {
            Text("Click me to make the POST request")
        }

        if (data.value != "") {
            Text(data.value)
        }
        else if (responseError.value != "") {
            Text(responseError.value)
        }*/
    }
}