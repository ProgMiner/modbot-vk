package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.api.SelfUser
import ru.byprogminer.modbot.vk.VkActor

class VkSelfUser
internal constructor(id: Long, actor: VkActor): VkUser(id, actor), SelfUser
