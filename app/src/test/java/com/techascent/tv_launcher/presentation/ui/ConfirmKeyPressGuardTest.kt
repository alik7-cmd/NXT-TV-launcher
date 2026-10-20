package com.techascent.tv_launcher.presentation.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfirmKeyPressGuardTest {
    @Test
    fun `inherited long press repeats and release are consumed`() {
        val guard = ConfirmKeyPressGuard()
        for (repeat in 1..20) {
            assertTrue(guard.shouldConsume(23, isDown = true, repeatCount = repeat))
        }
        assertTrue(guard.shouldConsume(23, isDown = false, repeatCount = 0))
    }

    @Test
    fun `fresh press and release pass through after inherited release`() {
        val guard = ConfirmKeyPressGuard()
        assertTrue(guard.shouldConsume(23, isDown = false, repeatCount = 0))
        repeat(2) {
            assertFalse(guard.shouldConsume(23, isDown = true, repeatCount = 0))
            assertFalse(guard.shouldConsume(23, isDown = false, repeatCount = 0))
        }
    }

    @Test
    fun `holding a fresh press suppresses repeats but allows its release`() {
        val guard = ConfirmKeyPressGuard()
        assertFalse(guard.shouldConsume(66, isDown = true, repeatCount = 0))
        assertTrue(guard.shouldConsume(66, isDown = true, repeatCount = 1))
        assertFalse(guard.shouldConsume(66, isDown = false, repeatCount = 0))
        assertTrue(guard.shouldConsume(66, isDown = false, repeatCount = 0))
    }

    @Test
    fun `release must match the fresh key press`() {
        val guard = ConfirmKeyPressGuard()
        assertFalse(guard.shouldConsume(23, isDown = true, repeatCount = 0))
        assertTrue(guard.shouldConsume(66, isDown = false, repeatCount = 0))
    }
}
