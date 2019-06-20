/* Generated by JJTricks on Thu Jan 01 01:00:00 CET 1970 -- Not intended for manual editing. */

package org.exprs.ast;

import org.exprs.ast.token.MyToken;

import org.exprs.ast.MyNodeParent;
import org.exprs.ast.JJTSimpleExprParserState;
import org.exprs.ast.NodeManipulator;
import org.exprs.ast.SimpleExprParserTreeConstants;
import org.exprs.ast.SimpleExprsNodeFactory;

/**
 * Instances of this interface bridge JJTricks internals with {@link MyNodeParent} instances. That
 * way, they don't need to conform to a specific interface, like JJTree forces you to do.
 */
interface NodeManipulator {

  /**
   * Called before calling {@link #onOpen}()} with the first token of a node. Calls are only
   * inserted if the JJTricks key {@code trackTokens} is enabled, or the similar TRACK_TOKENS JJTree
   * options.
   */
  public void setFirstToken(JJTSimpleExprParserState builder, MyNodeParent node, MyToken token);

  /**
   * Called before calling {@link #onPush}()} with the last token of a node. Calls are only inserted
   * if the JJTricks key {@code trackTokens} is enabled, or the similar TRACK_TOKENS JJTree options.
   */
  public void setLastToken(JJTSimpleExprParserState builder, MyNodeParent node, MyToken token);

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