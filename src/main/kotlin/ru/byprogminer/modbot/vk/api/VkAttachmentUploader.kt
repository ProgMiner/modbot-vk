package ru.byprogminer.modbot.vk.api

import ru.byprogminer.modbot.api.AttachmentUploader
import ru.byprogminer.modbot.api.message.Attachment
import ru.byprogminer.modbot.vk.VkAgent
import java.awt.image.RenderedImage
import java.net.URI

class VkAttachmentUploader(private val agent: VkAgent): AttachmentUploader {

    override fun photo(image: RenderedImage): Attachment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun video(uri: URI): Attachment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun audio(uri: URI): Attachment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun document(uri: URI): Attachment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
