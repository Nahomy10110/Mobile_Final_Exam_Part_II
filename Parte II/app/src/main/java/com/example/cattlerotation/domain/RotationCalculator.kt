package com.example.cattlerotation.domain

import com.example.cattlerotation.data.Rotation

enum class PaddockState { GREEN, RED, ORANGE }

object RotationCalculator {
    private const val DAY = 24 * 60 * 60 * 1000L
    const val RED_DAYS = 5
    const val ORANGE_DAYS = 15

    fun stateOnDate(rotations: List<Rotation>, date: Long): PaddockState {
        for (r in rotations) {
            if (date in r.startDate until redEnd(r)) return PaddockState.RED
            if (date in redEnd(r) until orangeEnd(r)) return PaddockState.ORANGE
        }
        return PaddockState.GREEN
    }

    fun activeRotationOn(rotations: List<Rotation>, date: Long): Rotation? =
        rotations.firstOrNull { date in it.startDate until orangeEnd(it) }

    fun redEnd(r: Rotation) = r.startDate + RED_DAYS * DAY
    fun orangeEnd(r: Rotation) = redEnd(r) + ORANGE_DAYS * DAY
}