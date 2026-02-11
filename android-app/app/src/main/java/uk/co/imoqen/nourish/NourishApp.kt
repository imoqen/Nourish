package uk.co.imoqen.nourish

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The NourishApp application, required by Hilt.
 * This is declared in the AndroidManifest.xml
 */
@HiltAndroidApp
class NourishApp: Application()