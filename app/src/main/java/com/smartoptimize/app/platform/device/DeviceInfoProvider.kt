package com.smartoptimize.app.platform.device

import android.os.Build
import com.smartoptimize.app.domain.model.DeviceInfo

/** Static device facts from [Build] (public API, no permission needed). */
class DeviceInfoProvider {

    fun read(): DeviceInfo = DeviceInfo(
        manufacturer = Build.MANUFACTURER ?: "",
        model = Build.MODEL ?: "",
        androidVersion = Build.VERSION.RELEASE ?: "",
        sdkInt = Build.VERSION.SDK_INT,
        supportedAbis = Build.SUPPORTED_ABIS?.toList() ?: emptyList()
    )
}
