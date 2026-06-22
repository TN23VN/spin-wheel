package com.example.spinwheel.data

import android.content.Context
import com.example.spinwheel.model.WheelModel
import com.example.spinwheel.model.WheelSlice
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.random.Random

object WheelRepository {
    private const val PREFS = "spin_wheel_data"
    private const val KEY_WHEELS = "wheels"
    private const val MAX_DUPLICATES = 5

    val defaultColors = listOf(
        0xFFFFBA5F.toInt(),
        0xFFF568B4.toInt(),
        0xFFFFFF74.toInt(),
        0xFFCA8AF2.toInt(),
        0xFF66F766.toInt(),
        0xFFA6E4EF.toInt(),
        0xFFFF6B59.toInt(),
        0xFF197EFB.toInt(),
    )

    val themes = listOf(
        "Classic",
        "Candy",
        "Ocean",
        "Forest",
        "Sunset",
        "Neon",
    )

    private val themeColors = listOf(
        defaultColors,
        listOf(0xFFFF8CC6.toInt(), 0xFFFFD166.toInt(), 0xFFB8F7FF.toInt(), 0xFFD5A3FF.toInt(), 0xFFFF9F9F.toInt()),
        listOf(0xFF93E5FF.toInt(), 0xFF197EFB.toInt(), 0xFF5AD7C8.toInt(), 0xFFE7F8FF.toInt(), 0xFF72A8FF.toInt()),
        listOf(0xFF66F766.toInt(), 0xFF20D600.toInt(), 0xFFEEFFE7.toInt(), 0xFFAE5900.toInt(), 0xFFFFBA5F.toInt()),
        listOf(0xFFFF6B59.toInt(), 0xFFFFBA5F.toInt(), 0xFFFFD6D5.toInt(), 0xFFC4251B.toInt(), 0xFFFFB4A9.toInt()),
        listOf(0xFFFF00C8.toInt(), 0xFF00F5FF.toInt(), 0xFFB6FF00.toInt(), 0xFFFFEA00.toInt(), 0xFF7B61FF.toInt()),
    )

    fun getWheels(context: Context): MutableList<WheelModel> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_WHEELS, null)
        if (json.isNullOrBlank()) {
            return defaultWheels().also { saveWheels(context, it) }
        }

        return runCatching {
            val type = object : TypeToken<MutableList<WheelModel>>() {}.type
            Gson().fromJson<MutableList<WheelModel>>(json, type)
        }.getOrElse {
            defaultWheels().also { saveWheels(context, it) }
        }
    }

    fun getWheel(context: Context, id: Long): WheelModel? {
        return getWheels(context).find { it.id == id }
    }

    fun getDefaultWheel(context: Context): WheelModel {
        return getWheels(context).first()
    }

    fun saveWheel(context: Context, wheel: WheelModel) {
        val wheels = getWheels(context)
        val index = wheels.indexOfFirst { it.id == wheel.id }
        if (index >= 0) {
            wheels[index] = wheel
        } else {
            wheels.add(0, wheel)
        }
        saveWheels(context, wheels)
    }

    fun deleteWheel(context: Context, id: Long) {
        val wheels = getWheels(context).filterNot { it.id == id }.toMutableList()
        if (wheels.isEmpty()) {
            wheels.add(defaultWheel("Who will drink this shot ?"))
        }
        saveWheels(context, wheels)
    }

    fun duplicateWheel(context: Context, id: Long): DuplicateResult {
        val wheels = getWheels(context)
        val source = wheels.find { it.id == id } ?: return DuplicateResult.NotFound
        val baseName = source.name.substringBefore(" Copy").trim()
        val copies = wheels.count { it.name == "$baseName Copy" || it.name.startsWith("$baseName Copy ") }
        if (copies >= MAX_DUPLICATES) {
            return DuplicateResult.LimitReached
        }

        val nextName = uniqueCopyName(baseName, wheels)
        val duplicate = source.copy(
            id = System.currentTimeMillis() + Random.nextLong(1, 999),
            name = nextName,
            slices = source.slices.map { it.copy() }.toMutableList(),
        )
        wheels.add(0, duplicate)
        saveWheels(context, wheels)
        return DuplicateResult.Success(duplicate)
    }

    fun newWheel(themeIndex: Int = 0): WheelModel {
        return WheelModel(
            id = System.currentTimeMillis() + Random.nextLong(1, 999),
            name = "",
            themeIndex = themeIndex.coerceIn(themes.indices),
            slices = MutableList(5) { index ->
                WheelSlice("", colorFor(themeIndex, index))
            },
        )
    }

    fun colorFor(themeIndex: Int, index: Int): Int {
        val palette = themeColors.getOrElse(themeIndex) { defaultColors }
        return palette[index % palette.size]
    }

    fun applyTheme(wheel: WheelModel, themeIndex: Int): WheelModel {
        val normalized = themeIndex.coerceIn(themes.indices)
        val slices = wheel.slices.mapIndexed { index, slice ->
            slice.copy(color = colorFor(normalized, index))
        }.toMutableList()
        return wheel.copy(themeIndex = normalized, slices = slices)
    }

    private fun saveWheels(context: Context, wheels: MutableList<WheelModel>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_WHEELS, Gson().toJson(wheels))
            .apply()
    }

    private fun defaultWheels(): MutableList<WheelModel> {
        return mutableListOf(
            defaultWheel("Who will drink this shot ?"),
            defaultWheel("Call your mom & tell her you're arrested"),
            defaultWheel("Eat some raw eggs"),
            defaultWheel("Drink some Tequila"),
        )
    }

    private fun defaultWheel(name: String): WheelModel {
        val labels = listOf("Brian", "Glenn", "Peter", "Lois", "Joe", "Bonnie")
        return WheelModel(
            id = System.currentTimeMillis() + Random.nextLong(1, 99999),
            name = name,
            themeIndex = 0,
            slices = labels.mapIndexed { index, label ->
                WheelSlice(label, colorFor(0, index))
            }.toMutableList(),
        )
    }

    private fun uniqueCopyName(baseName: String, wheels: List<WheelModel>): String {
        val first = "$baseName Copy"
        if (wheels.none { it.name == first }) return first
        var index = 2
        while (wheels.any { it.name == "$first $index" }) {
            index++
        }
        return "$first $index"
    }

    sealed class DuplicateResult {
        data class Success(val wheel: WheelModel) : DuplicateResult()
        data object LimitReached : DuplicateResult()
        data object NotFound : DuplicateResult()
    }
}
