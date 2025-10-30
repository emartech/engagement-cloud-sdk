package com.emarsys.networking.clients.embedded.messaging.model

import kotlinx.serialization.Serializable

@Serializable
data class MetaData(
    val version: String,
    val design: DesignMetaData?
)

@Serializable
data class DesignMetaData(
    val fillColor: FillColors?,
    val text: TextMetaData?,
    val misc: Misc?
)

@Serializable
data class FillColors(
    val primaryBackground: String,
    val secondaryBackground: String,
    val buttonDefaultState: String,
    val buttonToggledState: String,
    val chipsButtonDefaultState: String,
    val chipsButtonDisabledState: String,
    val chipsButtonToggledState: String,
    val listMessageSelectedState: String,
    val dialogOverlayShade: String,
    val snackbarBackground: String
)

@Serializable
data class TextMetaData(
    val defaultFontType: String,
    val defaultFontColor: String,
    val defaultFontSize: Int,
    val selectedTabFontColor: String,
    val filterButtonToggledStateFontColor: String,
    val listMessageLeadDialogTextFontColor: String,
    val listMessageLeadDialogTextFontSize: Int,
    val listMessageDescriptionFontColor: String,
    val listMessageDescriptionFontSize: Int,
    val emptyStateTitleFontSize: Int,
    val emptyStateFontColor: String,
    val emptyStateDescriptionFontSize: Int,
    val emptyStateDescriptionFontColor: String,
    val chipsButtonDisabledStateFontColor: String,
    val chipsButtonSelectedStateFontColor: String
)

@Serializable
data class Misc(
    val dividerWidth: Int,
    val dividerColor: String,
    val dialogCornerRadius: Int,
    val filterButtonCornerRadius: Int,
    val chipsButtonCornerRadius: Int,
    val chipsButtonDefaultStateStrokeColor: String,
    val chipsButtonDefaultStateStrokeSize: Int
)

