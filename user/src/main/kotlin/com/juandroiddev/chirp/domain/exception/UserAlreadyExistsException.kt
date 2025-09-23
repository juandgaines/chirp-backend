package com.juandroiddev.chirp.domain.exception

import java.lang.RuntimeException

class UserAlreadyExistsException: RuntimeException (
    "A user with the given email or username already exists."
)