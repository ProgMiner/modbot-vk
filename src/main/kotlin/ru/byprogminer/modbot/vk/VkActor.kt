package ru.byprogminer.modbot.vk

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import ru.byprogminer.modbot.AbstractActor
import ru.byprogminer.modbot.EventBus
import ru.byprogminer.modbot.utility.LargeObject
import ru.byprogminer.modbot.vk.api.VkAttachmentUploader
import ru.byprogminer.modbot.vk.api.VkConversation
import ru.byprogminer.modbot.vk.api.VkGroup
import ru.byprogminer.modbot.vk.api.VkUser
import ru.byprogminer.modbot.vk.utility.JsonObjectLargeObject
import ru.byprogminer.modbot.vk.utility.formatUrlQuery
import ru.byprogminer.modbot.vk.utility.openUrlConnection
import java.io.Reader
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

abstract class VkActor(private val accessToken: String, eventBus: EventBus):
    AbstractActor<VkAttachmentUploader>(eventBus)
{

    protected data class LongPollingStatus(
        val key: String,
        val server: String,
        val ts: String
    )

    companion object {

        private const val VK_API_URL = "https://api.vk.com/method/%1\$s?%2\$s"
        private const val VK_API_VER = "5.101"

        private const val VK_LP_WAIT = 25
        const val VK_LP_URL = "https://%1\$s?act=a_check&key=%2\$s&ts=%3\$s&wait=$VK_LP_WAIT"
    }

    private lateinit var longPollingFuture: Future<*>
    private lateinit var longPollingStatus: LongPollingStatus

    private val usersCache = WeakHashMap<Long, VkUser>()
    private val groupsCache = WeakHashMap<Long, VkGroup>()
    private val conversationsCache = WeakHashMap<Long, VkConversation>()

    abstract override val attachmentUploader: VkAttachmentUploader

    fun getUser(id: Long): VkUser = usersCache.computeIfAbsent(id) { VkUser(it, this) }
    fun getGroup(id: Long): VkGroup = groupsCache.computeIfAbsent(id) { VkGroup(it, this) }
    fun getConversation(id: Long): VkConversation = conversationsCache
        .computeIfAbsent(id) { VkConversation(it, this) }

    override fun onStart() {
        longPollingFuture = CompletableFuture.runAsync {
            longPollingStatus = requestLongPollingStatus()

            longPollingIteration()
        }
    }

    private fun longPollingIteration() {
        val response = requestLongPollingUpdates(longPollingStatus)

        val ts = response["ts"]?.asString()
        if (ts != null) {
            longPollingStatus = longPollingStatus.copy(ts = ts)
        }

        when (val failed = response["failed"]?.asInt()) {
            1, null -> {
                longPollingFuture = CompletableFuture.runAsync(this::longPollingIteration)

                if (failed == 1) {
                    return
                }
            }

            else -> {
                val newLongPollingStatus = requestLongPollingStatus()

                longPollingStatus = when (failed) {
                    2 -> longPollingStatus.copy(key = newLongPollingStatus.key)
                    3 -> longPollingStatus.copy(key = newLongPollingStatus.key, ts = newLongPollingStatus.ts)
                    else -> newLongPollingStatus
                }
            }
        }

        processLongPollingUpdates(response["updates"] ?: return)
    }

    protected abstract fun requestLongPollingStatus(): LongPollingStatus
    protected abstract fun requestLongPollingUpdates(longPollingStatus: LongPollingStatus): LargeObject
    protected abstract fun processLongPollingUpdates(updates: LargeObject)

    internal fun api(method: String, arguments: Map<String, Any>, assertNoError: Boolean = true): JSONObject {
        val response = JSON.parseObject(requestApi(method, arguments))

        if (!assertNoError) {
            return response
        }

        throw VkApiException(JsonObjectLargeObject(response.getJSONObject("error") ?: return response))
    }

    private fun requestApi(method: String, arguments: Map<String, Any>): String {
        val args = mutableMapOf<String, Any>("access_token" to accessToken, "v" to VK_API_VER)
        args.putAll(arguments)

        return VK_API_URL.format(method, args.formatUrlQuery()).openUrlConnection()
            .getInputStream().reader().use(Reader::readText)
    }
}