/* Generated by JJTricks on Thu Jan 01 01:00:00 CET 1970 -- Not intended for manual editing. */

package com.exprs.ast;

import com.jjtx.exprs.Token;

import com.exprs.ast.MyNodeParent;
import com.exprs.ast.JJTSimpleExprParserState;
import com.exprs.ast.NodeManipulator;

/**
 * Instances of this interface bridge JJTricks internals with {@link MyNodeParent} instances. That
 * way, they don't need to conform to a specific interface, like JJTree forces you to do.
 */
public interface NodeManipulator {

  /** Called before calling {@link #onOpen}()} with the first token of a node. */
  public void setFirstToken(JJTSimpleExprParserState builder, MyNodeParent node, Token token);

  /** Called before calling {@link #onPush}()} with the last token of a node. */
  public void setLastToken(JJTSimpleExprParserState builder, MyNodeParent node, Token token);

  /**
   * Called when a node is first open. In this state, the node has no children yet, and no parent.
   */
  public void onOpen(JJTSimpleExprParserState builder, MyNodeParent node);

  /**
   * Called when a node is done being built. In this state, the node already has all its children,
   * but no parent yet. It's not yet on the stack of the tree builder.
   */
  public void onPush(JJTSimpleExprParserState builder, MyNodeParent node);

  /**
   * Called when a node is done being built. In this state, the node already has all its children,
   * but no parent yet.
   */
  public void addChild(
      JJTSimpleExprParserState builder, MyNodeParent parent, MyNodeParent child, int index);
}