package ru.byprogminer.modbot.vk.utility

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import ru.byprogminer.modbot.utility.HolderLargeObject
import ru.byprogminer.modbot.utility.LargeObject

class JsonObjectLargeObject(override val value: JSONObject): LargeObject {

    override fun get(key: String): LargeObject? = value[key]?.let {
        when (it) {
            is JSONObject -> JsonObjectLargeObject(it)
            is JSONArray -> JsonArrayLargeObject(it)
            else -> HolderLargeObject(it)
        }
    }
}
