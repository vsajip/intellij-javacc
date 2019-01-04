package com.github.oowekyala.ijcc.lang.refs

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.manipulators.JccIdentifierManipulator
import com.github.oowekyala.ijcc.util.filterMapAs
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult


/**
 * Poly reference to the multiple declarations of a JJTree node.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JjtNodePolyReference(psiElement: JccNodeClassOwner)
    : PsiPolyVariantReferenceBase<JccNodeClassOwner>(psiElement) {

    override fun getVariants(): Array<Any> = emptyArray()

    override fun multiResolve(incompleteCode: Boolean): Array<PsiEltResolveResult<JccNodeClassOwner>> {
        val myName = element.nodeSimpleName ?: return emptyArray()

        return element.containingFile
            .nonTerminalProductions
            .flatMap { it.descendantSequence(includeSelf = true) }
            .filterMapAs<JccNodeClassOwner>()
            .filter { it.nodeSimpleName == myName }
            .map { PsiEltResolveResult(it) }
            .toList().toTypedArray()
    }

    override fun getRangeInElement(): TextRange = element.nodeIdentifier!!.textRange.relativize(element.textRange)!!

    override fun handleElementRename(newElementName: String): PsiElement =
            JccIdentifierManipulator().handleContentChange(element.nodeIdentifier!!, newElementName)!!

}

data class PsiEltResolveResult<out T : PsiElement>(private val myElt: T) : ResolveResult {
    override fun getElement(): T = myElt

    override fun isValidResult(): Boolean = true
}