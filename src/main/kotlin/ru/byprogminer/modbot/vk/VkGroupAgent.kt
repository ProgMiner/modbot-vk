package ru.byprogminer.modbot.vk

import ru.byprogminer.modbot.vk.api.VkAttachmentUploader
import ru.byprogminer.modbot.vk.api.VkSelfGroup

class VkGroupAgent(id: Long, accessToken: String): VkAgent(accessToken) {

    override val attachmentUploader = VkAttachmentUploader(this)

    override val user = VkSelfGroup(id, this)
}
