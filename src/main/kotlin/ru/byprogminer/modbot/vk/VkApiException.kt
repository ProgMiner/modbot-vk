package ru.byprogminer.modbot.vk

import ru.byprogminer.modbot.ApiException
import ru.byprogminer.modbot.utility.LargeObject

class VkApiException: ApiException {

    companion object {

        private const val ERROR_MSG = "VK API error #%1\$s: %2\$s"

        private fun formatErrorMessage(error: LargeObject) = ERROR_MSG
            .format(error["error_code"], error["error_msg"])
    }

    override val error: LargeObject

    constructor(error: LargeObject): super(formatErrorMessage(error)) {
        this.error = error
    }

    constructor(cause: Throwable, error: LargeObject): super(formatErrorMessage(error), cause) {
        this.error = error
    }
}