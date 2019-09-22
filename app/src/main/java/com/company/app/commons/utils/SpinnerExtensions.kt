package com.company.app.commons.utils

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import es.situm.sdk.model.cartography.Building

/**
 * Copyright (c) 2017 Fueled. All rights reserved.
 * @author chetansachdeva on 10/04/18
 */
object SpinnerExtensions {

    /**
     * set spinner buildings
     */
    fun Spinner.setSpinnerBuildings(buildings: List<Building>?) {
        if (buildings != null) {
            val arrayAdapter = SpinnerBuildingAdapter(context, buildings)
            adapter = arrayAdapter
        }
    }

    /**
     * set spinner onItemSelectedListener listener
     */
    fun Spinner.setSpinnerItemSelectedListener(listener: ItemSelectedListener?) {
        if (listener == null) {
            onItemSelectedListener = null
        } else {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (tag != position) {
                        listener.onItemSelected(parent.getItemAtPosition(position))
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    interface ItemSelectedListener {
        fun onItemSelected(item: Any)
    }
}