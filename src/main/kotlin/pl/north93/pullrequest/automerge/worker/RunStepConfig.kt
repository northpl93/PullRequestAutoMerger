package pl.north93.pullrequest.automerge.worker

import java.io.File

data class RunStepConfig(
    private val credentials: Credentials
) {
    fun getWorkdir(): File =
        File("workdir").also {
            if (! it.exists()) {
                it.mkdir()
            }
        }

    fun getCredentials(): Credentials = credentials
}