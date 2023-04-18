package at.jor.superhero

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

internal class CategoryListAdapter(private var itemsList: ArrayList<Category>, private var context: Context, private var width: Int) :
    RecyclerView.Adapter<CategoryListAdapter.MyViewHolder>() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ivHomeItem: ImageView = view.findViewById(R.id.ivHomeItem)
        var ivHolderLayout: ConstraintLayout = view.findViewById(R.id.cvHomeItem)
        var tvCategoryLabel: TextView = view.findViewById(R.id.tvCategoryLabel)
        val height = (width/16)*9

        var firebaseAnalytics = Firebase.analytics

    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 10f
        circularProgressDrawable.centerRadius = 110f
        circularProgressDrawable.setColorSchemeColors(Color.WHITE)
        circularProgressDrawable.start()
        val item = itemsList[position]
        holder.tvCategoryLabel.text = item.name
        holder.ivHolderLayout.layoutParams.width = width
        holder.ivHolderLayout.layoutParams.height = holder.height
        GlideApp.with(context)
            .load(item.cover)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(circularProgressDrawable)
            .into(holder.ivHomeItem)
        val newWallies = ArrayList(item.wallies)
        holder.ivHolderLayout.setOnClickListener {
            holder.firebaseAnalytics.logEvent("Category") {
                param("CategoryName", item.name)
            }
            val intent = Intent(context, PhotoShelf::class.java)
            intent.putExtra("CategoryLabel", item.name)
            intent.putExtra("Wallies", newWallies)
            context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return itemsList.size
    }
}