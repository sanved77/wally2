package xyz.nagdibai.superwallpapers

import at.jor.superhero.Saamaan
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WallyApi {

    @GET("getSaamaan/{appName}")
    fun getSaamaan(
        @Path("appName") appName: String?
    ): Call<Saamaan>

}