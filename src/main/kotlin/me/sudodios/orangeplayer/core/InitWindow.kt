package me.sudodios.orangeplayer.core

import java.awt.*
import java.awt.event.WindowEvent
import java.io.InputStream
import javax.swing.*

class InitWindow {

    private fun getFont(size : Float) : Font {
        val `is`: InputStream = javaClass.classLoader.getResourceAsStream("fonts/goolazir.ttf")!!
        return Font.createFont(Font.TRUETYPE_FONT, `is`).deriveFont(size)
    }

    private val titleText = JLabel("Initialize Orange Player ...").apply {
        font = getFont(16f)
        horizontalAlignment = JLabel.CENTER
        verticalAlignment = JLabel.CENTER
        alignmentX = Component.CENTER_ALIGNMENT
        alignmentY = Component.CENTER_ALIGNMENT
        foreground = Color.orange
    }
    private val descText = JLabel("Please wait ...").apply {
        font = getFont(13f)
        horizontalAlignment = JLabel.CENTER
        verticalAlignment = JLabel.CENTER
        alignmentX = Component.CENTER_ALIGNMENT
        alignmentY = Component.CENTER_ALIGNMENT
        foreground = Color.white
    }

    private var jFrame = JFrame("OrangePlayer").apply {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        contentPane.background = Color.black
        isUndecorated = true

        val panel = JPanel()
        panel.background = Color.black
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        panel.add(Box.createVerticalGlue())
        panel.add(titleText)
        panel.add(Box.createRigidArea(Dimension(0, 5)))
        panel.add(descText)
        panel.add(Box.createVerticalGlue())

        contentPane.add(panel, BorderLayout.CENTER)

        val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
        val x: Int = (screenSize.width - 300) / 2
        val y: Int = (screenSize.height - 100) / 2
        setLocation(x, y)
        minimumSize = Dimension(300, 100)
        pack()
    }

    fun show () {
        jFrame.isVisible = true
    }

    fun hide() {
        jFrame.isVisible = false
    }

    fun changeStateText(input : String) {
        descText.text = input
    }

    fun notSupport() {
        titleText.text = "Orange Player Not Supported !"
        changeStateText("Close after 2 seconds")
        Thread.sleep(2000)
        jFrame.dispatchEvent(WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING))
    }

}