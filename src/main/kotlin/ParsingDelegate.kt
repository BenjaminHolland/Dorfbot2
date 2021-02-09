import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ParsingDelegate<T : Any>(val name: String, private val converter: (String) -> T) : ReadOnlyProperty<Any?, T> {
    private var holder: Optional<T> = Optional.empty()
    fun parse(value: String) {
        holder = Optional.of(converter(value))
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return holder.orElseThrow { Exception("No value set") }
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, T> {
        return this
    }
}