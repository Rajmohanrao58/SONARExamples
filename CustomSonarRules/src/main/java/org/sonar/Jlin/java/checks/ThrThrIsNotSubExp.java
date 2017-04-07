package org.sonar.Jlin.java.checks;

import java.util.List;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.ThrowStatementTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import com.google.common.collect.ImmutableList;

@Rule(key = "ThrThrIsNotSubExp")
public class ThrThrIsNotSubExp extends IssuableSubscriptionVisitor{
	private static final String THROWABLE_CLASS  = "java.lang.Throwable" ;
	private static final String EXCEPTION_CLASS  = "java.lang.Exception" ;
	@Override
	public List<Kind> nodesToVisit() {
		 return ImmutableList.of(Tree.Kind.THROW_STATEMENT);
	}
	 @Override
	  public void visitNode(Tree tree) {
	    ThrowStatementTree node = (ThrowStatementTree) tree;
	    Type symbol = node.expression().symbolType();
	    if(symbol.isSubtypeOf(THROWABLE_CLASS) && !symbol.isSubtypeOf(EXCEPTION_CLASS)){
	    	 reportIssue(node.expression(), "Throwable thrown is not a subclass of java.lang.Exception.");
	    }
	 }
	

	
}
