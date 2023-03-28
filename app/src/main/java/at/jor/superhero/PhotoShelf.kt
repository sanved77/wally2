package at.jor.superhero

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.jor.superhero.databinding.PhotoShelfBinding

class PhotoShelf : AppCompatActivity() {

    private lateinit var bnd: PhotoShelfBinding
    private lateinit var label: String
    private lateinit var wallies: ArrayList<String>
    private lateinit var photoShelfAdapter: PhotoShelfAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = PhotoShelfBinding.inflate(layoutInflater)
        setContentView(bnd.root)
        grabStuff()
        setupShelfGrid()
    }

    private fun grabStuff() {
        label = intent.getStringExtra("CategoryLabel")!!
        wallies = intent.getSerializableExtra("SubCategoryWallies") as ArrayList<String>

    }

    private fun setupShelfGrid() {
        val rvShelf: RecyclerView = bnd.rvShelf
        rvShelf.doOnLayout {
            val imgWidth = it.measuredWidth
            rvShelf.layoutManager = GridLayoutManager(applicationContext,NO_OF_ITEMS_IN_SHELF_GRID)
            photoShelfAdapter = wallies?.let { it1 -> PhotoShelfAdapter(it1, this@PhotoShelf, (imgWidth/NO_OF_ITEMS_IN_SHELF_GRID)) }!!
            rvShelf.adapter = photoShelfAdapter
        }
    }

}