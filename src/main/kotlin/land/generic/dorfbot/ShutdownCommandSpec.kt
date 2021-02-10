package land.generic.dorfbot

import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parser

class ShutdownCommandSpec : BindingContext() {

    private val grammar = object : Grammar<Unit>() {
        val prefix by literalToken("~shutdown")
        override val rootParser: Parser<Unit>
            get() = prefix use {}
    }


    override fun tryParse(text: String): ParseResult<Unit> {
        return grammar.tryParseToEnd(text)
    }
}