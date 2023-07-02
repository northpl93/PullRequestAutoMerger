package pl.north93.pullrequest.automerge.step

import pl.north93.pullrequest.automerge.worker.RunStepConfig

interface RunStep {
    fun run(config: RunStepConfig): RunStep?
}