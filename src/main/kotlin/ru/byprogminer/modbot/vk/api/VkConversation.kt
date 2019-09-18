package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.api.Conversation
import ru.byprogminer.modbot.api.Message
import ru.byprogminer.modbot.api.PhotoVariant
import ru.byprogminer.modbot.api.User
import ru.byprogminer.modbot.utility.LargeObject
import ru.byprogminer.modbot.vk.VkActor
import ru.byprogminer.modbot.vk.utility.JsonObjectLargeObject
import ru.byprogminer.modbot.vk.utility.doGetPhoto
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@Suppress("MemberVisibilityCanBePrivate")
class VkConversation
internal constructor(
    val id: Long,
    override val actor: VkActor,
    private val future: Future<LargeObject> = CompletableFuture.supplyAsync { JsonObjectLargeObject(actor
        .api("messages.getConversationById", mapOf("peer_id" to (2_000_000_000 + id).toString()))
        .getJSONObject("response").getJSONArray("items").getJSONObject(0)) }
): Conversation {

    private val chatSettings by lazy { future.get()["chat_settings"]!! }

    private val _name by lazy { chatSettings["title"]!!.asString() }
    override var name: String
        get() = _name
        set(value) = TODO()

    override val photo: Set<PhotoVariant>? by lazy { CompletableFuture.supplyAsync { chatSettings }
        .doGetPhoto("photo_50", "photo_100", "photo_200") }

    private val _pinnedMessage: Nothing by lazy { chatSettings["pinned_message"]; TODO("RemoteMessage factory") }
    override var pinnedMessage: Message?
        get() = _pinnedMessage
        set(value) = TODO()

    // VK API doesn't provide conversation owner
    override val owner get() = throw UnsupportedOperationException() // TODO
    override val admins: Map<Int, User> get() = TODO("getConversationMembers parser with VkUser and VkGroup building")
    override val members: Set<User> get() = TODO()

    /**
     * Warning: VK API doesn't provide an ability to joining users for groups!
     */
    override fun joinUser(user: User) {
        require(user is VkUser)

        actor.api("messages.addChatUser", mapOf("chat_id" to id.toString(), "user_id" to user.id.toString()))
    }

    override fun kickUser(user: User) {
        require(user is VkAccount)

        actor.api("messages.removeChatUser", mapOf("chat_id" to id.toString(), when (user) {
            is VkUser -> "user_id" to user.id.toString()
            is VkGroup -> "member_id" to "-${user.id}"
            else -> throw IllegalArgumentException()
        }))
    }

    override fun appointAdmin(user: User, rank: Int) = throw UnsupportedOperationException()
    override fun dismissAdmin(user: User) = throw UnsupportedOperationException()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VkConversation

        if (id != other.id) return false
        if (actor != other.actor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + actor.hashCode()
        return result
    }
}
