package at.jor.superhero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.jor.superhero.databinding.HomeBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
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

    private lateinit var adBanner: AdView
    private lateinit var bnd: HomeBinding
    private lateinit var categoryListAdapter: CategoryListAdapter

    private var BASE_URL = "http://nagdibai.xyz/wally-api/"
    private val homeCategoryList = ArrayList<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = HomeBinding.inflate(layoutInflater)
        setContentView(bnd.root)
        adsInit()
        setupCategoryList()
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

                    data.appBlob.categories.forEach{
                        homeCategoryList.add(Category(it.name, it.cover, it.wallies))
                    }
                    categoryListAdapter.notifyDataSetChanged()

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

    private fun adsInit() {
        MobileAds.initialize(this) {}
        adBanner = bnd.adBannerHome
        val adRequest = AdRequest.Builder().build()
        adBanner.loadAd(adRequest)
    }

    private fun setupCategoryList() {
        val rvHome: RecyclerView = bnd.rvHome
        rvHome.doOnLayout {
            val imgWidth = it.measuredWidth
            categoryListAdapter = CategoryListAdapter(homeCategoryList, this, imgWidth)
            rvHome.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            rvHome.adapter = categoryListAdapter
        }
    }

}