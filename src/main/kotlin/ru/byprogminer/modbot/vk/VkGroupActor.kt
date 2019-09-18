package ru.byprogminer.modbot.vk

import ru.byprogminer.modbot.vk.api.VkAttachmentUploader
import ru.byprogminer.modbot.vk.api.VkSelfGroup

class VkGroupActor(id: Long, accessToken: String): VkActor(accessToken) {

    override val attachmentUploader = VkAttachmentUploader(this)

    override val user = VkSelfGroup(id, this)
}
