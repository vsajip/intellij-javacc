package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.pom.Navigatable

/**
 * One element of the structure view. This class is used for all elements, regardless of their type.
 *
 * TODO represent synthetic members as non navigatable?
 *
 */
class JccStructureTreeElement(val element: JccPsiElement,
                              children: List<JccStructureTreeElement>)
    : StructureViewTreeElement, SortableTreeElement, Navigatable by element {

    constructor(element: JccPsiElement) : this(element, emptyList())

    private val myChildren: Array<JccStructureTreeElement> = children.toTypedArray()

    override fun getChildren(): Array<out TreeElement> = myChildren

    override fun getValue(): Any = element

    override fun getAlphaSortKey(): String = when (element) {
        is JccOptionSection     -> "aaaaaaa"
        is JccParserDeclaration -> "aaaaaaZ"
        is JccTokenManagerDecls -> "aaaaaZZ"
        is JccRegexProduction   -> "aaaaZZZ"
        else                    -> element.presentableText
    }


    override fun getPresentation(): ItemPresentation = element.presentationForStructure

}