package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.model.AppOrganization
import com.techascent.tv_launcher.domain.model.AppSortOrder
import com.techascent.tv_launcher.domain.model.InstalledApp
import java.text.Collator
import java.util.Locale

data class LauncherAppSections(
    val all: List<InstalledApp>,
    val pinned: List<InstalledApp>,
    val recent: List<InstalledApp>,
    val hidden: List<InstalledApp>,
)

/** Rebuilt only when installed apps, local launch history, or organization preferences change. */
class ArrangeLauncherApps {
    operator fun invoke(
        apps: List<InstalledApp>,
        history: List<String>,
        settings: AppOrganization,
        locale: Locale = Locale.getDefault(),
    ): LauncherAppSections {
        val collator = Collator.getInstance(locale).apply { strength = Collator.SECONDARY }
        val alphabetical = Comparator<InstalledApp> { first, second ->
            collator.compare(first.label, second.label)
        }.thenBy { it.label.lowercase(Locale.ROOT) }
            .thenBy { it.packageName }
            .thenBy { it.activityName }
        val visible = apps.filterNot { it.packageName in settings.hiddenPackages }
        val byPackage = visible.associateBy { it.packageName }
        val launchOrder = history.distinct()
        val ranks = launchOrder.withIndex().associate { it.value to it.index }
        val order = when (settings.sortOrder) {
            AppSortOrder.Alphabetical -> alphabetical
            AppSortOrder.RecentlyUsed -> compareBy<InstalledApp> {
                ranks[it.packageName] ?: Int.MAX_VALUE
            }.then(alphabetical)
        }
        return LauncherAppSections(
            all = visible.sortedWith(order),
            pinned = settings.pinnedPackages.distinct().mapNotNull(byPackage::get),
            recent = launchOrder.mapNotNull(byPackage::get).take(5),
            hidden = apps.filter { it.packageName in settings.hiddenPackages }.sortedWith(alphabetical),
        )
    }
}
