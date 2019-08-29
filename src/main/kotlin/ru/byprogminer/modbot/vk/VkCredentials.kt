package ru.byprogminer.modbot.vk

sealed class VkCredentials {

    abstract val id: Long
    internal abstract val accessToken: String

    data class User(
        override val id: Long,
        override val accessToken: String
    ): VkCredentials()

    data class Group(
        override val id: Long,
        override val accessToken: String
    ): VkCredentials()
}
