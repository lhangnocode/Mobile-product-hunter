# Project Specs — Mobile Product Hunter

## Overview

**Product Hunter** is a native Android app (Kotlin + Jetpack Compose) that lets users track product prices across Vietnamese e-commerce platforms (Shopee, Lazada, Tiki). Users can set price-drop alerts, browse trending deals, and maintain a personal watchlist.

- **Package ID:** `android.app.producthunt`
- **Min SDK:** 31 (Android 12)
- **Target SDK:** 36
- **Version:** 1.0 (versionCode 1)
- **Language:** Kotlin 2.3.20
- **UI toolkit:** Jetpack Compose (BOM 2026.03.01)

---

## Tech Stack

| Layer | Library / Tool | Version |
|---|---|---|
| Build system | Gradle (KTS) | 9.3.1 |
| Android Gradle Plugin | AGP | 9.0.0 |
| Language | Kotlin | 2.3.20 |
| UI | Jetpack Compose (BOM) | 2026.03.01 |
| Material Design | Material3 | 1.4.0 |
| Navigation | Navigation Compose | 2.9.6 |
| DI | Hilt + KSP | 2.58 |
| Database | Room (KSP) | 2.8.4 |
| Networking | Retrofit + OkHttp | 3.0.0 / 5.3.2 |
| JSON | Gson + Converter | 2.13.2 / 3.0.0 |
| Preferences | DataStore (RxJava3) | 1.2.0 |
| Splash screen | Core SplashScreen | 1.2.0 |
| Fonts | Compose UI Text Google Fonts | 1.10.1 |
| Icons | Material Icons Extended | (BOM) |

**Dependency injection processor:** KSP (not KAPT).

---

## Project Structure

```
app/src/main/java/android/app/producthunt/
├── MainActivity.kt              # Entry point, Hilt entry point, NavGraph host
├── ProductHuntApplication.kt    # Application class
│
├── ui/
│   ├── navigation/
│   │   ├── Route.kt             # Route constants (all screen paths)
│   │   └── NavGraph.kt          # AppNavGraph composable, slide transitions
│   │
│   ├── screens/
│   │   ├── LoginScreen.kt       # Auth screen (email/password, terms checkbox)
│   │   ├── ProductDetailScreen.kt
│   │   └── main/
│   │       ├── MainScreen.kt    # Bottom-nav shell + PriceAlertsScreen (Home tab)
│   │       ├── TrendingScreen.kt
│   │       └── WishlistScreen.kt
│   │
│   ├── components/
│   │   ├── appbar/
│   │   │   ├── BackTopBar.kt    # Detail screen top bar (back + logo + avatar)
│   │   │   └── MainNavBar.kt    # Animated bottom nav bar
│   │   └── card/
│   │       ├── ProductGridCard.kt   # 2-col grid card (Trending)
│   │       ├── ProductMiniCard.kt   # Alert tracking card with progress bar
│   │       ├── SmartDealCard.kt     # Wishlist item card with status badge
│   │       └── CategoryChip.kt     # Filter chip for Wishlist categories
│   │
│   └── theme/
│       ├── Color.kt             # PH_* color tokens
│       ├── Type.kt              # Typography scale + PriceLarge/PriceMedium
│       ├── Icons.kt             # PHIcons object (Material icon aliases)
│       └── Theme.kt             # AndroidAppProductHuntTheme (light + dark)
│
└── (data / domain layers — not yet scaffolded)
```

---

## Navigation

### Route Map

| Route constant | Path string | Screen |
|---|---|---|
| `Route.LOGIN` | `login` | LoginScreen |
| `Route.SIGNUP` | `signup` | (placeholder) |
| `Route.FORGOT_PASSWORD` | `forgot_password` | (placeholder) |
| `Route.VERIFY_OTP` | `verify_otp?email={email}` | (placeholder) |
| `Route.RESET_PASSWORD` | `reset_password?email={email}&otp={otp}` | (placeholder) |
| `Route.HOME` | `home` | ProductDetailScreen (temp) |
| `Route.TRENDING` | `trending` | (via MainScreen tab) |
| `Route.WISHLIST` | `wishlist` | (via MainScreen tab) |
| `Route.ALERTS` | `alerts` | (placeholder tab) |
| `Route.PROFILE` | `profile` | (placeholder tab) |
| `Route.PRODUCT_DETAIL` | `product_detail` | ProductDetailScreen |
| `Route.MAIN` | `main` | MainScreen |

> **Note:** `NavGraph.kt` has an unresolved merge conflict between `HEAD` (includes `Route.MAIN → MainScreen`) and `origin/main` (includes `Route.HOME` and `Route.PRODUCT_DETAIL`). The `startDestination` is currently set to `Route.PRODUCT_DETAIL`.

### Transitions

All routes use a horizontal slide animation (Left on push, Right on pop) with a 300ms tween.

---

## Screens

### LoginScreen

- Email/username + password fields (Vietnamese labels)
- Password visibility toggle
- Terms & conditions checkbox
- Login button enabled only when: email non-empty, password ≥ 6 chars, terms accepted
- Navigates to `Route.HOME` on success, `Route.SIGNUP` on register tap

### ProductDetailScreen

Hardcoded demo content (Sony WH-1000XM5):

- `BackTopBar` — back + logo + avatar
- `ProductImageSection` — dark background image box with "BEST VALUE" badge
- `ProductBasicInfo` — star rating, title, description
- `BestPriceCard` — current price, discount %, "BUY NOW AT SHOPEE" button
- `PriceHistorySection` — 3M/6M toggle tabs, chart placeholder (ShowChart icon)
- `MarketComparisonSection` — Shopee / Lazada / Tiki comparison cards
- FABs: wishlist (dark circle) + set-alert (primary circle)

