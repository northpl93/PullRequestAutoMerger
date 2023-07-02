package pl.north93.pullrequest.automerge.strategy

import javassist.ClassPool
import javassist.CtClass
import javassist.Loader
import javassist.bytecode.AccessFlag
import org.eclipse.jgit.diff.RawText

object TweakedResolveMerger {
    val TWEAKED_RESOLVE_MERGER_CLASS = createTweakedResolveMergerClass()

    private fun createTweakedResolveMergerClass(): Class<*> {
        val pool = ClassPool.getDefault()
        pool.importPackage(RawText::class.java.packageName)
        pool.importPackage(LinePerLineMerger::class.java.packageName)

        val tweakedClass = pool["org.eclipse.jgit.merge.ResolveMerger"]

        val contentMergeMethod = tweakedClass.getDeclaredMethod("contentMerge")
        contentMergeMethod.setBody("""{
        RawText baseText = $1 == null ? RawText.EMPTY_TEXT : getRawText($1.getEntryObjectId(), $4[T_BASE]);
		RawText ourText = $2 == null ? RawText.EMPTY_TEXT : getRawText($2.getEntryObjectId(), $4[T_OURS]);
		RawText theirsText = $3 == null ? RawText.EMPTY_TEXT : getRawText($3.getEntryObjectId(), $4[T_THEIRS]);
        return LinePerLineMerger.INSTANCE.contentMerge(baseText, ourText, theirsText);
        }
        """.trimIndent())

        tweakedClass.name = "org.eclipse.jgit.merge.TweakedResolveMerger"
        tweakedClass.constructors.forEach { it.modifiers = AccessFlag.PUBLIC }
        writeDebugClassFile(tweakedClass)

        val simpleClassLoader = Loader.Simple()
        return simpleClassLoader.invokeDefineClass(tweakedClass)
    }

    private fun writeDebugClassFile(tweakedClass: CtClass) {
        if (System.getProperty("debug").toBoolean()) {
            tweakedClass.writeFile()
        }
    }
}