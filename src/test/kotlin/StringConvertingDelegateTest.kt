import land.generic.dorfbot.ParsingDelegate
import org.junit.jupiter.api.Test

internal class StringConvertingDelegateTest{
    @Test
    fun weirdTest(){
        val delegate = ParsingDelegate("a"){ it.toInt()}
        val delegated by delegate
        delegate.parse("1234")
    }
}