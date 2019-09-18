package ru.byprogminer.modbot.vk

import ru.byprogminer.modbot.vk.api.VkAttachmentUploader
import ru.byprogminer.modbot.vk.api.VkSelfUser

class VkUserActor(id: Long, accessToken: String): VkActor(accessToken) {

    override val attachmentUploader = VkAttachmentUploader(this)

    override val user = VkSelfUser(id, this)
}
