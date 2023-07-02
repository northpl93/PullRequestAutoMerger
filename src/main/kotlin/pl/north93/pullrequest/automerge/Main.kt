package pl.north93.pullrequest.automerge

import joptsimple.OptionParser
import joptsimple.OptionSet
import pl.north93.pullrequest.automerge.application.CopyDefaultConfigApplication
import pl.north93.pullrequest.automerge.application.PullRequestAutoMergeApplication
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val options: OptionSet = parser.parse(*args)

        val configFile: File = if (options.has("config")) {
            File(options.valueOf("config").toString())
        } else File("config.yml")

        if (options.has("copy-default-config")) {
            CopyDefaultConfigApplication(configFile).launch()
        } else {
            PullRequestAutoMergeApplication(configFile).launch()
        }
    }

    private val parser: OptionParser = OptionParser().also {
        it.accepts("config").withRequiredArg()
        it.accepts("copy-default-config")
    }
}