package com.techascent.tv_launcher.presentation.ui.options

/** Rejects a held key inherited from the view that opened a popup. */
internal class ConfirmKeyPressGuard {
    private var pressedKey: Int? = null

    fun shouldConsume(keyCode: Int, isDown: Boolean, repeatCount: Int): Boolean {
        if (isDown) {
            if (repeatCount != 0) return true
            pressedKey = keyCode
            return false
        }
        val hasFreshPress = pressedKey == keyCode
        pressedKey = null
        return !hasFreshPress
    }
}
