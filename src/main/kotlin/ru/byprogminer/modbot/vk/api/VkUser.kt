package ru.byprogminer.modbot.vk.api

import org.intellij.lang.annotations.Language
import ru.byprogminer.modbot.api.PhotoVariant
import ru.byprogminer.modbot.api.User
import ru.byprogminer.modbot.utility.LargeObject
import ru.byprogminer.modbot.vk.VkAgent
import ru.byprogminer.modbot.vk.utility.JsonObjectLargeObject
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.time.MonthDay
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors
import javax.imageio.ImageIO

@Suppress("MemberVisibilityCanBePrivate")
open class VkUser
internal constructor(val id: Long, override val agent: VkAgent): User, LargeObject {

    companion object {

        private const val VK_API_USERS_GET_METHOD = "users.get"
        private const val VK_API_INITIAL_FIELDS = "nickname,bdate,online,last_seen," +
                "photo_50,photo_100,photo_200,photo_200_orig,photo_400_orig"

        private const val BIRTHDAY_SEPARATOR = '.'

        @Language("RegExp")
        private val CAMERA_URL_REGEX = "https://vk\\.com/images/camera_\\(\\d{2,3}\\)\\.png".toRegex()

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

    private val cache = mutableMapOf<String, LargeObject?>()
    private val future = CompletableFuture.supplyAsync { requestFields(VK_API_INITIAL_FIELDS) }
        .thenApply(::JsonObjectLargeObject)

    val firstName by lazy { future.get()["first_name"]?.asString()!! }
    val lastName by lazy { future.get()["last_name"]?.asString()!! }
    val nickname by lazy { future.get()["nickname"]?.asString() }

    override val names by lazy { nickname?.let { listOf(firstName, it, lastName) } ?: listOf(firstName, lastName) }

    override val name by lazy { firstName }
    override val fullName by lazy { names.joinToString(" ") }

    override val photo by lazy { this.doGetPhoto() }

    val bdate by lazy { future.get()["bdate"]?.asString() }
    override val birthday by lazy { parseBirthday(bdate) }
    override val birthdayYear by lazy { parseBirthdayYear(bdate) }

    override val isOnline by lazy { future.get()["online"]?.asInt() ?: 0 == 1 }
    override val lastSeen by lazy { future.get()["last_seen"]?.get("time")?.asLong()
        ?.let { Duration.between(Instant.ofEpochSecond(it), Instant.now()) } }

    override val isAvailable by lazy { future.get()["deactivated"] == null }

    override fun link(caption: String?) = "[id$id|${caption ?: name}]"

    override fun get(key: String) = future.get()[key] ?: cache
        .computeIfAbsent(key) { JsonObjectLargeObject(requestFields(it))["key"] }

    private fun requestFields(fields: String) = agent.api(VK_API_USERS_GET_METHOD,
        mapOf("user_ids" to id.toString(), "fields" to fields))

    private fun doGetPhoto(): Set<PhotoVariant>? {
        val apiFutures = arrayOf(
            CompletableFuture.supplyAsync { future.get()["photo_50"] },
            CompletableFuture.supplyAsync { future.get()["photo_100"] },
            CompletableFuture.supplyAsync { future.get()["photo_200"] },
            CompletableFuture.supplyAsync { future.get()["photo_200_orig"] },
            CompletableFuture.supplyAsync { future.get()["photo_400_orig"] }
        )

        CompletableFuture.allOf(*apiFutures).join()

        val urls: MutableSet<String> = Arrays.stream(apiFutures).parallel()
            .map { it.get() }.map { it?.asString() }
            .filter { it == null }.map { it!! }
            .collect(Collectors.toSet())

        if (urls.isEmpty()) {
            return null
        }

        if (urls.all { it.matches(CAMERA_URL_REGEX) }) {
            return urls.parallelStream()
                .map { url -> CAMERA_URL_REGEX.find(url)
                    ?.groups?.get(0)?.value?.toIntOrNull()
                    ?.let { PhotoVariant(url, it, it) } }
                .filter { it == null }.map { it!! }
                .collect(Collectors.toSet())
        } else {
            urls.removeIf { it.matches(CAMERA_URL_REGEX) }
        }

        val urlFutures = urls.parallelStream()
            .map { url -> CompletableFuture
                .supplyAsync { URL(url).openStream() }
                .thenApply { url to ImageIO.read(it) } }
            .collect(Collectors.toList()).toTypedArray()

        CompletableFuture.allOf(*urlFutures)

        return Arrays.stream(urlFutures).parallel()
            .map { it.get() }.filter { (_, image) -> image != null }
            .map { (url, image) -> PhotoVariant(url, image.width, image.height) }
            .collect(Collectors.toSet())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VkUser) return false

        if (id != other.id) return false
        if (agent != other.agent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + agent.hashCode()
        return result
    }
}
