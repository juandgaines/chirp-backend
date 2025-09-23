package com.juandroiddev.chirp.domain.exception

import java.lang.RuntimeException

class PasswordEncodeException: RuntimeException(
    "Failed to encode the password."
)