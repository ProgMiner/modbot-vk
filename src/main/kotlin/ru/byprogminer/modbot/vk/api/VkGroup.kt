package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.utility.LargeObject
import ru.byprogminer.modbot.vk.VkActor
import ru.byprogminer.modbot.vk.utility.JsonObjectLargeObject
import ru.byprogminer.modbot.vk.utility.doGetPhoto
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@Suppress("MemberVisibilityCanBePrivate")
open class VkGroup
internal constructor(
    val id: Long,
    override val actor: VkActor,
    private val future: Future<LargeObject> = CompletableFuture.supplyAsync {
        requestFields(id, actor, VK_API_INITIAL_FIELDS)
    }
): VkAccount() {

    companion object {

        private const val VK_API_INITIAL_FIELDS = "has_photo"

        internal fun requestFields(id: Long, actor: VkActor, fields: String) = JsonObjectLargeObject(actor
            .api("groups.getById", mapOf("group_id" to id.toString(), "fields" to fields))
            .getJSONArray("response").getJSONObject(0))
    }

    private val customProperties = mutableMapOf<String, LargeObject?>()

    override val name by lazy { future.get()["name"]?.asString()!! }

    override val fullName by lazy { name }
    override val names by lazy { listOf(name) }

    override val photo by lazy { future.doGetPhoto("photo_50", "photo_100", "photo_200") }

    override val birthday: Nothing? = null
    override val birthdayYear: Nothing? = null

    override val isOnline = false
    override val lastSeen: Nothing? = null

    override val isAvailable by lazy { future.get()["deactivated"] == null }

    override fun link(caption: String?) = "[club$id|${caption ?: name}]"

    override operator fun get(key: String) = future.get()[key] ?: customProperties
        .computeIfAbsent(key) { requestFields(id, actor, it)["key"] }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VkGroup

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
