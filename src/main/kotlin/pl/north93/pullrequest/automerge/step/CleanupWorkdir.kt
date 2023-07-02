package pl.north93.pullrequest.automerge.step

import mu.KotlinLogging
import org.eclipse.jgit.api.Git
import pl.north93.pullrequest.automerge.worker.RunStepConfig
import java.io.File

class CleanupWorkdir(
    private val localRepository: Git
) : RunStep {
    override fun run(config: RunStepConfig): RunStep? {
        val repoRootDir = File(localRepository.repository.directory.parent)
        localRepository.close()

        logger.info("Deleting working directory {}", repoRootDir.absolutePath)
        repoRootDir.deleteRecursively()

        return null
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}