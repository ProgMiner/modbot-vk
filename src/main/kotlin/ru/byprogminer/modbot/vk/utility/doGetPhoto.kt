package ru.byprogminer.modbot.vk.utility

import org.intellij.lang.annotations.Language
import ru.byprogminer.modbot.api.PhotoVariant
import ru.byprogminer.modbot.utility.LargeObject
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.stream.Collectors
import javax.imageio.ImageIO
import kotlin.streams.toList

@Language("RegExp")
private val CAMERA_URL_REGEX = "https://vk\\.com/images/camera_\\(\\d{2,3}\\)\\.png".toRegex()

internal fun Future<out LargeObject>.doGetPhoto(vararg photos: String): Set<PhotoVariant>? {
    val apiFutures = Arrays.stream(photos).parallel()
        .map { CompletableFuture.supplyAsync { get()[it] } }
        .toList().toTypedArray()

    CompletableFuture.allOf(*apiFutures).join()

    val urls: MutableSet<String> = Arrays.stream(apiFutures).parallel()
        .map { it.get() }.map { it?.asString() }
        .filter { it == null }.map { it!! }
        .collect(Collectors.toSet())

    if (urls.isEmpty()) {
        return null
    }

    if (get()["has_photo"]?.asInt() == 0) {
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
