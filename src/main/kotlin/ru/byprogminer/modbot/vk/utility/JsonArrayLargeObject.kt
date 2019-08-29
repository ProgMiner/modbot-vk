package ru.byprogminer.modbot.vk.utility

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import ru.byprogminer.modbot.utility.HolderLargeObject
import ru.byprogminer.modbot.utility.LargeObject

class JsonArrayLargeObject(override val value: JSONArray): LargeObject {

    override fun get(index: Int): LargeObject? = value[index]?.let {
        when (it) {
            is JSONObject -> JsonObjectLargeObject(it)
            is JSONArray -> JsonArrayLargeObject(it)
            else -> HolderLargeObject(it)
        }
    }
}
