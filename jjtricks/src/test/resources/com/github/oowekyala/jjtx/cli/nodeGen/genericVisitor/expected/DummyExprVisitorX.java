/* Generated by JJTricks on Thu Jan 01 01:00:00 CET 1970 -- Not intended for manual editing. */

public interface DummyExprVisitorX<T> {

  /** Visits {@linkplain MyNodeParent MyNodeParent}. This is the root of the delegation chain. */
  default void visit(MyNodeParent node, T data) {
    // Don't recurse by default
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
  default void visit(ExExpression node, T data) {
    visit((MyNodeParent) node, data);
  }

  /**
   * Visits {@linkplain ExBinaryExpression BinaryExpression}. Delegates to {@link
   * #visit(ExExpression) } if unimplemented.
   */
  default void visit(ExBinaryExpression node, T data) {
    visit((ExExpression) node, data);
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
  default void visit(ExLiteral node, T data) {
    visit((ExExpression) node, data);
  }

  /**
   * Visits {@linkplain ExNullLiteral NullLiteral}. Delegates to {@link #visit(ExLiteral) } if
   * unimplemented.
   */
  default void visit(ExNullLiteral node, T data) {
    visit((ExLiteral) node, data);
  }

  /**
   * Visits {@linkplain ExIntegerLiteral IntegerLiteral}. Delegates to {@link #visit(ExLiteral) } if
   * unimplemented.
   */
  default void visit(ExIntegerLiteral node, T data) {
    visit((ExLiteral) node, data);
  }
}