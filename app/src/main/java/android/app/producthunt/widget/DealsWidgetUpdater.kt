package android.app.producthunt.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object DealsWidgetUpdater {
    suspend fun updateAll(context: Context) {
        DealsWidget().updateAll(context.applicationContext)
    }
}
