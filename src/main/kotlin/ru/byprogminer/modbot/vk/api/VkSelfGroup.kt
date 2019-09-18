package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.api.SelfUser
import ru.byprogminer.modbot.vk.VkActor

class VkSelfGroup
internal constructor(id: Long, actor: VkActor): VkGroup(id, actor), SelfUser
