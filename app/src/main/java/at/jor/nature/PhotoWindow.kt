package at.jor.superhero

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import at.jor.superhero.databinding.PhotoWindowSlideBinding
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.yalantis.ucrop.UCrop
import java.io.*
import java.util.*
import kotlin.concurrent.schedule

class PhotoWindow : AppCompatActivity() {

    private lateinit var bnd: PhotoWindowSlideBinding

    private lateinit var mAdView : AdView
    private var startIdx = 0
    private lateinit var itemsList: ArrayList<String>

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var TAG = "PhotoWindow"

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var options: UCrop.Options

    private lateinit var viewPager: ViewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = PhotoWindowSlideBinding.inflate(layoutInflater)
        setContentView(bnd.root)

        firebaseInit()
        initAdsAndBanner()
        setViewPager()
        setButtons()
        loadFullPageAd()
        setActivityCallback()
        setUpUCrop()
    }

    private fun firebaseInit() {
        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "PhotoWindow")
        }
    }

    private fun initAdsAndBanner() {
        MobileAds.initialize(this) {}

        mAdView = bnd.bannerAdPhotoWindow
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun setViewPager () {
        itemsList = intent.getSerializableExtra("ItemsList") as ArrayList<String>
        startIdx = intent.getIntExtra("StartIdx", 0)
        viewPager = bnd.vpPhoto
        viewPagerAdapter = ViewPagerAdapter(itemsList, this@PhotoWindow)
        viewPager.adapter = viewPagerAdapter
        viewPagerAdapter.notifyDataSetChanged()
        viewPager.currentItem = startIdx
    }

    private fun setButtons() {

        bnd.bDownload.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                downloadPhoto()
            } else {
                checkPermissionAndDownloadBitmap()
            }
        }

        bnd.bApply.setOnClickListener {
            bnd.frameProgress.visibility = View.VISIBLE
            bnd.loadingCircle.visibility = View.VISIBLE
            Timer().schedule(100){
                cropAndApply()
            }
        }
    }

    private fun cropAndApply() {
        val currViewPage: ImageView = bnd.vpPhoto.findViewWithTag("viewPagerIvPhoto" + viewPager.currentItem)
        val bitmap = (currViewPage.drawable as BitmapDrawable).bitmap
        val cacheDir = baseContext.cacheDir
        val f = File(cacheDir, "pic")

        try {
            val out = FileOutputStream(f)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            Log.d(TAG, "File saved")
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not in cache ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "IO Error ${e.message}")
        }
        var sourceUri = Uri.fromFile(File(cacheDir, "pic"))
        var fauxDestination = Uri.fromFile(File(cacheDir, "1"))

        var width = ScreenMetrics.getScreenSize(this).width
        var height = ScreenMetrics.getScreenSize(this).height


        val cropper = UCrop.of(sourceUri, fauxDestination)
            .withAspectRatio(width.toFloat(), height.toFloat())
            .withMaxResultSize(width, height)
            .getIntent(this)

        firebaseAnalytics.logEvent("Apply") {
            param("Url", itemsList[viewPager.currentItem])
        }

        resultLauncher.launch(cropper)
    }

    private fun setActivityCallback() {
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    downloadPhoto()
                    Log.d(TAG, "Permission granted")
                } else {
                    val snack = Snackbar.make(bnd.clPhoto, "Need permission to download to Gallery", Snackbar.LENGTH_INDEFINITE)
                    snack.setAction("Retry", View.OnClickListener {
                        // TODO: Add analytics
                        checkPermissionAndDownloadBitmap()
                    })
                    snack.setActionTextColor(Color.YELLOW)
                    snack.anchorView = bnd.bannerAdPhotoWindow
                    snack.show()
                }
            }

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            bnd.frameProgress.visibility = View.INVISIBLE
            bnd.loadingCircle.visibility = View.INVISIBLE
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                var resultUri: Uri? = data?.let { UCrop.getOutput(it) };
                bnd.frameProgress.visibility = View.INVISIBLE
                bnd.loadingCircle.visibility = View.INVISIBLE
                if (resultUri != null) {
                    applyWallpaper(resultUri)
                } else {
                    Toast.makeText(this, "Error loading data for crop tool", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermissionAndDownloadBitmap() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                downloadPhoto()
                Log.d(TAG, "Permission granted")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                showDialog(
                    "Storage Permission",
                    "Select 'Allow' to give permission to store downloaded photos",
                    "OK"
                ) {
                    // TODO: Add analytics
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun saveMediaToStorage() {
        val currViewPage: ImageView = bnd.vpPhoto.findViewWithTag("viewPagerIvPhoto" + viewPager.currentItem)
        val bitmap = (currViewPage.drawable as BitmapDrawable).bitmap
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            val snack = Snackbar.make(bnd.clPhoto, "Wallpaper downloaded to your Gallery", Snackbar.LENGTH_INDEFINITE)
            snack.setAction("OK", View.OnClickListener {
                val manager = ReviewManagerFactory.create(this@PhotoWindow)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        val flow = manager.launchReviewFlow(this@PhotoWindow, reviewInfo)
                    } else {
                        Log.e(TAG, "Review window not present")
                    }
                }
                Log.d(TAG, "Download complete dialog dismissed")
            })

            snack.setTextColor(Color.BLACK)
            snack.setActionTextColor(Color.BLACK)
            snack.setBackgroundTint(Color.rgb(42,202,234))
            snack.anchorView = bnd.bannerAdPhotoWindow
            snack.show()
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@PhotoWindow)
            }
            firebaseAnalytics.logEvent("Download") {
                param("Url", itemsList[viewPager.currentItem])
            }
        }
    }

    private fun applyWallpaper(imageUri: Uri) {
        var bitmap: Bitmap? = null
        if (Build.VERSION.SDK_INT >= 29) {
            val source: ImageDecoder.Source =
                ImageDecoder.createSource(applicationContext.contentResolver, imageUri)
            try {
                bitmap = ImageDecoder.decodeBitmap(source)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            try {
                bitmap =
                    MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, imageUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val wallpaperManager = WallpaperManager.getInstance(baseContext)
        if (bitmap != null) {
            wallpaperManager.setBitmap(bitmap)
//            Toast.makeText(this, "Wallpaper set!", Toast.LENGTH_SHORT).show()
            val snack = Snackbar.make(bnd.clPhoto, "Wallpaper set", Snackbar.LENGTH_INDEFINITE)
            snack.setAction("OK", View.OnClickListener {
                // TODO: Add analytics
            })
            snack.setTextColor(Color.BLACK)
            snack.setActionTextColor(Color.BLACK)
            snack.setBackgroundTint(Color.rgb(42,202,234))
            snack.anchorView = bnd.bannerAdPhotoWindow
            snack.show()
            if(mInterstitialAd != null) {
                mInterstitialAd?.show(this)
            }
        } else {
            Log.e(TAG, "Error loading wallpaper manager")
            Toast.makeText(this, "Error loading wallpaper manager", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadPhoto() {
        saveMediaToStorage()
    }

    private fun loadFullPageAd() {
        var adRequest = AdRequest.Builder().build()

        // TODO: Change the ad id
        InterstitialAd.load(this,getString(R.string.admob_photowindow_full_test), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                adError?.message?.let { Log.d(TAG, it) }
                mInterstitialAd = null
                loadFullPageAd()
            }
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Full page Ad was loaded.")
                mInterstitialAd = interstitialAd
                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad was dismissed.")
                        loadFullPageAd()
                    }
                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen content.")
                        mInterstitialAd = null
                    }
                }
            }
        })
    }

    private fun setUpUCrop () {
        options = UCrop.Options()
        options.setCompressionQuality(100)
        options.setMaxBitmapSize(10000)
        options.setToolbarColor(ContextCompat.getColor(this, android.R.color.white))
        options.setStatusBarColor(ContextCompat.getColor(this, android.R.color.white))
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, android.R.color.black))
        options.setToolbarWidgetColor(ContextCompat.getColor(this, android.R.color.black))
    }

}