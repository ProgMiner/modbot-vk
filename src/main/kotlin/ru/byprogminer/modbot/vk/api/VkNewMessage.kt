package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.api.Chat
import ru.byprogminer.modbot.api.NewMessage
import ru.byprogminer.modbot.api.User
import ru.byprogminer.modbot.api.message.Attachment
import ru.byprogminer.modbot.vk.VkAgent
import java.security.SecureRandom
import java.util.stream.Collectors
import java.util.stream.Stream

data class VkNewMessage(
    override val text: String?,
    override val reply: VkRemoteMessage?,
    override val forwarded: List<VkRemoteMessage>,
    override val attachments: List<Attachment>,
    val lat: Double?,
    val long: Double?,
    val stickerId: Long?,
    val keyboard: Nothing?, // TODO
    val dontParseLinks: Boolean?,
    val disableMentions: Boolean?
): VkMessage, NewMessage {

    override fun sendTo(user: User): VkRemoteMessage = when (user) {
        is VkUser -> doSend(user)
        is VkGroup -> doSend(user)

        else -> throw IllegalArgumentException("user isn't VK user or group")
    }

    private fun doSend(user: VkUser): VkRemoteMessage = doSend(user.agent,
        makeArgs().also { it["user_id"] = user.id.toString() })

    private fun doSend(group: VkGroup): VkRemoteMessage = doSend(group.agent,
        makeArgs().also { it["peer_id"] = "-${group.id}" })

    override fun sendTo(chat: Chat): VkRemoteMessage {
        require(chat is VkChat) { "chat isn't VKChat" }

        val args = makeArgs()
        args["chat_id"] = chat.id.toString()

        return doSend(chat.agent, args)
    }

    private fun doSend(agent: VkAgent, args: Map<String, String>): VkRemoteMessage {
        val response = agent.api("messages.send", args)

        return TODO()
    }

    private fun makeArgs(): MutableMap<String, String> = Stream.of(
        "random_id" to SecureRandom().nextLong(),
        "message" to text,
        "lat" to lat,
        "long" to long,
        "attachment" to attachments.ifEmpty { null }, // TODO
        "reply_to" to reply?.id,
        "forward_messages" to forwarded.parallelStream()
            .filter { it != null }.map { it.id.toString() }
            .collect(Collectors.joining(",")).ifBlank { null },
        "sticker_id" to stickerId,
        "keyboard" to keyboard,
        "dont_parse_links" to dontParseLinks,
        "disable_mentions" to disableMentions
    ).filter { (_, value) -> value != null }
        .map { (key, value) -> key to (value as Any).toString() }
        .collect(Collectors.toMap(Pair<String, String>::first, Pair<String, String>::second))
}
