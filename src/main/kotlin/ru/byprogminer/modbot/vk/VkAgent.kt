package ru.byprogminer.modbot.vk

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import ru.byprogminer.modbot.AbstractAgent
import ru.byprogminer.modbot.vk.api.VkAttachmentUploader
import ru.byprogminer.modbot.vk.api.VkConversation
import ru.byprogminer.modbot.vk.api.VkGroup
import ru.byprogminer.modbot.vk.api.VkUser
import ru.byprogminer.modbot.vk.utility.JsonObjectLargeObject
import java.io.Reader
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.stream.Collectors

abstract class VkAgent(private val accessToken: String): AbstractAgent<VkAttachmentUploader>() {

    companion object {

        private const val VK_API_URL = "https://api.vk.com/method/%1\$s?%2\$s"
        private const val VK_API_VER = "5.101"
    }

    private val usersCache = WeakHashMap<Long, VkUser>()
    private val groupsCache = WeakHashMap<Long, VkGroup>()
    private val conversationsCache = WeakHashMap<Long, VkConversation>()

    abstract override val attachmentUploader: VkAttachmentUploader

    fun getUser(id: Long): VkUser = usersCache.computeIfAbsent(id) { VkUser(it, this) }
    fun getGroup(id: Long): VkGroup = groupsCache.computeIfAbsent(id) { VkGroup(it, this) }
    fun getConversation(id: Long): VkConversation = conversationsCache.computeIfAbsent(id) { VkConversation(it, this) }

    // TODO long polling

    internal fun api(method: String, arguments: Map<String, String>, assertNoError: Boolean = true): JSONObject {
        val response = JSON.parseObject(requestApi(method, arguments))

        if (!assertNoError) {
            return response
        }

        throw VkApiException(JsonObjectLargeObject(response.getJSONObject("error") ?: response))
    }

    private fun requestApi(method: String, arguments: Map<String, String>): String {
        val args = mutableMapOf("access_token" to accessToken, "v" to VK_API_VER)
        args.putAll(arguments)

        val url = VK_API_URL.format(method, args.entries.parallelStream().map { (key, value) ->
            "${URLEncoder.encode(key, Charsets.UTF_8.name())}=${URLEncoder.encode(value, Charsets.UTF_8.name())}" }
            .collect(Collectors.joining("&")))

        val connection = URL(url).openConnection()
        connection.setRequestProperty("Accept-Encoding", Charsets.UTF_8.name())

        return connection.getInputStream().reader().use(Reader::readText)
    }
}