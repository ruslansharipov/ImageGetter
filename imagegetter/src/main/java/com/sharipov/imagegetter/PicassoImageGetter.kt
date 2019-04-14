package com.sharipov.imagegetter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.Log
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class PicassoImageGetter constructor() : Html.ImageGetter {
    constructor(
        picasso: Picasso,
        textView: TextView
    ) : this() {
        this.picasso = picasso
        this.textView = textView
    }

    private companion object {
        const val TAG = "PicassoImageGetter"
    }

    lateinit var picasso: Picasso
    lateinit var textView: TextView

    var urlEditor: ((String) -> String)? = null
    var placeHolderRes: Int? = null
    var errorDrawableRes: Int? = null

    override fun getDrawable(source: String): Drawable = BitmapDrawablePlaceHolder().apply {
        val url = urlEditor?.invoke(source) ?: source
        Log.d(TAG, "Start loading url: $url")
        var requestCreator = picasso.load(url)
        placeHolderRes?.let { requestCreator = requestCreator.placeholder(it) }
        errorDrawableRes?.let { requestCreator = requestCreator.placeholder(it) }
        requestCreator.into(this)
    }

    private inner class BitmapDrawablePlaceHolder : BitmapDrawable(), Target {
        private var drawable: Drawable? = null
            set(value) {
                if (value != null) {
                    field = value
                    checkBounds()
                }
            }

        override fun draw(canvas: Canvas) {
            if (drawable != null) {
                checkBounds()
                drawable!!.draw(canvas)
            }
        }

        private fun checkBounds() {
            val defaultProportion = drawable!!.intrinsicWidth.toFloat() / drawable!!.intrinsicHeight.toFloat()
            val width = textView.width
            val height = (width.toFloat() / defaultProportion).toInt()

            if (bounds.right != textView.width || bounds.bottom != height) {
                setBounds(0, 0, width, height) //set to full width
                drawable!!.setBounds(0, 0, width, height)
                textView.text = textView.text
            }
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom) {
            drawable = BitmapDrawable(textView.context.resources, bitmap)
            Log.v(TAG, "onBitmapLoaded: Bitmap $bitmap from: $from")
        }

        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
            drawable = errorDrawable
            Log.e(TAG, "onBitmapFailed: Exception ${e.message}")
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            drawable = placeHolderDrawable
            Log.v(TAG, "onPrepareLoad: placeHolderDrawable == $placeHolderDrawable")
        }
    }
}
