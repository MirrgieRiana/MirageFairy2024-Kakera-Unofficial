package miragefairy2023.colormaker

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JPanel
import javax.swing.JSlider

class PanelSliderField(private val min: Int, private val max: Int) : JPanel() {
    val listeners = mutableListOf<SetValueEvent.() -> Unit>()

    class SetValueEvent(val value: Int, val source: Any?)

    private var _value = 0
    private var isInProcessing = false

    var value: Int
        get() = _value
        set(value) {
            setValue(value, null)
        }

    private fun setValue(value: Int, source: Any?) {
        isInProcessing = true

        _value = value
        listeners.forEach {
            try {
                SetValueEvent(value, source).it()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        isInProcessing = false
    }


    init {

        // レイアウト
        layout = GridBagLayout().also {
            it.columnWidths = intArrayOf(300, 50)
            it.rowHeights = intArrayOf(0)
            it.columnWeights = doubleArrayOf(0.0, 0.0)
            it.rowWeights = doubleArrayOf(0.0)
        }

        // スライダーを追加
        JSlider().also { c ->
            c.majorTickSpacing = 8
            c.paintTicks = true
            c.maximum = max
            c.addChangeListener {
                if (isInProcessing) return@addChangeListener
                setValue(c.value, c)
            }
            listeners += {
                if (source != c) c.value = value
            }
            add(c, GridBagConstraints().also {
                it.insets = Insets(0, 0, 0, 5)
                it.fill = GridBagConstraints.HORIZONTAL
                it.gridx = 0
                it.gridy = 0
            })
        }

        // 入力欄
        ParsingTextField(
            { it.trim().toIntOrNull()?.takeIf { a -> a in min..max } },
            { "$it" },
        ).also { c ->
            c.columns = 5
            c.listeners.add { value ->
                if (isInProcessing) return@add
                setValue(value, c)
            }
            listeners += {
                if (source != c) c.setValue(value)
            }
            add(c, GridBagConstraints().also {
                it.fill = GridBagConstraints.HORIZONTAL
                it.gridx = 1
                it.gridy = 0
            })
        }

        // 初期化
        value = 0

    }
}
