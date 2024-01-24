package miragefairy2023.colormaker

import java.awt.Color
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.MouseInfo
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel
import javax.swing.JToggleButton
import javax.swing.Timer
import kotlin.math.roundToInt

@Suppress("UNNECESSARY_LATEINIT")
class PanelColorSlider : JPanel() {
    private lateinit var sliderR: PanelSliderField
    private lateinit var sliderG: PanelSliderField
    private lateinit var sliderB: PanelSliderField
    private lateinit var sliderH: PanelSliderField
    private lateinit var sliderS: PanelSliderField
    private lateinit var sliderV: PanelSliderField
    private val textField: ParsingTextField<Int>

    init {

        layout = GridBagLayout().also {
            it.columnWidths = intArrayOf(0, 0, 0, 0)
            it.rowHeights = intArrayOf(0, 0, 0, 0)
            it.columnWeights = doubleArrayOf(1.0, Double.MIN_VALUE, 1.0, Double.MIN_VALUE)
            it.rowWeights = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
        }

        add(PanelSliderField(0, 255).also {
            sliderR = it
            it.listeners += inProcessing@{
                if (isInProcessing) return@inProcessing
                setValue(Rgb(Color(sliderR.value, sliderG.value, sliderB.value)), it)
            }
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 0
            it.gridy = 0
            it.gridwidth = 2
            it.gridheight = 1
        })

        add(PanelSliderField(0, 255).also {
            sliderG = it
            it.listeners += inProcessing@{
                if (isInProcessing) return@inProcessing
                setValue(Rgb(Color(sliderR.value, sliderG.value, sliderB.value)), it)
            }
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 0
            it.gridy = 1
            it.gridwidth = 2
            it.gridheight = 1
        })

        add(PanelSliderField(0, 255).also {
            sliderB = it
            it.listeners += inProcessing@{
                if (isInProcessing) return@inProcessing
                setValue(Rgb(Color(sliderR.value, sliderG.value, sliderB.value)), it)
            }
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 0
            it.gridy = 2
            it.gridwidth = 2
            it.gridheight = 1
        })

        add(PanelSliderField(0, 360).also {
            sliderH = it
            it.listeners += inProcessing@{
                if (isInProcessing) return@inProcessing
                setValue(Hsv(sliderH.value, sliderS.value, sliderV.value), it)
            }
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 2
            it.gridy = 0
            it.gridwidth = 2
            it.gridheight = 1
        })

        add(PanelSliderField(0, 100).also {
            sliderS = it
            it.listeners += inProcessing@{
                if (isInProcessing) return@inProcessing
                setValue(Hsv(sliderH.value, sliderS.value, sliderV.value), it)
            }
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 2
            it.gridy = 1
            it.gridwidth = 2
            it.gridheight = 1
        })

        add(PanelSliderField(0, 100).also {
            sliderV = it
            it.listeners += inProcessing@{
                if (isInProcessing) return@inProcessing
                setValue(Hsv(sliderH.value, sliderS.value, sliderV.value), it)
            }
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 2
            it.gridy = 2
            it.gridwidth = 2
            it.gridheight = 1
        })

        val pattern = """[0-9a-fA-F]{6}""".toRegex()
        add(ParsingTextField({ it.trim().takeIf { s -> s matches pattern }?.toInt(16) }, { String.format("%06X", it and 0xffffff) }).also { c ->
            textField = c
            c.columns = 10
            c.listeners.add {
                if (isInProcessing) return@add
                setValue(Rgb(Color(it)), c)
            }
        }, GridBagConstraints().also {
            it.fill = GridBagConstraints.HORIZONTAL
            it.gridx = 0
            it.gridy = 3
            it.gridwidth = 1
            it.gridheight = 1
        })

        add(JToggleButton("Pick").also { c ->
            val timer = Timer(20) {
                try {
                    val location = MouseInfo.getPointerInfo().location
                    val createScreenCapture = Robot().createScreenCapture(Rectangle(location.x, location.y, 1, 1))
                    setValue(Rgb(Color(createScreenCapture.getRGB(0, 0))), c)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
            timer.isRepeats = true
            c.addActionListener {
                if (c.isSelected) {
                    timer.start()
                } else {
                    timer.stop()
                }
            }
            c.addComponentListener(object : ComponentAdapter() {
                override fun componentHidden(e: ComponentEvent) = timer.stop()
            })
        }, GridBagConstraints().also {
            it.fill = GridBagConstraints.HORIZONTAL
            it.gridx = 1
            it.gridy = 3
            it.gridwidth = 1
            it.gridheight = 1
        })
    }

    //

    private lateinit var value: ColorValue
    fun setValue(value: Color) = setValue(Rgb(value), null)
    fun getValue() = value.color

    private sealed interface ColorValue {
        val color: Color
        val r: Int
        val g: Int
        val b: Int
        val h: Int
        val s: Int
        val v: Int
    }

    private class Rgb(override val color: Color) : ColorValue {
        override val r = color.red
        override val g = color.green
        override val b = color.blue
        private val hsv = Color.RGBtoHSB(r, g, b, null)
        override val h = (hsv[0] * 360F).roundToInt()
        override val s = (hsv[1] * 100F).roundToInt()
        override val v = (hsv[2] * 100F).roundToInt()
    }

    private class Hsv(override val h: Int, override val s: Int, override val v: Int) : ColorValue {
        init {
            require(h in 0..360)
            require(s in 0..100)
            require(v in 0..100)
        }

        override val color = Color(Color.HSBtoRGB(h.toFloat() / 360F, s.toFloat() / 100F, v.toFloat() / 100F))
        override val r = color.red
        override val g = color.green
        override val b = color.blue
    }

    //

    val listeners = mutableListOf<(Color) -> Unit>()
    private var isInProcessing = false

    private fun setValue(value: ColorValue, source: Component?) {
        isInProcessing = true

        this.value = value
        if (source != textField) textField.setValue(value.color.rgb)
        if (source != sliderR) sliderR.value = value.r
        if (source != sliderG) sliderG.value = value.g
        if (source != sliderB) sliderB.value = value.b
        if (source != sliderH) sliderH.value = value.h
        if (source != sliderS) sliderS.value = value.s
        if (source != sliderV) sliderV.value = value.v
        listeners.forEach {
            try {
                it(value.color)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        isInProcessing = false
    }

    init {
        setValue(Color.white)
    }
}
