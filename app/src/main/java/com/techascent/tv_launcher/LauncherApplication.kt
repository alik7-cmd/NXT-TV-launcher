package com.techascent.tv_launcher

import android.app.Application
import com.techascent.tv_launcher.di.AppContainer

class LauncherApplication : Application() {
    val container by lazy { AppContainer(this) }
}
