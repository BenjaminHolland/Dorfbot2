import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.properties.ReadOnlyProperty

internal class StringConvertingDelegateTest{
    @Test
    fun weirdTest(){
        val delegate = ParsingDelegate("a"){ it.toInt()}
        val delegated by delegate
        delegate.parse("1234")
    }
}