### MainScreen (bottom-nav shell)

Five tabs managed with `remember { mutableStateOf(MainTab.Home) }`:

| Tab | Component | Status |
|---|---|---|
| Home | `PriceAlertsScreen` | Implemented |
| Trending | `TrendingScreen` | Implemented |
| Wishlist | `WishlistScreen` | Implemented |
| Alerts | Placeholder `Text` | Stub |
| Profile | Placeholder `Text` | Stub |

#### PriceAlertsScreen (Home tab)

- Header: menu icon, app title, avatar
- "Price Alerts" heading + "Add New Alert" button
- Master notifications toggle switch
- Active alerts list using `ProductMiniCard`
- Upcoming alerts empty state

#### TrendingScreen

- Grid header with trending icon
- `LazyVerticalGrid` (2 columns) of `ProductGridCard`
- Hardcoded 8 deals with Vietnamese prices (`đ`)

#### WishlistScreen

- "Your Smart Deals" header
- Smart Alerts Active summary card
- Category filter chips: All Items / Dropped / Target Reached
- `LazyColumn` of `SmartDealCard`
- Hardcoded 4 items (Nike, MacBook, Yeezy, Ray-Ban)

---

## Components

### Cards

| Component | Props | Used in |
|---|---|---|
| `ProductGridCard` | title, currentPrice, originalPrice?, discount?, isWishlisted, onProductClick, onWishlistClick | TrendingScreen |
| `ProductMiniCard` | title, currentPrice, targetPrice, progress (0–1f), statusText, imageUrl?, isWishlisted, onEdit/Delete/Wishlist | PriceAlertsScreen |
| `SmartDealCard` | title, currentPrice, originalPrice?, targetPrice, badgeText?, statusLabel?, statusColor, statusBgColor, isMatched, onRemoveClick | WishlistScreen |
| `CategoryChip` | text, isSelected, onClick | WishlistScreen |

### App Bars

| Component | Props | Notes |
|---|---|---|
| `BackTopBar` | onBack | Back arrow + logo + avatar; used in ProductDetailScreen |
| `MainNavBar` | navController, showOnRoutes | AnimatedVisibility slide-in/out; auto-hides on non-main routes |

---

## Theme System

### Colors (`Color.kt`)

All tokens are prefixed `PH_`:

| Token | Hex | Purpose |
|---|---|---|
| `PH_Primary` | `#FF8A50` | Orange — primary brand color |
| `PH_Background` | `#FFF8F1` | Warm cream — light mode background |
| `PH_Surface` | `#FFFFFF` | Card / dialog surfaces |
| `PH_OnBackground` | `#2D2D2D` | Dark grey text |
| `PH_Price_Current` | `#2D2D2D` | Current price text |
| `PH_Price_Target` | `#D35400` | Target price / dark orange |
| `PH_Status_Success_*` | green family | Price matched, good deals |
| `PH_Status_Error_*` | red family | Price dropped alerts |
| `PH_Status_Warning_*` | yellow family | Waiting / neutral status |
| `PH_Progress_Bar` | `#00796B` | Progress fill (teal) |
| `PH_Progress_Bg` | `#E0F2F1` | Progress track |

Dark mode: background/surface uses `PH_OnBackground (#2D2D2D)` with white text. Dynamic color is disabled (`dynamicColor = false`).

### Typography (`Type.kt`)

Material3 scale using `FontFamily.SansSerif`. Custom extensions:

- `PriceLarge` — ExtraBold 20sp (SmartDealCard current price)
- `PriceMedium` — Bold 16sp (ProductMiniCard prices)

### Icons (`Icons.kt`)

`PHIcons` object wraps Material icon aliases:
`Menu, Notifications, NotificationsOutlined, Search, Add, Edit, Delete, Home, Trending, Wishlist, WishlistOutlined, Profile, History`

---

## Data Models (UI layer — no backend yet)

```kotlin
data class ProductDeal(          // TrendingScreen
    val id: String,
    val title: String,
    val currentPrice: String,
    val originalPrice: String?,
    val discount: String?,
    val isWishlisted: Boolean = false
)

data class WishlistItem(         // WishlistScreen
    val id: String,
    val title: String,
    val currentPrice: String,
    val originalPrice: String? = null,
    val targetPrice: String,
    val badgeText: String? = null,
    val statusLabel: String? = null,
    val statusColor: Color = PH_Status_Warning_Text,
    val statusBgColor: Color = PH_Status_Warning_Bg,
    val isMatched: Boolean = false
)
```

All screen data is currently hardcoded. No ViewModels, repositories, or Room entities have been implemented yet.

---

## Known Issues / Merge Conflicts

- `NavGraph.kt` and `Route.kt` both contain unresolved `<<<<<<< HEAD` / `>>>>>>> origin/main` merge conflict markers from merging `feat/trendingdeal_wishlist` and `feat/product-detail`. The app will not compile until these are resolved.
- `MainNavBar.kt` exists as a standalone animated bottom bar component but is **not wired into** `MainActivity` or `NavGraph` — `MainScreen.kt` uses its own inline `NavigationBar` instead.
- Screens for Alerts and Profile tabs are stubs (`Text` placeholders only).
- All product images are placeholders (`Box` with grey background or a `Text("Img")` label).
- Price history chart is a placeholder (`ShowChart` icon at 20% opacity).
- `Route.HOME` currently routes to `ProductDetailScreen` instead of a real home screen.

---

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

Requires Android SDK with API 31+ installed. Java 11 source/target compatibility.
