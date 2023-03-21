package at.jor.superhero

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import at.jor.superhero.databinding.PhotoShelfBinding

class PhotoShelf : AppCompatActivity() {

    private lateinit var bnd: PhotoShelfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = PhotoShelfBinding.inflate(layoutInflater)
        setContentView(bnd.root)
    }

}