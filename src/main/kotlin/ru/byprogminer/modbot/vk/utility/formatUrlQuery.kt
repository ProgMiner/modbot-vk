package ru.byprogminer.modbot.vk.utility

import java.net.URLEncoder
import java.util.stream.Collectors

fun Map<String, Any>.formatUrlQuery(): String = entries.parallelStream().map { (key, value) ->
    "${URLEncoder.encode(key, Charsets.UTF_8.name())}=${URLEncoder.encode(value.toString(), Charsets.UTF_8.name())}" }
    .collect(Collectors.joining("&"))
