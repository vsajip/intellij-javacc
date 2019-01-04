package com.github.oowekyala.ijcc.insight.jjtree

import com.github.oowekyala.ijcc.lang.psi.JccNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccScopedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.parentSequence
import com.github.oowekyala.ijcc.util.filterMapAs
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.psi.PsiElement

/**
 * Renders cells in [JjtreePartialDeclarationLineMarkerProvider]
 * popup (name and location string).
 *
 * @author Clément Fournier
 * @since 1.0
 */
class PartialDeclCellRenderer : DefaultPsiElementCellRenderer() {


    override fun getElementText(element: PsiElement?): String {

        return element?.let { it as JccNodeClassOwner }.let { owner ->
            when (owner) {
                is JccScopedExpansionUnit   -> "#${owner.name}"
                is JccNonTerminalProduction -> "${owner.name}()"
                else                        -> super.getElementText(element)
            }
        }
    }

    override fun getContainerText(element: PsiElement?, name: String?): String? {
        return when (element) {
            is JccScopedExpansionUnit   ->
                element.parentSequence(includeSelf = false)
                    .filterMapAs<JccNonTerminalProduction>()
                    .firstOrNull()
                    ?.let { "in ${it.name}()" }
            is JccNonTerminalProduction ->
                element.jjtreeNodeDescriptor.let {
                    if (it == null) ""
                    else "#${it.name}"
                }
            else                        -> ""
        }
    }
}