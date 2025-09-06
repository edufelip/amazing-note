package com.edufelip.shared

import platform.UIKit.UIDevice

actual fun platformName(): String = "iOS ${UIDevice.currentDevice.systemName()} ${UIDevice.currentDevice.systemVersion}"

