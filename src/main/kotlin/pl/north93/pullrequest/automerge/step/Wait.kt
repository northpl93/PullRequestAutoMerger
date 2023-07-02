package pl.north93.pullrequest.automerge.step

import pl.north93.pullrequest.automerge.worker.RunStepConfig
import java.time.Duration

class Wait(
    private val waitTime: Duration,
    private val nextStep: () -> RunStep
) : RunStep {
    override fun run(config: RunStepConfig): RunStep {
        Thread.sleep(waitTime.toMillis())
        return nextStep()
    }
}