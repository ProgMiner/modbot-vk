package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.api.message.Attachment
import java.time.ZonedDateTime

data class VkReceivedRemoteMessage(
    override val id: Long,
    override val chat: VkChat?,
    override val user: VkAccount,
    override val text: String?,
    override val attachments: List<Attachment>,
    override val date: ZonedDateTime,
    override val randomId: Long?,
    override val ref: String?,
    override val refSource: String?,
    override val important: Boolean,
    override val geo: Geo?,
    override val payload: String?,
    override val reply: VkRemoteMessage?,
    override val forwarded: List<VkRemoteMessage>,
    override val action: Action?
): VkRemoteMessage() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VkReceivedRemoteMessage

        if (id != other.id) return false
        if (chat != other.chat) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + chat.hashCode()
        return result
    }
}
