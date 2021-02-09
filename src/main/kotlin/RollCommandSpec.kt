import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parser

class RollCommandSpec : BindingContext() {
    private val grammar = object : Grammar<Unit>() {
        val prefix by literalToken("~roll")
        val ws by regexToken("\\s+")
        val num by regexToken("\\d+")
        val d by literalToken("d")
        override val rootParser: Parser<Unit> = -prefix * -ws * num * -d * num map { (count, sides) ->
            update("sides", sides.text)
            update("count", count.text)
        }
    }
    val sides by bindInt("sides")
    val count by bindInt("count")

    override fun tryParse(text: String): ParseResult<Unit> {
        return grammar.tryParseToEnd(text)
    }
}