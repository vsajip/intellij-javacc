package com.github.oowekyala.jjtx.util

import com.github.oowekyala.ijcc.util.prepend
import com.github.oowekyala.treeutils.TreeLikeAdapter

/**
 * @author Clément Fournier
 */
interface TreeOps<Self : TreeOps<Self>> {


    val adapter: TreeLikeAdapter<Self>


    fun children(): Sequence<Self> = adapter.getChildren(myself()).asSequence()
    fun descendants(): Sequence<Self> = children().flatMap { it.descendantsOrSelf() }
    fun descendantsOrSelf(): Sequence<Self> = descendants().prepend(myself())

    private fun myself(): Self = this as Self

}


