/* Generated by JJTricks on Thu Jan 01 01:00:00 CET 1970 -- Not intended for manual editing. */

package org.exprs.ast;

public class ExIntegerLiteral extends AbstractExLiteral {

  public ExIntegerLiteral(int id) {
    super(id);
  }

  public ExIntegerLiteral(DummyExprParser parser, int id) {
    super(parser, id);
  }

  @Override
  public <T> void jjtAccept(DummyExprVisitorX<T> visitor, T data) {
    visitor.visit(this, data);
  }
}