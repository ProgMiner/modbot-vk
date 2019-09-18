package ru.byprogminer.modbot.vk

import com.alibaba.fastjson.JSON
import ru.byprogminer.modbot.EventBus
import ru.byprogminer.modbot.utility.LargeObject
import ru.byprogminer.modbot.vk.api.VkAttachmentUploader
import ru.byprogminer.modbot.vk.api.VkSelfUser
import ru.byprogminer.modbot.vk.utility.JsonObjectLargeObject
import ru.byprogminer.modbot.vk.utility.openUrlConnection
import java.io.Reader

class VkUserActor(id: Long, accessToken: String, eventBus: EventBus): VkActor(accessToken, eventBus) {

    companion object {

        private const val VK_LP_VER = 3
        private const val VK_LP_MOD = 2 or 8
        private const val VK_LP_URL = "${VkActor.VK_LP_URL}&mode=$VK_LP_MOD&version=$VK_LP_VER"
    }

    override val attachmentUploader = VkAttachmentUploader(this)

    override val user = VkSelfUser(id, this)

    override fun requestLongPollingStatus(): LongPollingStatus {
        val response = api("messages.getLongPollServer", mapOf("lp_version" to VK_LP_VER))
            .getJSONObject("response")

        return LongPollingStatus(
            key = response.getString("key"),
            server = response.getString("server"),
            ts = response.getString("ts")
        )
    }

    override fun requestLongPollingUpdates(longPollingStatus: LongPollingStatus) = JsonObjectLargeObject(JSON
        .parseObject(VK_LP_URL.format(longPollingStatus.server, longPollingStatus.key, longPollingStatus.ts)
            .openUrlConnection().getInputStream().reader().use(Reader::readText)))

    override fun processLongPollingUpdates(updates: LargeObject) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
