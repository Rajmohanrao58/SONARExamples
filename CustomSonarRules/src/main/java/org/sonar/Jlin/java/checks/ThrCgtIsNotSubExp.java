package org.sonar.Jlin.java.checks;

import java.util.List;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.CatchTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.TypeTree;

import com.google.common.collect.ImmutableList;

@Rule(key = "ThrCgtIsNotSubExp")
public class ThrCgtIsNotSubExp extends IssuableSubscriptionVisitor{
	private static final String THROWABLE_CLASS  = "java.lang.Throwable" ;
	private static final String EXCEPTION_CLASS  = "java.lang.Exception" ;
	@Override
	public List<Kind> nodesToVisit() {
		 return ImmutableList.of(Tree.Kind.CATCH);
	}
	 @Override
	  public void visitNode(Tree tree) {
		 CatchTree node = (CatchTree) tree;
	    TypeTree typetree = node.parameter().type();
	   Type symbol = typetree.symbolType();
	    if(symbol.isSubtypeOf(THROWABLE_CLASS) && !symbol.isSubtypeOf(EXCEPTION_CLASS)){
	    	 reportIssue(node.parameter(), "Throwable Caught is not a subclass of java.lang.Exception.");
	    }
	 }
	

	
}