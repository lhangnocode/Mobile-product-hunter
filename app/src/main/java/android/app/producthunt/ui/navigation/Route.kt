package android.app.producthunt.ui.navigation

object Route {
    const val AUTH_GATE = "auth_gate"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"
    
    // Main Tabs
    const val FEED = "feed"
    const val SEARCH = "search"
    const val WISHLIST = "wishlist"
    
    // Other Screens
    const val PROFILE = "profile"
    const val SEARCH_HISTORY = "search_history"
    const val SETTINGS = "settings"
    const val ALERTS = "alerts"
    
    // Legacy/Removed navigation to Detail (keeping for now to avoid breaking refs until replaced by chat cards)
    const val PRODUCT_DETAIL = "product_detail"

    const val APP_INFORMATION = "app_information"
    const val AGENT_MANAGEMENT = "agent_management"
}
