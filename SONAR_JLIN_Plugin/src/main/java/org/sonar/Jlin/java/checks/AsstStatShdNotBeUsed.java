package org.sonar.Jlin.java.checks;

import java.util.List;

import org.sonar.check.Rule;
import org.sonar.java.matcher.MethodMatcher;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.AssertStatementTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import com.google.common.collect.ImmutableList;
@Rule(key = "AsstStatShdNotBeUsed")
public class AsstStatShdNotBeUsed extends IssuableSubscriptionVisitor {

	@Override
	public List<Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.ASSERT_STATEMENT);
	}
	  @Override
	  public void visitNode(Tree tree) {
	  AssertStatementTree assertTree=(AssertStatementTree)tree;
	  reportIssue(assertTree, "assert statements should not be used");
	  
	  }
}
