import org.apache.commons.configuration2.JSONConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import org.apache.commons.configuration2.event.ConfigurationEvent
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger
import java.util.concurrent.TimeUnit
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

val DorfbotConfiguration: JSONConfiguration =
    ReloadingFileBasedConfigurationBuilder(JSONConfiguration::class.java)
        .configure(
            Parameters()
                .hierarchical()
                .setFile(FileSystemHelpers.configurationFolder.resolve("dorfbot.json").toFile())
        ).also { configurationBuilder ->
            PeriodicReloadingTrigger(configurationBuilder.reloadingController, null, 1, TimeUnit.MINUTES).also {
                it.start()
            }
            configurationBuilder
        }.configuration.also { config->
            config.addEventListener(ConfigurationEvent.ANY) {
                val logger = getLogger("configuration")
                logger.info("${it.source}:${it.eventType}:${it.propertyName}:${it.propertyValue}")
            }
        }



class ConfigDelegatedInt(val path: String) : ReadOnlyProperty<Any?, Int> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return DorfbotConfiguration.getInt(path)
    }
}

fun configInt(path: String) = ConfigDelegatedInt(path)

class ConfigDelegatedString(val path: String) : ReadOnlyProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return DorfbotConfiguration.getString(path)
    }
}

fun configString(path: String) = ConfigDelegatedString(path)

class ConfigDelegatedLong(val path: String) : ReadOnlyProperty<Any?, Long> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
        return DorfbotConfiguration.getLong(path)
    }
}

fun configLong(path: String) = ConfigDelegatedLong(path)

class ConfigDelegatedBool(val path: String) : ReadOnlyProperty<Any?, Boolean> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return DorfbotConfiguration.getBoolean(path)
    }
}

fun configBool(path: String) = ConfigDelegatedBool(path)

class ConfigDelegatedLongRange(val path: String) : ReadOnlyProperty<Any?, LongRange> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): LongRange {
        return (DorfbotConfiguration.getLong("$path.min")..DorfbotConfiguration.getLong("$path.max"))
    }
}

fun configLongRange(path: String) = ConfigDelegatedLongRange(path)

class ConfigDelegatedIntRange(val path: String) : ReadOnlyProperty<Any?, IntRange> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): IntRange {
        return (DorfbotConfiguration.getInt("$path.min")..DorfbotConfiguration.getInt("$path.max"))
    }
}

fun configIntRange(path: String) = ConfigDelegatedIntRange(path)
