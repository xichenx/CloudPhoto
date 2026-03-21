package com.xichen.cloudphoto.model

/** Swift: `PhotoTimeKt.photoCreatedAtEpochMilliseconds(photo)` — dedicated file so the framework exposes a stable `*Kt` symbol. */
fun photoCreatedAtEpochMilliseconds(photo: Photo): Long = photo.createdAt.toEpochMilliseconds()
