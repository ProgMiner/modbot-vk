package ru.byprogminer.modbot.vk

import ru.byprogminer.modbot.AbstractAgent
import ru.byprogminer.modbot.vk.api.VkAttachmentUploader
import ru.byprogminer.modbot.vk.api.VkSelfUser

class VkUserAgent(private val credentials: VkCredentials.User): AbstractAgent<VkAttachmentUploader>() {

    override val attachmentUploader = VkAttachmentUploader(credentials)

    override val user = VkSelfUser(credentials.id, this)

    // TODO long polling
}
