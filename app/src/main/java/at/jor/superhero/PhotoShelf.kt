package at.jor.superhero

import android.app.Activity
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.jor.superhero.databinding.PhotoShelfBinding
import com.google.android.gms.ads.*
import kotlin.collections.ArrayList


class PhotoShelf : AppCompatActivity() {

    private lateinit var bnd: PhotoShelfBinding
    private lateinit var adBanner: AdView
    private lateinit var label: String
    private lateinit var wallies: ArrayList<String>
    private lateinit var photoShelfAdapter: PhotoShelfAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = PhotoShelfBinding.inflate(layoutInflater)
        setContentView(bnd.root)
        adsInit()
        grabStuff()
        setupShelfGrid()
    }

    private fun grabStuff() {
        label = intent.getStringExtra("CategoryLabel")!!
        supportActionBar?.title = label;
        wallies = intent.getSerializableExtra("Wallies") as ArrayList<String>
    }

    private fun setupShelfGrid() {
        val rvShelf: RecyclerView = bnd.rvShelf
        val width: Int = getScreenWidth(this)
        rvShelf.layoutManager = GridLayoutManager(applicationContext, NO_OF_ITEMS_IN_SHELF_GRID)
        photoShelfAdapter = PhotoShelfAdapter(wallies, this@PhotoShelf, (width/NO_OF_ITEMS_IN_SHELF_GRID))
        rvShelf.adapter = photoShelfAdapter
    }

    private fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    private fun adsInit() {
        MobileAds.initialize(this) {}
        adBanner = bnd.adBannerShelf
        val adRequest = AdRequest.Builder().build()
        adBanner.loadAd(adRequest)
    }

}