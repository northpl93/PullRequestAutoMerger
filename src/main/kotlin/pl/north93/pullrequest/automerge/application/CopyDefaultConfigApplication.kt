package pl.north93.pullrequest.automerge.application

import mu.KotlinLogging
import java.io.File

class CopyDefaultConfigApplication(
    private val configFile: File
) {
    fun launch() {
        if (configFile.exists()) {
            logger.warn("File {} already exists", configFile.absolutePath)
            return
        }

        logger.info("Copying default config to {}", configFile.absolutePath)
        configFile.outputStream().use {
            CopyDefaultConfigApplication::class.java.getResourceAsStream("/example-config.yml")?.copyTo(it)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}