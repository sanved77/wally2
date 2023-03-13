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

        val call = api.getAllWallpapersWithMeta(getString(R.string.collection))

        call.enqueue(object : Callback<ChitraMeta> {
            override fun onResponse(call: Call<ChitraMeta>, response: Response<ChitraMeta>) {
                if (response.code() == 200) {
                    val data = response.body()!!
                    Log.e("", "")
                    categoryMap["All"] = ArrayList<ChitraItem>()

                    val trojanMode = data.trojanMode
                    val sda = data.sda
                    val subCategories = data.subCategories
                    with (sharedPref.edit()) {
                        putBoolean(TROJAN_MODE, trojanMode)
                        putBoolean(SDA, sda)
                        putBoolean(SUB_CAT_ENABLE, subCategories)
                        commit()
                    }
                    val wallpies = data.wallpies

                    var popularSorted = wallpies.sortedWith(compareBy {it.downloads}).asReversed()
                    for (i in wallpies.indices) {
                        if (i in 0..4)
                            popularItemsList.add(
                                ChitraItem(
                                    popularSorted[i]._id,
                                    popularSorted[i].category,
                                    popularSorted[i].subCategory,
                                    popularSorted[i].downloads,
                                    popularSorted[i].keywords,
                                    popularSorted[i].link
                                )
                            )
                        if (!categoryMap.containsKey(wallpies[i].category))
                            categoryMap[wallpies[i].category] = ArrayList<ChitraItem>()
                        var tempChitra = ChitraItem(
                            wallpies[i]._id,
                            wallpies[i].category,
                            wallpies[i].subCategory,
                            wallpies[i].downloads,
                            wallpies[i].keywords,
                            wallpies[i].link
                        )
                        categoryMap[wallpies[i].category]?.add(tempChitra)
                        categoryMap["All"]?.add(tempChitra)
                    }

                    popularListAdapter.notifyDataSetChanged()
                    bnd.tvLoadingPop.visibility = View.GONE

                    categoryMap.forEach {
//                        Random(System.currentTimeMillis()).nextInt(it.value.size)
                        var randIdx = Random(System.currentTimeMillis()).nextInt(it.value.size)
                        var randCat = it.value[randIdx]
                        val categoryLabel = if(it.key == "All") "All" else randCat.category
                        categoryItemsList.add(
                            ChitraItem(
                                randCat._id,
                                categoryLabel,
                                randCat.subCategory,
                                randCat.downloads,
                                randCat.keywords,
                                randCat.link
                            )
                        )
                    }
                    categoryListAdapter.notifyDataSetChanged()
                    bnd.tvLoadingCat.visibility = View.GONE

                } else {
                    gracefullyFail()
                    Log.e("MainActivity", "API failed 400")
                }
            }
            override fun onFailure(call: Call<ChitraMeta>, t: Throwable) {
                Toast.makeText(this@MainActivity, API_FAILURE_MSG, Toast.LENGTH_SHORT).show()
                gracefullyFail()
            }
        })

    }
}