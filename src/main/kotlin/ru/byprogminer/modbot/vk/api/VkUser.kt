package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.utility.LargeObject
import ru.byprogminer.modbot.vk.VkActor
import ru.byprogminer.modbot.vk.utility.JsonObjectLargeObject
import ru.byprogminer.modbot.vk.utility.doGetPhoto
import java.time.Duration
import java.time.Instant
import java.time.MonthDay
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@Suppress("MemberVisibilityCanBePrivate")
open class VkUser
internal constructor(
    val id: Long,
    override val actor: VkActor,
    private val future: Future<LargeObject> = CompletableFuture.supplyAsync {
        requestFields(id, actor, VK_API_INITIAL_FIELDS)
    }
): VkAccount() {

    companion object {

        private const val VK_API_INITIAL_FIELDS = "nickname,bdate,online,last_seen," +
                "photo_50,photo_100,photo_200,photo_200_orig,photo_400_orig"

        private const val BIRTHDAY_SEPARATOR = '.'

        internal fun requestFields(id: Long, actor: VkActor, fields: String) = JsonObjectLargeObject(actor
            .api("users.get", mapOf("user_ids" to id.toString(), "fields" to fields))
            .getJSONArray("response").getJSONObject(0))

        private fun parseBirthday(bdate: String?): MonthDay? {
            if (bdate == null) {
                return null
            }

            val components = bdate.split(BIRTHDAY_SEPARATOR)
            val day = components.elementAtOrNull(0)?.toIntOrNull() ?: return null
            val month = components.elementAtOrNull(1)?.toIntOrNull() ?: return null

            return MonthDay.of(month, day)
        }

        private fun parseBirthdayYear(bdate: String?): Int? = bdate
            ?.split(BIRTHDAY_SEPARATOR)?.elementAtOrNull(2)?.toIntOrNull()
    }

    private val customProperties = mutableMapOf<String, LargeObject?>()

    val firstName by lazy { future.get()["first_name"]?.asString()!! }
    val lastName by lazy { future.get()["last_name"]?.asString()!! }
    val nickname by lazy { future.get()["nickname"]?.asString() }

    override val names by lazy { nickname?.let { listOf(firstName, it, lastName) } ?: listOf(firstName, lastName) }

    override val name by lazy { firstName }
    override val fullName by lazy { names.joinToString(" ") }

    override val photo by lazy { future
        .doGetPhoto("photo_50", "photo_100", "photo_200", "photo_200_orig", "photo_400_orig") }

    val bdate by lazy { future.get()["bdate"]?.asString() }
    override val birthday by lazy { parseBirthday(bdate) }
    override val birthdayYear by lazy { parseBirthdayYear(bdate) }

    override val isOnline by lazy { future.get()["online"]?.asInt() ?: 0 == 1 }
    override val lastSeen by lazy { future.get()["last_seen"]?.get("time")?.asLong()
        ?.let { Duration.between(Instant.ofEpochSecond(it), Instant.now()) } }

    override val isAvailable by lazy { future.get()["deactivated"] == null }

    override fun link(caption: String?) = "[id$id|${caption ?: name}]"

    override operator fun get(key: String) = future.get()[key] ?: customProperties
        .computeIfAbsent(key) { requestFields(id, actor, it)["key"] }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VkUser

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
