package me.sudodios.orangeplayer.ui.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import me.sudodios.orangeplayer.utils.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.ui.components.*
import me.sudodios.orangeplayer.ui.components.menu.NestedPopupMenu
import me.sudodios.orangeplayer.ui.components.menu.dropDownMenu
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.utils.Utils.formatToCount
import kotlin.math.pow

@Composable
fun DialogEqualizer(
    expanded : Boolean,
    onDismissRequest: () -> Unit,
) {
    BaseDialog(expanded = expanded,onDismissRequest) {

        val equalizerOn = Player.Live.equalizerOn.value

        Column(modifier = Modifier.width(370.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ESwitch(
                modifier = Modifier.padding(12.dp).fillMaxWidth().clip(RoundedCornerShape(50)),
                isChecked = equalizerOn,
                textStyle = MaterialTheme.typography.titleMedium,
                text = "Equalizer",
                onClicked = {
                    if (equalizerOn) {
                        Player.turnOffEqualizer()
                    } else {
                        Player.turnOnEqualizer(Player.Live.equalizerPreset.value)
                    }
                }
            )
            EqualizerLayout(equalizerOn)
            EButton(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp).fillMaxWidth(),
                text = "Done !",
                centerText = true,
                backgroundColor = ColorBox.primary.copy(0.1f),
                textColor = ColorBox.primary,
                onClick = {
                    onDismissRequest.invoke()
                }
            )
        }
    }
}

@Composable
fun EqualizerLayout(
    switchTurnOnOff : Boolean,
) {

    val equalizerList = Player.getEqualizerList()
    val currentPresetName = Player.Live.equalizerPreset.value
    val disableAnimFraction = animateFloatAsState(if (!switchTurnOnOff) 0.3f else 1f, animationSpec = tween(400))
    var isOpenPresets by remember { mutableStateOf(false) }
    val amps = equalizerList[currentPresetName]!!

    Box(Modifier.fillMaxWidth().alpha(disableAnimFraction.value)) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EText(
                    text = "Preset",
                    fontSize = 13.sp,
                    color = ColorBox.text.copy(0.6f)
                )
                Row(
                    modifier = Modifier.padding(start = 16.dp).height(42.dp).weight(1f).clip(RoundedCornerShape(50))
                        .background(ColorBox.text.copy(0.1f)).clickable(enabled = switchTurnOnOff) {
                            isOpenPresets = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedText(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                        text = currentPresetName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorBox.text
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource("icons/arrow-down.svg"),
                        contentDescription = null,
                        tint = ColorBox.text.copy(0.8f)
                    )
                    Spacer(Modifier.padding(end = 12.dp))
                }
            }
            listFreq(amps)
            Spacer(Modifier.padding(10.dp))
            NestedPopupMenu(
                expanded = isOpenPresets,
                onDismissRequested = {isOpenPresets = false},
                menuItems = dropDownMenu {
                    equalizerList.keys.forEach {
                        item(id = it,it, selected = currentPresetName == it, onClicked = {
                            isOpenPresets = false
                            Player.turnOnEqualizer(it)
                        })
                    }
                }
            )
        }
    }

}

@Composable
private fun listFreq(amps: FloatArray) {
    Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp).fillMaxWidth()) {
        for (it in amps.indices) {
            val amp = amps[it]
            verticalAmp(
                mod = Modifier.weight(1f),
                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp).width(4.dp).height(190.dp),
                value = amp,
                getFreqText(it)
            )
        }
    }
}
@Composable
private fun verticalAmp(mod: Modifier, modifier: Modifier, value: Float, freq: String) {
    val calcOffsetY = (100f - (((value + 20) * 100) / 40f)) / 100f
    val animate = animateFloatAsState(calcOffsetY)
    Column(modifier = mod, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier) {
            val height = size.height
            val width = size.width
            drawIntoCanvas {
                //background
                drawRoundRect(
                    color = ColorBox.primary,
                    alpha = 0.1f,
                    topLeft = Offset(0f, 0f),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(2f.dp.toPx(), 2f.dp.toPx())
                )
                //foreground
                drawRoundRect(
                    color = ColorBox.primary,
                    topLeft = Offset(0f, animate.value * height),
                    size = Size(width, (1f - animate.value) * height),
                    cornerRadius = CornerRadius(2f.dp.toPx(), 2f.dp.toPx())
                )
            }
        }
        EText(text = freq, fontSize = 12.sp, color = ColorBox.text.copy(0.3f))
    }

}

private fun getFreqText(index: Int): String { return 2.0.pow(index + 5).toLong().formatToCount() }