package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.Agent
import ru.byprogminer.modbot.api.SelfUser

class VkSelfUser
internal constructor(id: Long, agent: Agent): VkUser(id, agent), SelfUser
