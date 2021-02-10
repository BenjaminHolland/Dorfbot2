package land.generic.dorfbot

import java.nio.file.Files
import java.nio.file.Path

object FileSystemHelpers {
    val configurationFolder = Path.of(
        if (System.getProperty("os.name").toUpperCase().contains("WIN")) {
            System.getenv("AppData") + "\\Dorfbot2"
        } else "/root/bots/dorf/"
    )

    fun ensureCreated() {
        if (!Files.exists(configurationFolder)) {
            Files.createDirectories(configurationFolder)
        }
    }
}