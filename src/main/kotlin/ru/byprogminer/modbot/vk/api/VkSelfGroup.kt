package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.api.SelfUser
import ru.byprogminer.modbot.vk.VkAgent

class VkSelfGroup
internal constructor(id: Long, agent: VkAgent): VkGroup(id, agent), SelfUser