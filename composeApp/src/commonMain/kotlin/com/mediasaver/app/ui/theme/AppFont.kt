package com.mediasaver.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mediasaver.app.generated.resources.Res
import org.jetbrains.compose.resources.Font
import com.mediasaver.app.generated.resources.poppins_bold
import com.mediasaver.app.generated.resources.poppins_extrabold
import com.mediasaver.app.generated.resources.poppins_medium
import com.mediasaver.app.generated.resources.poppins_regular
import com.mediasaver.app.generated.resources.poppins_semibold

/** Poppins (SIL Open Font License) — bundled under composeResources/font. */
@Composable
fun appFontFamily(): FontFamily = FontFamily(
    Font(Res.font.poppins_regular, FontWeight.Normal),
    Font(Res.font.poppins_medium, FontWeight.Medium),
    Font(Res.font.poppins_semibold, FontWeight.SemiBold),
    Font(Res.font.poppins_bold, FontWeight.Bold),
    Font(Res.font.poppins_extrabold, FontWeight.ExtraBold)
)
