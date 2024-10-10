package me.sudodios.orangeplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

object Fonts {

    private val WeightMedium = FontWeight.Medium
    private val WeightRegular = FontWeight.Normal
    
    private val BodyLargeLineHeight = 24.0.sp
    private val BodyLargeSize = 16.sp
    private val BodyLargeTracking = 0.5.sp
    private val BodyLargeWeight = WeightRegular
    private val BodyMediumLineHeight = 20.0.sp
    private val BodyMediumSize = 14.sp
    private val BodyMediumTracking = 0.2.sp
    private val BodyMediumWeight = WeightRegular
    private val BodySmallLineHeight = 16.0.sp
    private val BodySmallSize = 12.sp
    private val BodySmallTracking = 0.4.sp
    private val BodySmallWeight = WeightRegular
    private val DisplayLargeLineHeight = 64.0.sp
    private val DisplayLargeSize = 57.sp
    private val DisplayLargeTracking = (-0.2).sp
    private val DisplayLargeWeight = WeightRegular
    private val DisplayMediumLineHeight = 52.0.sp
    private val DisplayMediumSize = 45.sp
    private val DisplayMediumTracking = 0.0.sp
    private val DisplayMediumWeight = WeightRegular
    private val DisplaySmallLineHeight = 44.0.sp
    private val DisplaySmallSize = 36.sp
    private val DisplaySmallTracking = 0.0.sp
    private val DisplaySmallWeight = WeightRegular
    private val HeadlineLargeLineHeight = 40.0.sp
    private val HeadlineLargeSize = 32.sp
    private val HeadlineLargeTracking = 0.0.sp
    private val HeadlineLargeWeight = WeightRegular
    private val HeadlineMediumLineHeight = 36.0.sp
    private val HeadlineMediumSize = 28.sp
    private val HeadlineMediumTracking = 0.0.sp
    private val HeadlineMediumWeight = WeightRegular
    private val HeadlineSmallLineHeight = 32.0.sp
    private val HeadlineSmallSize = 24.sp
    private val HeadlineSmallTracking = 0.0.sp
    private val HeadlineSmallWeight = WeightRegular
    private val LabelLargeLineHeight = 20.0.sp
    private val LabelLargeSize = 14.sp
    private val LabelLargeTracking = 0.1.sp
    private val LabelLargeWeight = WeightMedium
    private val LabelMediumLineHeight = 16.0.sp
    private val LabelMediumSize = 12.sp
    private val LabelMediumTracking = 0.5.sp
    private val LabelMediumWeight = WeightMedium
    private val LabelSmallLineHeight = 16.0.sp
    private val LabelSmallSize = 11.sp
    private val LabelSmallTracking = 0.5.sp
    private val LabelSmallWeight = WeightMedium
    private val TitleLargeLineHeight = 28.0.sp
    private val TitleLargeSize = 22.sp
    private val TitleLargeTracking = 0.0.sp
    private val TitleLargeWeight = WeightRegular
    private val TitleMediumLineHeight = 24.0.sp
    private val TitleMediumSize = 16.sp
    private val TitleMediumTracking = 0.2.sp
    private val TitleMediumWeight = WeightMedium
    private val TitleSmallLineHeight = 20.0.sp
    private val TitleSmallSize = 14.sp
    private val TitleSmallTracking = 0.1.sp
    private val TitleSmallWeight = WeightMedium
    
    val mainFont = FontFamily(
        Font(
            resource = "fonts/goolazir.ttf"
        )
    )

    val numbersFont = FontFamily(
        Font(
            resource = "fonts/sono-regular.ttf"
        )
    )

    @Composable
    fun getTypography () : Typography {
        return MaterialTheme.typography.copy(
            displayLarge = TextStyle(
                fontFamily = mainFont,
                fontWeight = DisplayLargeWeight,
                fontSize = DisplayLargeSize,
                lineHeight = DisplayLargeLineHeight,
                letterSpacing = DisplayLargeTracking,
            ),
            displayMedium = TextStyle(
                fontFamily = mainFont,
                fontWeight = DisplayMediumWeight,
                fontSize = DisplayMediumSize,
                lineHeight = DisplayMediumLineHeight,
                letterSpacing = DisplayMediumTracking,
            ),
            displaySmall = TextStyle(
                fontFamily = mainFont,
                fontWeight = DisplaySmallWeight,
                fontSize = DisplaySmallSize,
                lineHeight = DisplaySmallLineHeight,
                letterSpacing = DisplaySmallTracking,
            ),
            headlineLarge = TextStyle(
                fontFamily = mainFont,
                fontWeight = HeadlineLargeWeight,
                fontSize = HeadlineLargeSize,
                lineHeight = HeadlineLargeLineHeight,
                letterSpacing = HeadlineLargeTracking,
            ),
            headlineMedium = TextStyle(
                fontFamily = mainFont,
                fontWeight = HeadlineMediumWeight,
                fontSize = HeadlineMediumSize,
                lineHeight = HeadlineMediumLineHeight,
                letterSpacing = HeadlineMediumTracking,
            ),
            headlineSmall = TextStyle(
                fontFamily = mainFont,
                fontWeight = HeadlineSmallWeight,
                fontSize = HeadlineSmallSize,
                lineHeight = HeadlineSmallLineHeight,
                letterSpacing = HeadlineSmallTracking,
            ),
            titleLarge = TextStyle(
                fontFamily = mainFont,
                fontWeight = TitleLargeWeight,
                fontSize = TitleLargeSize,
                lineHeight = TitleLargeLineHeight,
                letterSpacing = TitleLargeTracking,
            ),
            titleMedium = TextStyle(
                fontFamily = mainFont,
                fontWeight = TitleMediumWeight,
                fontSize = TitleMediumSize,
                lineHeight = TitleMediumLineHeight,
                letterSpacing = TitleMediumTracking,
            ),
            titleSmall = TextStyle(
                fontFamily = mainFont,
                fontWeight = TitleSmallWeight,
                fontSize = TitleSmallSize,
                lineHeight = TitleSmallLineHeight,
                letterSpacing = TitleSmallTracking,
            ),
            bodyLarge = TextStyle(
                fontFamily = mainFont,
                fontWeight = BodyLargeWeight,
                fontSize = BodyLargeSize,
                lineHeight = BodyLargeLineHeight,
                letterSpacing = BodyLargeTracking,
            ),
            bodyMedium = TextStyle(
                fontFamily = mainFont,
                fontWeight = BodyMediumWeight,
                fontSize = BodyMediumSize,
                lineHeight = BodyMediumLineHeight,
                letterSpacing = BodyMediumTracking,
            ),
            bodySmall = TextStyle(
                fontFamily = mainFont,
                fontWeight = BodySmallWeight,
                fontSize = BodySmallSize,
                lineHeight = BodySmallLineHeight,
                letterSpacing = BodySmallTracking,
            ),
            labelLarge = TextStyle(
                fontFamily = mainFont,
                fontWeight = LabelLargeWeight,
                fontSize = LabelLargeSize,
                lineHeight = LabelLargeLineHeight,
                letterSpacing = LabelLargeTracking,
            ),
            labelMedium = TextStyle(
                fontFamily = mainFont,
                fontWeight = LabelMediumWeight,
                fontSize = LabelMediumSize,
                lineHeight = LabelMediumLineHeight,
                letterSpacing = LabelMediumTracking,
            ),
            labelSmall = TextStyle(
                fontFamily = mainFont,
                fontWeight = LabelSmallWeight,
                fontSize = LabelSmallSize,
                lineHeight = LabelSmallLineHeight,
                letterSpacing = LabelSmallTracking,
            )
        )
    }


}