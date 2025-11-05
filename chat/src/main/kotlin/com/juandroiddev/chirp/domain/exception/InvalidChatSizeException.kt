package com.juandroiddev.chirp.domain.exception

import java.lang.RuntimeException

class InvalidChatSizeException: RuntimeException(
    "There must be at least 2 participants in a chat."
)