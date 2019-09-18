package ru.byprogminer.modbot.vk

import com.alibaba.fastjson.JSON
import ru.byprogminer.modbot.EventBus
import ru.byprogminer.modbot.utility.LargeObject
import ru.byprogminer.modbot.vk.api.VkAttachmentUploader
import ru.byprogminer.modbot.vk.api.VkSelfGroup
import ru.byprogminer.modbot.vk.utility.JsonObjectLargeObject
import ru.byprogminer.modbot.vk.utility.openUrlConnection
import java.io.Reader

class VkGroupActor(id: Long, accessToken: String, eventBus: EventBus): VkActor(accessToken, eventBus) {

    override val attachmentUploader = VkAttachmentUploader(this)

    override val user = VkSelfGroup(id, this)

    override fun requestLongPollingStatus(): VkActor.LongPollingStatus {
        val response = api("groups.getLongPollServer", mapOf("group_id" to user.id)).getJSONObject("response")

        return LongPollingStatus(
            key = response.getString("key"),
            server = response.getString("server"),
            ts = response.getString("ts")
        )
    }

    override fun requestLongPollingUpdates(longPollingStatus: VkActor.LongPollingStatus) = JsonObjectLargeObject(JSON
        .parseObject(VK_LP_URL.format(longPollingStatus.server, longPollingStatus.key, longPollingStatus.ts)
            .openUrlConnection().getInputStream().reader().use(Reader::readText)))

    override fun processLongPollingUpdates(updates: LargeObject) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
