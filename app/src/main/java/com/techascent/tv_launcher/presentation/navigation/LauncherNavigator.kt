package com.techascent.tv_launcher.presentation.navigation

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.LauncherEffect
import androidx.core.net.toUri

/** Turns one-off [LauncherEffect]s into system intents, keeping the activity free of navigation code. */
class LauncherNavigator(private val context: Context) {

    fun handle(effect: LauncherEffect) {
        when (effect) {
            is LauncherEffect.LaunchApp -> start(
                Intent(Intent.ACTION_MAIN).setComponent(
                    ComponentName(effect.packageName, effect.activityName),
                ),
            )
            is LauncherEffect.UninstallApp -> start(
                Intent(Intent.ACTION_DELETE, Uri.fromParts("package", effect.packageName, null)),
            )
            LauncherEffect.OpenInputs -> openInputs()
            LauncherEffect.OpenSettings -> start(Intent(Settings.ACTION_SETTINGS))
            LauncherEffect.OpenPlayStore -> openPlayStore()
        }
    }

    private fun openPlayStore() {
        val intent = context.packageManager.getLeanbackLaunchIntentForPackage("com.android.vending")
            ?: context.packageManager.getLaunchIntentForPackage("com.android.vending")
            ?: Intent(Intent.ACTION_VIEW, "market://search?q=apps&c=apps".toUri())
                .setPackage("com.android.vending")
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.store_unavailable, Toast.LENGTH_LONG).show()
        }
    }

    /** Opens the system input picker (HDMI etc.) like the stock launcher, then falls back to settings. */
    private fun openInputs() {
        val intent = listOf(VIEW_INPUTS, MITV_INPUT_POPUP, TV_INPUT_SETTINGS)
            .map(::Intent)
            .firstOrNull { it.resolveActivity(context.packageManager) != null }
            ?: Intent(Settings.ACTION_SETTINGS)
        start(intent)
    }

    private fun start(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // The target disappeared between listing and launching; nothing useful to do.
        }
    }

    private companion object {
        const val VIEW_INPUTS = "com.android.tv.action.VIEW_INPUTS"
        // Mi TV firmware ships its input picker under its own action instead of the AOSP one.
        const val MITV_INPUT_POPUP = "com.mitv.tvhome.atv.app.tv.INPUTSOURCE_POPUP"
        const val TV_INPUT_SETTINGS = "android.settings.TV_INPUT_SETTINGS"
    }
}
