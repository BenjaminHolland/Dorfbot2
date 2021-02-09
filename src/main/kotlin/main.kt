import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.parser.*
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import io.ktor.client.utils.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.bits.*

import kotlinx.cli.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.apache.log4j.BasicConfigurator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.file.*
import java.util.*
import kotlin.experimental.and


import kotlin.random.Random

fun getLogger(name: String): Logger = LoggerFactory.getLogger(name)
suspend fun main(argv: Array<String>) = coroutineScope {
    val discordToken by configString("discord_token")
    BasicConfigurator.configure()
    val logger = getLogger("main")

    logger.info("Starting Dorfbot")
    FileSystemHelpers.ensureCreated()
    val client = Kord(discordToken)
    val commands = listOf(
        YellCommandSpec(),
        RollCommandSpec(),
        BabbleCommandSpec(),
        ShutdownCommandSpec(),
        ZalgoCommandSpec()
    )

    logger.info("Initializing Dorfbot Memory")
    val memory = BabbleMemory()
    memory.restore()

    logger.info("Setting up event handlers")
    client.on<MessageCreateEvent> {
        try {
            if (message.author?.isBot != false) return@on
            if (!message.content.startsWith("~")) {
                memory.process(message.content)
                return@on
            }
            logger.info("Processing ${message.content}")
            for (context in commands) {
                when (context.tryParse(message.content)) {
                    is Parsed -> {
                        when (context) {
                            is YellCommandSpec -> doYellCommand(context, message.channel)
                            is RollCommandSpec -> doRollCommand(context, message.channel)
                            is BabbleCommandSpec -> doBabbleCommand(context, message.channel, memory)
                            is ZalgoCommandSpec -> doZalgoCommand(context, message.channel)
                            is ShutdownCommandSpec -> if (message.author != null && message.author!!.id == Snowflake(
                                    301806313397157888L
                                )
                            ) {
                                message.channel.createMessage("Goodbye friends!")
                                client.logout()
                            } else {
                                message.channel.createMessage("NO! YOU MAY NOT COMMAND ME!")
                            }
                        }
                    }
                    is ErrorResult -> {
                    }
                }
            }
        } catch (e: Throwable) {
            logger.error(e.message, e)
            throw Exception(e)
        }
    }

    logger.info("Starting unprompted babbling")
    launch {
        while (true) {
            val unpromptedDurationRange by configLongRange("babble.unprompted_duration_range")
            delay(unpromptedDurationRange.random())
            val spec = BabbleCommandSpec()
            spec.tryParse("~babble")
            doBabbleCommand(
                spec,
                client.getChannelOf<MessageChannel>(Snowflake(534943689340878851L))!!.asChannel(),
                memory
            )
        }
    }
    DorfbotConfiguration.logger.info("Logging in")
    client.login()
    logger.info("Shutting down")
    memory.save()
}

private val HEX_ARRAY = "0123456789ABCDEF"
fun ByteArray.bytesToHex(): String {
    val hexChars = CharArray(size * 2)
    for (j in indices) {
        val v: Int = (get(j).toInt() and 0xFF)
        hexChars[j * 2] = HEX_ARRAY[v ushr 4]
        hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
    }
    return String(hexChars)
}

suspend fun doZalgoCommand(context: ZalgoCommandSpec, channel: MessageChannelBehavior) {
    val zalgoCharacterRange = (0x300..0x36f)
    val zalgoAdditionRange = (2..6)
    val logger = getLogger("zalgo")
    val bytes = context.text.encodeToByteArray()
    logger.info("Original: ${bytes.bytesToHex()}")
    var zalgoContent = ""
    for(c in context.text){
        zalgoContent+=c
        for(i in (0..zalgoAdditionRange.random())){
            zalgoContent+=zalgoCharacterRange.random().toChar()
        }
    }
    val next = zalgoContent.encodeToByteArray()
    logger.info("Modified: ${next.bytesToHex()}")
    channel.createMessage(zalgoContent)
}


suspend fun doBabbleCommand(context: BabbleCommandSpec, channel: MessageChannelBehavior, babbleMemory: BabbleMemory) {
    if (context.isRemoving) {
        babbleMemory.remove(context.removedWord)
    } else {
        val punctuationRange by configIntRange("babble.punctuation_range")
        val words = babbleMemory.babble(Random.Default)
        val punctuation = "....!??,"
        val content: Sequence<String> = sequence {
            var nextPunctuationIn = punctuationRange.random()
            for (word in words) {
                yield(if (nextPunctuationIn == 0) word + punctuation.random() else word)
                nextPunctuationIn = if (nextPunctuationIn == 0) punctuationRange.random() else nextPunctuationIn - 1
            }
        }
        var message = content.joinToString(" ")
        if (message.last() !in punctuation) {
            message += punctuation.random(Random.Default)
        }
        channel.createMessage(message)
    }
}

suspend fun doRollCommand(context: RollCommandSpec, channel: MessageChannelBehavior) {
    if (context.sides > 100) {
        channel.createMessage("NO!")
    } else {
        val chaos = Random.Default
        val rolls = sequence {
            repeat(context.count) {
                yield(chaos.nextInt(1, context.sides + 1))
            }
        }.toList()
        val sum = rolls.sum()
        val str =
            channel.createEmbed {
                title = "Rolled $sum"
                description = rolls.joinToString(" ")
            }
    }

}

suspend fun doYellCommand(yellCommandSpec: YellCommandSpec, channel: MessageChannelBehavior) {
    val message = when (yellCommandSpec.sentiment) {
        YellCommandSpec.YellSentiment.POSITIVE -> "YEA!"
        YellCommandSpec.YellSentiment.NEGATIVE -> "NO!"
        YellCommandSpec.YellSentiment.NEUTRAL -> "YAH"
    }

    channel.createMessage(message)
}
