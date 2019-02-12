package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccScopedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.nodeClass
import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import gnu.trove.THashSet

/**
 * Adds a gutter icon linking a production to a JJTree node class.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JjtreeNodeClassLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(elements: List<PsiElement>,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
                                          forNavigation: Boolean) {
        // prunes duplicates when collecting for nav
        val visited = if (forNavigation) THashSet<PsiElement>() else null
        for (element in elements) {
            val elt = element as? JjtNodeClassOwner ?: continue
            if (forNavigation && !visited!!.add(elt)) continue


            val psiClass = elt.nodeClass ?: continue

            val builder = NavigationGutterIconBuilder.create(JccIcons.GUTTER_NODE_CLASS).setTarget(psiClass)
                .setTooltipText("Click to navigate to class ${psiClass.name}")
                .setPopupTitle("Class ${psiClass.name}")

            val markerBearer = when (elt) {
                is JccNonTerminalProduction -> elt.nameIdentifier
                is JccScopedExpansionUnit   -> elt.jjtreeNodeDescriptor.nameIdentifier
                else                        -> null
            }?.leaf

            markerBearer?.let { builder.createLineMarkerInfo(it) }?.runIt { result.add(it) }
        }
    }


}