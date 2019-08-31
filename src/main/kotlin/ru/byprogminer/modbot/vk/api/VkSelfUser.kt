package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.api.SelfUser
import ru.byprogminer.modbot.vk.VkAgent

class VkSelfUser
internal constructor(id: Long, agent: VkAgent): VkUser(id, agent), SelfUser
