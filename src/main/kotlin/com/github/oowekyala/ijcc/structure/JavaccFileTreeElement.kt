package com.github.oowekyala.ijcc.structure

import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import java.util.*

/**
 * Root of the structure view.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccFileTreeElement(file: JccFileImpl) : PsiTreeElementBase<JccFileImpl>(file) {

    private val declarations: List<StructureViewTreeElement>

    init {
        val result = mutableListOf<JavaccStructureViewElement>()
        // find declarations
        file.processDeclarations(DeclarationResolver {
            result.add(
                it
            )
        }, ResolveState.initial(), file, file)

        declarations = Collections.unmodifiableList(result)

    }

    override fun getChildrenBase(): Collection<StructureViewTreeElement> = declarations

    override fun getPresentableText(): String? = value!!.name

    private class DeclarationResolver(private val onFind: (JavaccStructureViewElement) -> Unit) : PsiScopeProcessor {

        override fun execute(psiElement: PsiElement, resolveState: ResolveState): Boolean {
            when (psiElement) {
                is JccNonTerminalProduction -> {
                    // TODO find children productions
                    onFind(NonTerminalStructureNode(psiElement))
                }
                is JccRegexprSpec           -> {
                    val regex = psiElement.regularExpression
                    if (regex is JccNamedRegularExpression && regex.nameIdentifier != null) onFind(
                        TerminalStructureLeaf(psiElement))
                }


            }

            return false
        }

        override fun <T> getHint(tKey: Key<T>): T? = null

        override fun handleEvent(event: PsiScopeProcessor.Event, o: Any?) = System.err.println(event.toString())
    }
}
