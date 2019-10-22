package com.noandish.nupdate

import android.content.Context
import android.graphics.Typeface

object TypefaceCache {

    private val listTypeface = ArrayList<TypefaceOption>()

    fun addTypeface(nameTypeface: String, context: Context): Typeface {
        for (i in listTypeface.indices) {
            if (listTypeface[i].nameTypeface == nameTypeface) {
                return listTypeface[i].typeface
            }
        }
        val typeface = Typeface.createFromAsset(context.assets, nameTypeface)
        listTypeface.add(
            TypefaceOption(
                typeface,
                nameTypeface
            )
        )
        return typeface
    }

    fun addTypeface(typeface: Typeface, nameTypeface: String) {
        listTypeface.add(TypefaceOption(typeface, nameTypeface))
    }

    fun removeTypeface(typeface: Typeface) {
        listTypeface.forEach {
            if (it.typeface == typeface) {
                listTypeface.remove(it)
            }
        }
    }

    class TypefaceOption(val typeface: Typeface, val nameTypeface: String)

    private const val TAG = "TypefaceCache"
}