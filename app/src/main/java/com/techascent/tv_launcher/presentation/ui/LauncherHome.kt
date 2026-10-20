package com.techascent.tv_launcher.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.AppsUiState
import com.techascent.tv_launcher.presentation.LauncherHeaderUiState
import com.techascent.tv_launcher.presentation.LauncherAppUiState
import com.techascent.tv_launcher.presentation.LauncherUiState
import com.techascent.tv_launcher.presentation.WeatherUiState
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing
import kotlinx.coroutines.launch

/**
 * Home screen: a collapsing header over a single favourites row, the All apps grid and the
 * Play Store panel. See [USE_WEATHER_HERO] for the weather art.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherHome(
    uiState: LauncherUiState,
    headerState: LauncherHeaderUiState,
    weatherState: WeatherUiState = WeatherUiState.Hidden,
    onAppClick: (LauncherAppUiState) -> Unit,
    onAppLongClick: (LauncherAppUiState) -> Unit,
    onInputClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPlayStoreClick: () -> Unit = {},
    onAddFavoriteClick: () -> Unit = {},
    onFavoriteLongClick: (LauncherAppUiState) -> Unit = onAppLongClick,
    onRemoveFavorite: (LauncherAppUiState) -> Unit = {},
    onUninstall: (LauncherAppUiState) -> Unit = {},
    onOptionsDismiss: () -> Unit = {},
) {
    // Start on the first favourite, or the add tile when the row is empty.
    val firstAppFocus = remember { FocusRequester() }
    val isPreview = LocalInspectionMode.current
    val content = uiState.appsState as? AppsUiState.Content
    val firstAppPackage = content?.favorites?.firstOrNull()?.packageName
    var hasInitialFocus by rememberSaveable { mutableStateOf(false) }
    val appsReady = content != null || uiState.appsState == AppsUiState.Empty
    LaunchedEffect(firstAppPackage, appsReady) {
        if (!isPreview && appsReady && !hasInitialFocus) {
            // The tile may not be attached on the first frame, so retry briefly.
            repeat(INITIAL_FOCUS_ATTEMPTS) {
                withFrameNanos { }
                if (runCatching { firstAppFocus.requestFocus() }.getOrDefault(false)) {
                    hasInitialFocus = true
                    return@LaunchedEffect
                }
            }
        }
    }
    val focusModifierFor: (LauncherAppUiState, Boolean) -> Modifier = { app, inDock ->
        val isFirst = app.packageName == firstAppPackage && inDock == content.favorites.isNotEmpty()
        if (isFirst) Modifier.focusRequester(firstAppFocus) else Modifier
    }
    val optionsFor: @Composable (LauncherAppUiState, Boolean) -> Unit = { app, favorite ->
        if (uiState.appOptions?.packageName == app.packageName && uiState.favoriteOptions == favorite) {
            AppOptionsPopup(
                app = app,
                favorite = favorite,
                onOpen = { onAppClick(app) },
                onRemove = { onRemoveFavorite(app) },
                onUninstall = { onUninstall(app) },
                onDismiss = onOptionsDismiss,
            )
        }
    }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    // derivedStateOf: recompose only when crossing the top, not on every scrolled pixel.
    val scrolledDown by remember { derivedStateOf { scrollState.value > 0 } }
    // Like the stock launcher, Back returns to the top of the home screen.
    BackHandler(enabled = scrolledDown) {
        scope.launch { scrollState.animateScrollTo(0) }
        runCatching { firstAppFocus.requestFocus() }
    }

    val defaultScrollSpec = LocalBringIntoViewSpec.current
    val headerScrollSpec = rememberCollapsingHeaderScrollSpec(scrollState, headerHeightPx)

    val weather = (weatherState as? WeatherUiState.Content)?.takeIf { USE_WEATHER_HERO }
    val collapseProgress = rememberHeaderCollapseProgress(scrollState) { headerHeightPx }

    Box(Modifier.fillMaxSize()) {
        if (weather != null) {
            // Behind the content, so tiles scroll over the art like the stock launcher's hero.
            WeatherArt(
                condition = weather.condition,
                isNight = weather.isNight,
                collapseProgress = collapseProgress,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(LauncherSpacing.WeatherArtWidth, LauncherSpacing.WeatherArtHeight),
            )
        }
        // Pivot scrolling that snaps the header open or closed; children get the default spec back.
        CompositionLocalProvider(LocalBringIntoViewSpec provides headerScrollSpec) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        top = with(density) { headerHeightPx.toDp() } + LauncherSpacing.Medium,
                        bottom = LauncherSpacing.Medium,
                    ),
                verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
            ) {
                CompositionLocalProvider(LocalBringIntoViewSpec provides defaultScrollSpec) {
                    if (appsReady) {
                        FavoritesDock(
                            favorites = content?.favorites.orEmpty(),
                            onAppClick = onAppClick,
                            onAppLongClick = onFavoriteLongClick,
                            onAddFavoriteClick = onAddFavoriteClick,
                            addFocusModifier = if (content?.favorites.isNullOrEmpty()) Modifier.focusRequester(firstAppFocus) else Modifier,
                            focusModifier = { focusModifierFor(it, true) },
                            selectedPackage = if (uiState.favoriteOptions) uiState.appOptions?.packageName else null,
                            options = { optionsFor(it, true) },
                        )
                    }
                    Column(
                        modifier = Modifier.padding(horizontal = LauncherSpacing.ScreenHorizontal),
                        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
                    ) {
                        when (val state = uiState.appsState) {
                            AppsUiState.Loading -> StatusText(text = stringResource(R.string.apps_loading))
                            is AppsUiState.Error -> StatusText(
                                text = stringResource(state.messageResId),
                                color = LauncherColors.TextError,
                            )
                            AppsUiState.Empty -> StatusText(text = stringResource(R.string.apps_empty))
                            is AppsUiState.Content -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = LauncherSpacing.Small),
                                ) {
                                    SectionTitle(stringResource(R.string.apps_heading))
                                    Text(
                                        text = stringResource(R.string.apps_favorite_hint),
                                        color = LauncherColors.TextTertiary,
                                        fontSize = 14.sp,
                                    )
                                }
                                if (state.isRefreshing || state.refreshErrorResId != null) {
                                    Text(
                                        text = stringResource(state.refreshErrorResId ?: R.string.apps_refreshing),
                                        color = if (state.refreshErrorResId != null) LauncherColors.TextError else LauncherColors.TextSecondary,
                                        fontSize = 14.sp,
                                    )
                                }
                                AllAppsGrid(state.apps) { app, tileModifier ->
                                    FavoriteAppTile(
                                        app = app,
                                        onClick = { onAppClick(app) },
                                        onLongClick = { onAppLongClick(app) },
                                        optionsOpen = !uiState.favoriteOptions && uiState.appOptions?.packageName == app.packageName,
                                        options = { optionsFor(app, false) },
                                        modifier = tileModifier.then(focusModifierFor(app, false)),
                                    )
                                }
                            }
                        }
                        //PlayStoreSection(onClick = onPlayStoreClick)
                    }
                }
            }
        }
        CollapsingLauncherHeader(
            state = headerState,
            scrollState = scrollState,
            contentFocus = if (appsReady) firstAppFocus else FocusRequester.Default,
            onInputClick = onInputClick,
            onSettingsClick = onSettingsClick,
            onActionsFocused = { scope.launch { scrollState.animateScrollTo(0) } },
            onHeightChanged = { headerHeightPx = it },
            weather = weather,
        )
        if (weather != null) {
            WeatherReadout(
                state = weather,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        end = LauncherSpacing.ScreenHorizontal,
                        top = LauncherSpacing.HeaderTopPadding + LauncherSpacing.ButtonSize + LauncherSpacing.Small,
                    )
                    .graphicsLayer {
                        val p = collapseProgress()
                        alpha = (1f - p * 1.6f).coerceAtLeast(0f)
                        translationY = -0.3f * p * headerHeightPx
                    },
            )
        }
    }
}

/** Single banner row using the same adaptive tile width as the app grid. */
@Composable
private fun FavoritesDock(
    favorites: List<LauncherAppUiState>,
    onAppClick: (LauncherAppUiState) -> Unit,
    onAppLongClick: (LauncherAppUiState) -> Unit,
    onAddFavoriteClick: () -> Unit,
    addFocusModifier: Modifier,
    focusModifier: (LauncherAppUiState) -> Modifier,
    selectedPackage: String?,
    options: @Composable (LauncherAppUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowState = rememberLazyListState()
    var addFocused by remember { mutableStateOf(false) }
    LaunchedEffect(favorites.size, addFocused) {
        if (addFocused) rowState.animateScrollToItem(favorites.size)
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
    ) {
        Box(Modifier.padding(horizontal = LauncherSpacing.ScreenHorizontal)) {
            SectionTitle(stringResource(R.string.favorites_heading))
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
        val tileWidth = gridTileLayout(maxWidth - LauncherSpacing.ScreenHorizontal * 2).second
        LazyRow(
            state = rowState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
            contentPadding = PaddingValues(
                horizontal = LauncherSpacing.ScreenHorizontal,
                vertical = LauncherSpacing.Medium,
            ),
        ) {
            items(favorites, key = { it.packageName }) { app ->
                FavoriteAppTile(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                    optionsOpen = selectedPackage == app.packageName,
                    options = { options(app) },
                    modifier = Modifier
                        .width(tileWidth)
                        .then(focusModifier(app)),
                )
            }
            item(key = "add_favorite") {
                AddFavoriteTile(
                    onClick = onAddFavoriteClick,
                    onFocusChanged = { addFocused = it },
                    modifier = Modifier.width(tileWidth).then(addFocusModifier),
                )
            }
        }
        }
    }
}

private const val INITIAL_FOCUS_ATTEMPTS = 10

/**
 * true: weather art, readout and a compact pill in the collapsed bar.
 * false: no weather on screen and no network requests.
 */
internal const val USE_WEATHER_HERO = true

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = LauncherColors.TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun StatusText(text: String, color: Color = LauncherColors.TextSecondary) {
    Text(
        text = text,
        color = color,
        fontSize = 18.sp,
        modifier = Modifier.padding(vertical = LauncherSpacing.Large),
    )
}
