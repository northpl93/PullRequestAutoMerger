package pl.north93.pullrequest.automerge.worker

import mu.KotlinLogging
import org.kohsuke.github.GHRepository
import pl.north93.pullrequest.automerge.config.MergerApplicationConfig
import pl.north93.pullrequest.automerge.step.RunStep
import pl.north93.pullrequest.automerge.step.SelectPullRequests
import java.lang.Exception

class RunStepWorker(
    private val config: MergerApplicationConfig,
    private val repository: GHRepository
) : Runnable {
    override fun run() = try {
        val config = RunStepConfig(config.credentials)

        var step: RunStep? = SelectPullRequests(repository, this.config.pullRequestsFilter)
        while (step != null) {
            step = step.run(config)
        }
    } catch (e: Exception) {
        logger.error("Exception occurred while processing repository {}", repository.fullName, e)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}