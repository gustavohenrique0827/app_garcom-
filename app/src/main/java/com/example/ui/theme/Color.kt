package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

var isDarkGlobal by mutableStateOf(true)

val Ink: Color get() = if (isDarkGlobal) Color(0xFF14161C) else Color(0xFFF5F7FA)
val Ink2: Color get() = if (isDarkGlobal) Color(0xFF1C1F28) else Color(0xFFFFFFFF)
val Ink3: Color get() = if (isDarkGlobal) Color(0xFF252A36) else Color(0xFFEBEFF4)
val Paper: Color get() = if (isDarkGlobal) Color(0xFFF5F3EE) else Color(0xFF14161C)
val Brass = Color(0xFFE0A13B)
val BrassBright = Color(0xFFF4B955)
val Teal = Color(0xFF2DD4BF)
val Coral = Color(0xFFFF6B5C)
val Line: Color get() = if (isDarkGlobal) Color(0x14FFFFFF) else Color(0x1A000000)
val TextDim: Color get() = if (isDarkGlobal) Color(0x8CF5F3EE) else Color(0xBF14161C)
val TextDimmer: Color get() = if (isDarkGlobal) Color(0x59F5F3EE) else Color(0x7314161C)

val GtechPurple = Color(0xFF8B7CF0)
val GtechPurpleBright = Color(0xFFA698FF)

// Original values preserved for compatibility if needed
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
