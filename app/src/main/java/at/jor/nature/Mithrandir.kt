package at.jor.superhero

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager


fun Context.showDialog(
    title: String,
    body: String,
    btnText: String,
    cancelBtnText: String = "",
    callback: () -> Unit
) {
    AlertDialog.Builder(this, R.style.AlertDialogTheme).also {
        it.setTitle(title)
        it.setMessage(body)
        it.setPositiveButton(btnText) { _, _ ->
            callback()
        }
        if(cancelBtnText != "") {
            it.setNegativeButton(cancelBtnText) { _, _ ->
                Log.d("Mithrandir", "Cancel pressed on ad watch")
            }
        }
    }.create().show()
}

fun applyPadding(context: Context, param: ViewGroup.MarginLayoutParams, side: Int, paddingSize: Int) {
    var dpRatio = context.resources.displayMetrics.density;
    var pad = (paddingSize * dpRatio).toInt()
    when (side) {
        POS -> param.setMargins(pad,pad,0,0)
        LAST_POS -> param.setMargins(pad,pad,pad,0)
    }
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun getEmoji(unicode: Int): String {
    return String(Character.toChars(unicode))
}

private fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw      = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    } else {
        @Suppress
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }
}