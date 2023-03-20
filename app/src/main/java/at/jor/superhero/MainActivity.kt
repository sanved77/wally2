package at.jor.superhero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import at.jor.superhero.databinding.HomeBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.nagdibai.superwallpapers.WallyApi
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var bnd: HomeBinding
    private var BASE_URL = "http://nagdibai.xyz/wally-api/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = HomeBinding.inflate(layoutInflater)
        val view = bnd.root
        setContentView(R.layout.home)
        grabThemWallpapers()

    }

    private fun grabThemWallpapers() {

        val client = OkHttpClient.Builder()
            .readTimeout(6, TimeUnit.SECONDS)
            .build()
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WallyApi::class.java)

        val call = api.getSaamaan(getString(R.string.app))

        call.enqueue(object : Callback<Saamaan> {
            override fun onResponse(call: Call<Saamaan>, response: Response<Saamaan>) {
                if (response.code() == 200) {
                    val data = response.body()!!
                    Log.e("Dhuski Chandan", "" + data)

                } else {
                    Log.e("MainActivity", "API failed 400")
                }
            }
            override fun onFailure(call: Call<Saamaan>, t: Throwable) {
                Log.e("MainActivity", "Call failed")
                Toast.makeText(this@MainActivity, "Call Fail", Toast.LENGTH_SHORT).show()
            }
        })

    }
}