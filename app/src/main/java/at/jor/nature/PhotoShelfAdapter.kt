package at.jor.superhero

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase


internal class PhotoShelfAdapter(private var itemsList: ArrayList<String>, private var context: Context, private var width: Int) :
    RecyclerView.Adapter<PhotoShelfAdapter.MyViewHolder>() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemImageView: ImageView = view.findViewById(R.id.ivPopListItem)
        var popItemCard: LinearLayout = view.findViewById(R.id.popItemCard)
        var llPhotoHolder: LinearLayout = view.findViewById(R.id.llPhotoHolder)
        val photoHolderParam = llPhotoHolder.layoutParams as ViewGroup.MarginLayoutParams
        val cardParam = popItemCard.layoutParams as ViewGroup.MarginLayoutParams
        val height = (width/9)*16
        var firebaseAnalytics = Firebase.analytics
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.shelf_item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 60f
        circularProgressDrawable.setColorSchemeColors(Color.BLACK)
        circularProgressDrawable.start()
        var isReadyToBeOpened = false
        val item = itemsList[position]
        applyPadding(context, holder.cardParam, if (position % NO_OF_ITEMS_IN_SHELF_GRID == NO_OF_ITEMS_IN_SHELF_GRID - 1) LAST_POS else POS, 2)
        holder.photoHolderParam.width = width
        holder.photoHolderParam.height = holder.height
        GlideApp.with(context)
            .load(item)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(circularProgressDrawable)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Drawable>?, p3: Boolean): Boolean {
                    Toast.makeText(context, "Image load failed", Toast.LENGTH_SHORT).show()
                    return false
                }
                override fun onResourceReady(p0: Drawable?, p1: Any?, p2: Target<Drawable>?, p3: DataSource?, p4: Boolean): Boolean {
                    isReadyToBeOpened = true
                    circularProgressDrawable.stop()
                    return false
                }
            })
            .into(holder.itemImageView)
        holder.itemImageView.setOnClickListener {
            if(isReadyToBeOpened) {
                holder.firebaseAnalytics.logEvent("PhotoOpened") {
                    param("PhotoLink", item)
                }
                val intent = Intent(context, PhotoWindow::class.java)
                intent.putExtra("StartIdx", position)
                intent.putExtra("ItemsList", itemsList)
                context.startActivity(intent)
            }
        }
    }
    override fun getItemCount(): Int {
        return itemsList.size
    }
}
