package com.techascent.tv_launcher.domain.model

enum class AppSortOrder { Alphabetical, RecentlyUsed }

data class AppOrganization(
    val pinnedPackages: List<String> = emptyList(),
    val hiddenPackages: Set<String> = emptySet(),
    val sortOrder: AppSortOrder = AppSortOrder.Alphabetical,
) {
    fun togglePin(packageName: String) = copy(
        pinnedPackages = if (packageName in pinnedPackages) pinnedPackages - packageName
        else pinnedPackages + packageName,
    )
}
