package miragefairy2024.colormaker

import java.awt.Color
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document

class ParsingTextField<T> : JTextField {
    val parser: (String) -> T?
    val builder: (T) -> String

    constructor(parser: (String) -> T?, builder: (T) -> String) : super() {
        this.parser = parser
        this.builder = builder
        init()
    }

    constructor(parser: (String) -> T?, builder: (T) -> String, doc: Document?, text: String?, columns: Int) : super(doc, text, columns) {
        this.parser = parser
        this.builder = builder
        init()
    }

    constructor(parser: (String) -> T?, builder: (T) -> String, columns: Int) : super(columns) {
        this.parser = parser
        this.builder = builder
        init()
    }

    constructor(parser: (String) -> T?, builder: (T) -> String, text: String?, columns: Int) : super(text, columns) {
        this.parser = parser
        this.builder = builder
        init()
    }

    constructor(parser: (String) -> T?, builder: (T) -> String, text: String?) : super(text) {
        this.parser = parser
        this.builder = builder
        init()
    }

    private fun init() {

        // イベント登録
        run {

            // アクションリスナー登録
            addActionListener { parse() }

            // ドキュメントのリスナーに常にドキュメントリスナーを配置する
            run {
                val dl = object : DocumentListener {
                    override fun removeUpdate(e: DocumentEvent) = parse()
                    override fun insertUpdate(e: DocumentEvent) = parse()
                    override fun changedUpdate(e: DocumentEvent) = parse()
                }
                if (document != null) document.addDocumentListener(dl)
                addPropertyChangeListener("document") { e ->
                    e.oldValue.let { if (it is Document) it.removeDocumentListener(dl) }
                    e.newValue.let { if (it is Document) it.addDocumentListener(dl) }
                }
            }

        }

    }

    //

    var colorSuccess: Color = Color.decode("#BFFFBF")
    var colorError: Color = Color.decode("#FFBFBF")

    val listeners = mutableListOf<(T) -> Unit>()

    private fun parse() {
        val res = parser(text)
        if (res != null) {
            value = res
            listeners.forEach { it ->
                try {
                    it(res)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            setBackground(colorSuccess)
        } else {
            setBackground(colorError)
        }
    }

    //

    var value: T? = null
        private set

    fun setValue(value: T) = run { text = builder(value) }

}
