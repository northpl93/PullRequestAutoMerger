package pl.north93.pullrequest.automerge.strategy

import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.ObjectInserter
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.merge.MergeStrategy
import org.eclipse.jgit.merge.Merger
import pl.north93.pullrequest.automerge.strategy.TweakedResolveMerger.TWEAKED_RESOLVE_MERGER_CLASS
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType.methodType

class TweakedResolveMergeStrategy : MergeStrategy() {
    override fun getName(): String = "resolve"

    override fun newMerger(db: Repository?): Merger =
        CONSTRUCTOR_REPOSITORY_BOOLEAN.invokeExact(db, false) as Merger

    override fun newMerger(db: Repository?, inCore: Boolean): Merger =
        CONSTRUCTOR_REPOSITORY_BOOLEAN.invokeExact(db, inCore) as Merger

    override fun newMerger(inserter: ObjectInserter?, config: Config?): Merger =
        CONSTRUCTOR_INSERTER_CONFIG.invokeExact(inserter, config) as Merger

    companion object {
        private val LOOKUP = MethodHandles.lookup()

        private val CONSTRUCTOR_REPOSITORY_BOOLEAN: MethodHandle =
            LOOKUP.findConstructor(
                TWEAKED_RESOLVE_MERGER_CLASS,
                methodType(Void.TYPE, Repository::class.java, Boolean::class.java)
            ).asType(methodType(Merger::class.java, Repository::class.java, Boolean::class.java))

        private val CONSTRUCTOR_INSERTER_CONFIG: MethodHandle =
            LOOKUP.findConstructor(
                TWEAKED_RESOLVE_MERGER_CLASS,
                methodType(Void.TYPE, ObjectInserter::class.java, Config::class.java)
            ).asType(methodType(Merger::class.java, ObjectInserter::class.java, Config::class.java))
    }
}