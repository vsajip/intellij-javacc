package com.github.oowekyala.ijcc.lang.util

import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpression
import com.github.oowekyala.ijcc.lang.psi.ancestorOrSelf
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.intellij.lang.LanguageCommenters
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import org.intellij.lang.annotations.Language

/**
 * Base class offering utilities to parse and create tests.
 * Mostly copied from intellij-JavaCC.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccTestBase : LightCodeInsightFixtureTestCase(), ParseUtilsMixin {

    protected val fileName: String
        get() = "$testName.jjt"

    private val testName: String
        get() = camelOrWordsToSnake(getTestName(true))


    override fun getProject(): Project {
        return super.getProject()
    }

    protected fun replaceCaretMarker(text: String) = text.replace("/*caret*/", "<caret>")

    /**
     * Selects the caret with the given [id] from [this] grammar.
     * This allows reusing the same grammar for tests that depend
     * on caret position. Just write several caret markers like `/*caret[someId]*/`
     * in the grammar, then call this method to keep only the caret
     * marker with the given id and replace it with `<caret>`.
     */
    protected fun String.selectCaretMarker(id: String): String =
        replace("/*caret[$id]*/", "<caret>")
            .also {
                check(this != it) {
                    "No caret with id [$id] found"
                }
            }
            .replace(Regex("/\\*caret.*?\\*/"), "")


    protected fun checkByText(
        @Language("JavaCC") before: String,
        @Language("JavaCC") after: String,
        action: () -> Unit
    ) {
        configureByText(before)
        action()
        myFixture.checkResult(replaceCaretMarker(after))
    }

    protected fun openFileInEditor(path: String) {
        myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir(path))
    }

    private fun getVirtualFileByName(path: String): VirtualFile? =
        LocalFileSystem.getInstance().findFileByPath(path)


    protected inline fun <reified T : PsiElement> findElementInEditor(marker: String = "^"): T {
        val (element, data) = findElementWithDataAndOffsetInEditor<T>(marker)
        check(data.isEmpty()) { "Did not expect marker data" }
        return element
    }

    protected inline fun <reified T : PsiElement> findElementAndDataInEditor(marker: String = "^"): Pair<T, String> {
        val (element, data) = findElementWithDataAndOffsetInEditor<T>(marker)
        return element to data
    }

    protected inline fun <reified T : PsiElement> findElementWithDataAndOffsetInEditor(
        marker: String = "^"
    ): Triple<T, String, Int> {
        val elementsWithDataAndOffset = findElementsWithDataAndOffsetInEditor<T>(marker)
        check(elementsWithDataAndOffset.isNotEmpty()) { "No `$marker` marker:\n${myFixture.file.text}" }
        check(elementsWithDataAndOffset.size <= 1) { "More than one `$marker` marker:\n${myFixture.file.text}" }
        return elementsWithDataAndOffset.first()
    }

    protected inline fun <reified T : PsiElement> findElementsWithDataAndOffsetInEditor(
        marker: String = "^"
    ): List<Triple<T, String, Int>> {
        val commentPrefix = LanguageCommenters.INSTANCE.forLanguage(myFixture.file.language).lineCommentPrefix ?: "//"
        val caretMarker = "$commentPrefix$marker"
        val text = myFixture.file.text!!
        val result = mutableListOf<Triple<T, String, Int>>()
        var markerOffset = -caretMarker.length
        while (true) {
            markerOffset = text.indexOf(caretMarker, markerOffset + caretMarker.length)
            if (markerOffset == -1) break
            val data = text.drop(markerOffset).removePrefix(caretMarker).takeWhile { it != '\n' }.trim()
            val markerPosition = myFixture.editor.offsetToLogicalPosition(markerOffset + caretMarker.length - 1)
            val previousLine = LogicalPosition(markerPosition.line - 1, markerPosition.column)
            val elementOffset = myFixture.editor.logicalPositionToOffset(previousLine)
            val elementAtMarker = myFixture.file.findElementAt(elementOffset)!!
            val element = elementAtMarker.ancestorOrSelf<T>()
                ?: error("No ${T::class.java.simpleName} at ${elementAtMarker.text}")
            result.add(Triple(element, data, elementOffset))
        }
        return result
    }

    // got from intellij-kotlin,
    fun Document.extractMarkerOffset(project: Project, caretMarker: String = "<caret>"): Int {
        return extractMultipleMarkerOffsets(project, caretMarker).singleOrNull() ?: -1
    }

    private fun Document.extractMultipleMarkerOffsets(project: Project, caretMarker: String = "<caret>"): List<Int> {
        val offsets = ArrayList<Int>()

        runWriteAction {
            val text = StringBuilder(text)
            while (true) {
                val offset = text.indexOf(caretMarker)
                if (offset >= 0) {
                    text.delete(offset, offset + caretMarker.length)
                    setText(text.toString())

                    offsets += offset
                } else {
                    break
                }
            }
        }

        PsiDocumentManager.getInstance(project).commitAllDocuments()
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(this)

        return offsets
    }

    protected fun applyQuickFix(name: String) {
        val action = myFixture.findSingleIntention(name)
        myFixture.launchAction(action)
    }

    protected open fun configureByText(text: String, fileName: String = "dummy.jjt"): PsiFile =
        myFixture.configureByText(fileName, text)

    protected open fun configureByText(text: String): PsiFile = myFixture.configureByText(fileName, text)


    inline fun <reified R : JccExpansion> String.asExpansionOfType(): R =
        asExpansion().also { check(it is R) }.let { it as R }

    inline fun <reified R : JccRegularExpression> String.asRegex(): R = project.jccEltFactory.createRegex(this)


    companion object {

        @Language("JavaCC")
        const val DummyHeader = // changing spaces on that may break tests, don't do
            """
PARSER_BEGIN(Dummy)

package dummy.grammar;

public class Dummy {

}

PARSER_END(Dummy)
"""

        @JvmStatic
        fun camelOrWordsToSnake(name: String): String {
            if (' ' in name) return name.trim().replace(" ", "_")

            return name.split("(?=[A-Z])".toRegex()).joinToString("_", transform = String::toLowerCase)
        }
    }

}
