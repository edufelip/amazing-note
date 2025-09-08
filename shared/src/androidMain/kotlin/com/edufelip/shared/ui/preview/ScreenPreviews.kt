package com.edufelip.shared.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Phone • Light", device = Devices.PHONE, showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Phone • Dark", device = Devices.PHONE, showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Foldable • Light", device = Devices.FOLDABLE, showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Foldable • Dark", device = Devices.FOLDABLE, showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Tablet • Light", device = Devices.TABLET, showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Tablet • Dark", device = Devices.TABLET, showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ScreenPreviewsDarkLight
