package pl.north93.pullrequest.automerge.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mu.KotlinLogging
import org.kohsuke.github.GHFileNotFoundException
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import pl.north93.pullrequest.automerge.config.MergerApplicationConfig
import pl.north93.pullrequest.automerge.worker.RunStepWorker
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PullRequestAutoMergeApplication(
    private val configFile: File
) {
    fun launch() {
        if (! configFile.exists()) {
            logger.warn("Config file {} doesn't exists. You can launch application with --copy-default-config to generate a new one", configFile.absolutePath)
            return
        }

        val mergerApplicationConfig = loadConfig()
        val github = with(mergerApplicationConfig.credentials){
            GitHub.connect(username, password)
        }

        val executorService = Executors.newFixedThreadPool(mergerApplicationConfig.concurrency)
        mergerApplicationConfig.repositories
            .mapToRepositories(github)
            .forEach {
                executorService.submit(RunStepWorker(mergerApplicationConfig, it))
            }

        executorService.shutdown()
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
    }

    private fun List<String>.mapToRepositories(gitHub: GitHub): List<GHRepository> = mapNotNull {
        try {
            gitHub.getRepository(it)
        } catch (e: GHFileNotFoundException) {
            logger.error("Repository {} doesn't exists", it, e)
            null
        }
    }

    private fun loadConfig(): MergerApplicationConfig {
        logger.info("Loading config from file {}", configFile)
        val objectMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())
        return objectMapper.readValue(configFile, MergerApplicationConfig::class.java)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}