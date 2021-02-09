import java.nio.file.Files
import java.nio.file.Path

object FileSystemHelpers {
    val configurationFolder = Path.of(
        if (System.getProperty("os.name").toUpperCase().contains("WIN")) {
            System.getenv("AppData") + "\\Dorfbot2"
        } else System.getenv("user.home") + "/Library/Application Support/Dorfbot2"
    )

    fun ensureCreated() {
        if (!Files.exists(configurationFolder)) {
            Files.createDirectories(configurationFolder)
        }
    }
}