package com.qrvault.ui.navigation

object Routes {
    const val HOME = "home"
    const val ADD = "add?itemId={itemId}"
    const val ADD_ROUTE = "add"
    const val DETAILS = "details/{itemId}"
    const val VIEWER = "viewer/{itemId}"
    const val SETTINGS = "settings"
    const val PIN_SETUP = "pin_setup"
    const val CAMERA = "camera"

    fun add(itemId: Long): String = "add?itemId=$itemId"
    fun details(itemId: Long): String = "details/$itemId"
    fun viewer(itemId: Long): String = "viewer/$itemId"
}

object AddArg {
    const val ITEM_ID = "itemId"
}

object DetailArg {
    const val ITEM_ID = "itemId"
}

object ViewerArg {
    const val ITEM_ID = "itemId"
}