package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.api.NewMessage
import ru.byprogminer.modbot.api.PhotoVariant
import ru.byprogminer.modbot.api.RemoteMessage
import ru.byprogminer.modbot.api.message.Sticker
import kotlin.streams.asStream

abstract class VkRemoteMessage: VkMessage, RemoteMessage {

    data class Geo(
        val type: String,
        val coordinates: Coordinates,
        val place: Place?
    ) {

        data class Coordinates(
            val latitude: Double,
            val longitude: Double
        )

        data class Place(
            val id: Long?,
            val title: String?,
            val latitude: Double,
            val longitude: Double,
            val created: Long?,
            val icon: String?,
            val country: String,
            val city: String
        )
    }

    data class Action(
        val type: String,
        val memberId: Long?,
        val text: String?,
        val email: String?,
        val photo: Set<PhotoVariant>
    )

    companion object {

        private val USER_MENTION_REGEX = "vk\\.com/(\\w+)|\\[(\\w+)\\|.+]".toRegex()
    }

    abstract val id: Long

    abstract override val chat: VkChat?
    abstract override val user: VkAccount

    abstract val randomId: Long?

    abstract val ref: String?
    abstract val refSource: String?

    abstract val important: Boolean

    abstract val geo: Geo?
    abstract val payload: String?

    abstract val action: Action?

    override val allMentionedUsers by lazy { doGetAllMentionedUsers() }

    private fun doGetAllMentionedUsers() = mutableListOf<VkAccount>().apply {
        text?.let { text -> USER_MENTION_REGEX.findAll(text).asStream().parallel()
            .map { chat.agent.resolveScreenName(it.groups[0]?.value) }
            .filter { it != null }.forEach { add(it) } }

        reply?.user?.let(this::add)
        forwarded.forEach { add(it.user) }
    }

    override fun edit(new: NewMessage) = edit(new as? VkNewMessage
        ?: throw IllegalArgumentException("new isn't VkNewMessage"))

    open fun edit(new: VkNewMessage): Nothing = TODO()

    override fun remove(forAll: Boolean) {
        chat.agent.api("messages.remove", mutableMapOf("message_ids" to id.toString())
            .also { if (forAll) it["delete_for_all"] = "1" })
    }

    override fun cloneToNew() = VkNewMessage(text, reply, forwarded, attachments, geo?.coordinates?.latitude,
        geo?.coordinates?.longitude, attachments.parallelStream().filter { it is Sticker }.map { it as Sticker }
            .findAny().orElseGet { null }?.id, null, null, null)
}
