package com.example.spinwheel.model

data class WheelSlice(
    var label: String = "",
    var color: Int = 0,
    var hidden: Boolean = false,
)

data class WheelModel(
    var id: Long = System.currentTimeMillis(),
    var name: String = "",
    var themeIndex: Int = 0,
    var fontSize: Int = 14,
    var repeat: Int = 1,
    var spinTime: Int = 1800,
    var slices: MutableList<WheelSlice> = mutableListOf(),
)
