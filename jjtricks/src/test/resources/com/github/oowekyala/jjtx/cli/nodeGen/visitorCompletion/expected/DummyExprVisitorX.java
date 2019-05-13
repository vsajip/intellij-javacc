/*
 * Generated by JJTricks
 * Not intended for manual editing.
 */

public interface DummyExprVisitorX {

  /** Visits {@linkplain MyNodeParent MyNodeParent}. This is the root of the delegation chain. */
  default void visit(MyNodeParent node) {
    node.childrenAccept(this, data);
  }

  /**
   * Visits {@linkplain ExExpression Expression}. Delegates to {@link #visit(MyNodeParent) } if
   * unimplemented.
   *
   * <p>This method is delegated to by:
   *
   * <ul>
   *   <li>{@link #visit(ExBinaryExpression) }
   *   <li>{@link #visit(ExLiteral) }
   * </ul>
   */
  default void visit(ExExpression node) {
    visit((MyNodeParent) node);
  }

  /**
   * Visits {@linkplain ExBinaryExpression BinaryExpression}. Delegates to {@link
   * #visit(ExExpression) } if unimplemented.
   */
  default void visit(ExBinaryExpression node) {
    visit((ExExpression) node);
  }

  /**
   * Visits {@linkplain ExLiteral Literal}. Delegates to {@link #visit(ExExpression) } if
   * unimplemented.
   *
   * <p>This method is delegated to by:
   *
   * <ul>
   *   <li>{@link #visit(ExNullLiteral) }
   *   <li>{@link #visit(ExIntegerLiteral) }
   * </ul>
   */
  default void visit(ExLiteral node) {
    visit((ExExpression) node);
  }

  /**
   * Visits {@linkplain ExNullLiteral NullLiteral}. Delegates to {@link #visit(ExLiteral) } if
   * unimplemented.
   */
  default void visit(ExNullLiteral node) {
    visit((ExLiteral) node);
  }

  /**
   * Visits {@linkplain ExIntegerLiteral IntegerLiteral}. Delegates to {@link #visit(ExLiteral) } if
   * unimplemented.
   */
  default void visit(ExIntegerLiteral node) {
    visit((ExLiteral) node);
  }
}