package ru.byprogminer.modbot.vk

import ru.byprogminer.modbot.vk.api.VkAttachmentUploader
import ru.byprogminer.modbot.vk.api.VkSelfUser

class VkUserAgent(id: Long, accessToken: String): VkAgent(accessToken) {

    override val attachmentUploader = VkAttachmentUploader(this)

    override val user = VkSelfUser(id, this)

    // TODO long polling
}
