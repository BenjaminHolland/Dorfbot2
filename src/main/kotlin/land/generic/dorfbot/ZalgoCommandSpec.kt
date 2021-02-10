package land.generic.dorfbot

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parser

class ZalgoCommandSpec : BindingContext() {
    private val grammar = object : Grammar<Unit>() {
        val prefix by literalToken("~zalgo")
        val ws by regexToken("\\s+")
        val word by regexToken(".*")
        override val rootParser: Parser<Unit>
            get() = -prefix * -ws * word use {
                update("text", this.text)
            }
    }
    val text by bind("text") { it }
    override fun tryParse(text: String): ParseResult<Unit> {
        return grammar.tryParseToEnd(text)
    }
}