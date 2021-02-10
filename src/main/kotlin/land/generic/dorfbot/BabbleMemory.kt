package land.generic.dorfbot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.Executors
import kotlin.random.Random

/**
 * API for interacting with the bots babble vocabulary and speech production.
 */
class BabbleMemory {
    private val logger = getLogger("Memory")

    private val throttleDuration by configLong("babble.throttle_duration")
    private val wordsPerParagraphRange by configIntRange("babble.words_per_paragraph_range")
    private val punctuationRange by configIntRange("babble.punctuation_range")
    private val saveInterval by configLong("babble.save_timing.interval")
    private val saveDelay by configLong("babble.save_timing.delay")

    private val babbleThread =
        Executors.newSingleThreadExecutor { Thread(it, "BabbleThread") }.asCoroutineDispatcher()
    private val babbleFlow = MutableSharedFlow<String>()
    private val memory: MutableSet<String> = mutableSetOf()
    private val memoryMutex = Mutex(false)
    private val babbleJob = Job()
    private val babbleScope = CoroutineScope(babbleThread + babbleJob)
    private val disallowedSymbols = ".,!:\'\"()[]=+!@#$%^&*`~_{}\\|<>?;"

    init {
        // Launch a coroutine that processes new messages and adds vocabulary to the list.
        babbleScope.launch {
            babbleFlow
                .debounce(throttleDuration) // If we're getting spammed, we wait a bit until it settles.
                .collect {
                    memoryMutex.withLock {
                        memory.addAll(it.split(" ")
                            .asSequence()
                            .filterNot { it.contains("@") } //Filter out anything that even resembles a mention
                            .filterNot { it.contains("://") } //Filter out anything that even resembles a link
                            .map { digestVocabularyWord(it) })
                    }
                }
        }
        // Launch a coroutine that routinely persists the vocabulary
        babbleScope.launch {
            ticker(saveInterval, saveDelay, babbleThread).consumeAsFlow().collect {
                logger.info("Starting automatic save")
                save()
            }
        }
    }

    private fun digestVocabularyWord(word: String): String {
        // Strip away "bad" symbols. This could probably be done better with a regex
        var cur = word
        for (c in disallowedSymbols) {
            cur = cur.replace(c.toString(), "")
        }
        return word.toLowerCase()
    }

    /**
     * Persist the current list of words to storage.
     *
     * May also load words that have been added to the file after the last call to save.
     */
    suspend fun save() {
        val file = FileSystemHelpers.configurationFolder.resolve("dorfbot_memory.txt")
        if (!Files.exists(file)) {
            Files.createFile(file)
        }
        val currentContents = Files.newBufferedReader(file).readLines().toSet()
        Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING).use {
            memoryMutex.withLock {
                logger.info("Saving ${memory.size} words")
                memory.addAll(currentContents)
                it.write(memory.joinToString("\n") { digestVocabularyWord(it) })
            }
        }
    }

    /**
     * Restores the words currently persisted to storage.
     */
    suspend fun restore() {
        val file = FileSystemHelpers.configurationFolder.resolve("dorfbot_memory.txt")
        if (Files.exists(file)) {
            memoryMutex.withLock {
                memory.clear()
                memory.addAll(Files.newBufferedReader(file).readLines())
            }
        }
    }

    /**
     * Submit a string for processing.
     */
    suspend fun process(newContent: String) {
        if (newContent.length > 1000) return
        babbleFlow.emit(newContent)
    }

    /**
     * Remove a word from memory.
     */
    suspend fun remove(word: String) {
        memoryMutex.withLock {
            memory.remove(word)
        }
    }

    /**
     * Generate a speech fragment.
     */
    suspend fun babble(chaos: Random): List<String> {
        val words = mutableListOf<String>()
        memoryMutex.withLock {
            for (i in (0..wordsPerParagraphRange.random())) {

                words.add(memory.random(chaos))
            }
        }
        return words
    }
}