package com.github.oowekyala.ijcc.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType

/** Lazy sequence of children. */
fun PsiElement.childrenSequence(reversed: Boolean = false): Sequence<PsiElement> = when (reversed) {
    false -> children.asSequence()
    true  -> {
        val children = children
        var i = children.size
        sequence {
            while (i > 0) {
                val child = children[--i]
                yield(child)
            }
        }
    }
}

/** Lazy sequence of all descendants.
 *
 * @param reversed If true the children are returned in the reversed order
 * @param depthFirst Perform a depth first traversal. Default is false, i.e. breadth-first
 */
fun PsiElement.descendantSequence(reversed: Boolean = false, depthFirst: Boolean = false): Sequence<PsiElement> {
    val children = childrenSequence(reversed)
    return when (depthFirst) {
        true  -> children.flatMap { sequenceOf(it) + it.descendantSequence(reversed, depthFirst) }
        false -> children + children.flatMap { it.descendantSequence(reversed, depthFirst) }
    }
}

/** Lazy sequence of siblings.
 *
 * @param forward If true the sequence iterates on the following siblings, otherwise on the previous siblings
 */
fun PsiElement.siblingSequence(forward: Boolean) =
        if (forward) generateSequence(this.nextSibling) { it.nextSibling }
        else generateSequence(this.prevSibling) { it.prevSibling }

/** Returns true if the node's token type is [TokenType.WHITE_SPACE]. */
val PsiElement.isWhitespace: Boolean
    get() = node.elementType == TokenType.WHITE_SPACE

val PsiElement.prevSiblingNoWhitespace: PsiElement?
    get() = siblingSequence(forward = false).firstOrNull { !it.isWhitespace }

val PsiElement.nextSiblingNoWhitespace: PsiElement?
    get() = siblingSequence(forward = true).firstOrNull { !it.isWhitespace }

val PsiElement.lastChildNoWhitespace: PsiElement?
    inline get() = childrenSequence(reversed = true).firstOrNull { !it.isWhitespace }

/** Parent sequence, stopping at the file node. */
fun PsiElement.parentSequence(includeSelf: Boolean) =
        generateSequence(if (includeSelf) this else parent) { it.parent }.takeWhile { it !is PsiDirectory }


val PsiElement.textRangeInParent: TextRange
    get() {
        val offset = startOffsetInParent
        return TextRange(offset, offset + textLength)
    }

fun PsiElement.innerRange(from: Int = 0, endOffset: Int = 0): TextRange = TextRange(from, textLength - endOffset)


// constrain the hierarchies to be the same to avoid some confusions

fun JccRegularExpression.safeReplace(regex: JccRegularExpression) = replace(regex)
fun JccExpansionUnit.safeReplace(regex: JccExpansionUnit) = replace(regex)
fun JccRegexpElement.safeReplace(regex: JccRegexpElement) = replace(regex)
