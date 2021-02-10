package land.generic.dorfbot

import com.github.h0tk3y.betterParse.parser.ParseResult
import kotlin.properties.ReadOnlyProperty

abstract class BindingContext {
    private val bindings: MutableMap<String, ParsingDelegate<*>> = mutableMapOf()
    fun <T : Any> bind(name: String, converter: (String) -> T): ReadOnlyProperty<Any?, T> =
        ParsingDelegate(name, converter).also {
            bindings[name] = it
        }

    fun bindInt(name: String) = bind(name) { it.toInt() }
    protected fun update(name: String, value: String) {
        bindings[name]?.parse(value)
    }

    abstract fun tryParse(text: String): ParseResult<Unit>
}