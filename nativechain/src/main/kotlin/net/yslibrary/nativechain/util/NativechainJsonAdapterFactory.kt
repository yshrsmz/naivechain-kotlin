package net.yslibrary.nativechain.util

import com.squareup.moshi.JsonAdapter
import se.ansman.kotshi.KotshiJsonAdapterFactory

@KotshiJsonAdapterFactory
abstract class NativechainJsonAdapterFactory : JsonAdapter.Factory {

    companion object {
        val INSTANCE: NativechainJsonAdapterFactory = KotshiNativechainJsonAdapterFactory()
    }
}