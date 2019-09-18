package ru.byprogminer.modbot.vk.utility

import java.net.URL
import java.net.URLConnection

fun String.openUrlConnection(): URLConnection {
    val connection = URL(this).openConnection()
    connection.setRequestProperty("Accept-Encoding", Charsets.UTF_8.name())

    return connection
}
