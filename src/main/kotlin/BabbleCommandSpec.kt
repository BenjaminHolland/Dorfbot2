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

class BabbleCommandSpec : BindingContext() {

    private val grammar = object : Grammar<Unit>() {
        val prefix by literalToken("~babble")
        val removePrefix by literalToken("remove word")
        val ws by regexToken("\\s+")
        val word by regexToken(".*")
        override val rootParser: Parser<Unit>
            get() = -prefix * optional(-ws * -removePrefix * -ws * word) map {
                update("isRemoving", it?.text ?: "")
                update("removedWord", it?.text ?: "")
            }
    }

    val isRemoving by bind("isRemoving") { it != "" }
    val removedWord by bind("removedWord") { it }
    override fun tryParse(text: String): ParseResult<Unit> {
        return grammar.tryParseToEnd(text)
    }
}