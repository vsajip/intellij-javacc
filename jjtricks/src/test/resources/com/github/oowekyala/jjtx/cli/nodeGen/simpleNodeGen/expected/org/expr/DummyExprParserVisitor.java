/* Generated by JJTricks on Thu Jan 01 01:00:00 CET 1970 -- Not intended for manual editing. */

package org.expr;

public interface DummyExprParserVisitor {

  /** Visits {@linkplain Root Root}. This is the root of the delegation chain. */
  default Object visit(Root node, Object data) {
    return node.childrenAccept(this, data);
  }

  /**
   * Visits {@linkplain ExExpression Expression}. Delegates to {@link #visit(Root,Object) } if
   * unimplemented.
   *
   * <p>This method is delegated to by:
   *
   * <ul>
   *   <li>{@link #visit(ExBinaryExpression,Object) }
   *   <li>{@link #visit(ExLiteral,Object) }
   * </ul>
   */
  default Object visit(ExExpression node, Object data) {
    return visit((Root) node, data);
  }

  /**
   * Visits {@linkplain ExBinaryExpression BinaryExpression}. Delegates to {@link
   * #visit(ExExpression,Object) } if unimplemented.
   */
  default Object visit(ExBinaryExpression node, Object data) {
    return visit((ExExpression) node, data);
  }

  /**
   * Visits {@linkplain ExLiteral Literal}. Delegates to {@link #visit(ExExpression,Object) } if
   * unimplemented.
   *
   * <p>This method is delegated to by:
   *
   * <ul>
   *   <li>{@link #visit(ExNullLiteral,Object) }
   *   <li>{@link #visit(ExIntegerLiteral,Object) }
   * </ul>
   */
  default Object visit(ExLiteral node, Object data) {
    return visit((ExExpression) node, data);
  }

  /**
   * Visits {@linkplain ExNullLiteral NullLiteral}. Delegates to {@link #visit(ExLiteral,Object) }
   * if unimplemented.
   */
  default Object visit(ExNullLiteral node, Object data) {
    return visit((ExLiteral) node, data);
  }

  /**
   * Visits {@linkplain ExIntegerLiteral IntegerLiteral}. Delegates to {@link
   * #visit(ExLiteral,Object) } if unimplemented.
   */
  default Object visit(ExIntegerLiteral node, Object data) {
    return visit((ExLiteral) node, data);
  }
}
