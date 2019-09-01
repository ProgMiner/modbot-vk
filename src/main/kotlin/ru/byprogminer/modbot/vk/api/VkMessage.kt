package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.api.Message

interface VkMessage: Message {

    override val reply: VkRemoteMessage?
    override val forwarded: List<VkRemoteMessage>
}
