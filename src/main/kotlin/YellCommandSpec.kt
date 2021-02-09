import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parser

class YellCommandSpec : BindingContext() {
    enum class YellSentiment {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    private val grammar = object : Grammar<Unit>() {
        val prefix by literalToken("~yell")
        val sentiment by regexToken("positive|negative|\\+|-")
        val ws by regexToken("\\s+")
        override val rootParser: Parser<Unit>
            get() = -prefix * optional(-ws * sentiment) map { sentiment -> update("sentiment", sentiment?.text ?: "") }
    }

    val sentiment by bind("sentiment") {
        when (it) {
            "positive", "+" -> YellSentiment.POSITIVE
            "negative", "-" -> YellSentiment.NEGATIVE
            else -> YellSentiment.NEUTRAL
        }
    }

    override fun tryParse(text: String): ParseResult<Unit> {
        return grammar.tryParseToEnd(text)
    }
}