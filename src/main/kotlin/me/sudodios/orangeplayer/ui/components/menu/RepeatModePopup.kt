package me.sudodios.orangeplayer.ui.components.menu

import androidx.compose.runtime.Composable
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.utils.Pref

@Composable
fun RepeatModePopup(
    show : Boolean = false,
    onDismissRequest : () -> Unit
) {

    val currentMode = Pref.repeatMode

    NestedPopupMenu(
        expanded = show,
        onDismissRequested = onDismissRequest,
        menuItems = dropDownMenu {
            item(id = "1","Repeat All","icons/repeat-all.svg", selected = currentMode == Player.REPEAT_MODE_ALL, onClicked = {
                Pref.repeatMode = Player.REPEAT_MODE_ALL
                onDismissRequest()
            })
            item(id = "2","Repeat One","icons/repeat-one.svg", selected = currentMode == Player.REPEAT_MODE_ONE, onClicked = {
                Pref.repeatMode = Player.REPEAT_MODE_ONE
                onDismissRequest()
            })
            item(id = "3","Shuffle","icons/shuffle.svg", selected = currentMode == Player.REPEAT_MODE_SHUFFLE, onClicked = {
                Pref.repeatMode = Player.REPEAT_MODE_SHUFFLE
                onDismissRequest()
            })
            item(id = "4","Stop","icons/stop.svg", selected = currentMode == Player.REPEAT_MODE_STOP, onClicked = {
                Pref.repeatMode = Player.REPEAT_MODE_STOP
                onDismissRequest()
            })
        }
    )

}