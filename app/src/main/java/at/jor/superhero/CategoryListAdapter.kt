package at.jor.superhero

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide

internal class CategoryListAdapter(private var itemsList: ArrayList<Category>, private var context: Context, private var width: Int) :
    RecyclerView.Adapter<CategoryListAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ivHomeItem: ImageView = view.findViewById(R.id.ivHomeItem)
        var ivHolderLayout: ConstraintLayout = view.findViewById(R.id.cvHomeItem)
        val height = (width/16)*9
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 60f
        circularProgressDrawable.start()
        val item = itemsList?.get(position)
        holder.ivHolderLayout.getLayoutParams().width = width
        holder.ivHolderLayout.getLayoutParams().height = holder.height
        if (item != null) {
            Log.e("CATEGORY", "----- good -----")
            Glide.with(context)
                .load(item.cover)
                .skipMemoryCache(true)
                .placeholder(circularProgressDrawable)
                .into(holder.ivHomeItem)
        } else {
            Log.e("CATEGORY", "----- bad -----")
        }


//        holder.itemImageView.setOnClickListener {
//            val intent = Intent(context, PhotoWindow::class.java)
//            intent.putExtra("StartIdx", position)
//            intent.putExtra("ItemsList", itemsList)
//
//            context.startActivity(intent)
//        }
    }
    override fun getItemCount(): Int {
        return 3
    }
}