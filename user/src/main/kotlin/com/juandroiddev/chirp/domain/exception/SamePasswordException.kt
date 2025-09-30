package com.juandroiddev.chirp.domain.exception

class SamePasswordException(): RuntimeException("New password cannot be the same as the old password.")