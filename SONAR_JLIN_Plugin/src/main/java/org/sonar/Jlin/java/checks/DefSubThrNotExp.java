package org.sonar.Jlin.java.checks;

import java.util.List;
import java.util.Set;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol.TypeSymbol;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@Rule(key = "DefSubThrNotExp")
public class DefSubThrNotExp  extends IssuableSubscriptionVisitor{

	private static final Set<String> THOWABLE_CLASSES = ImmutableSet.of(
		    "java.lang.Throwable"
		  );
	private static final Set<String> EXCEPTION_CLASSES = ImmutableSet.of(
		    "java.lang.Exception"
		  );
	@Override
	public List<Kind> nodesToVisit() {
		 return ImmutableList.of(Tree.Kind.CLASS);
	}
	
	 @Override
	  public void visitNode(Tree tree) {
	    ClassTree node = (ClassTree) tree;
	    TypeSymbol symbol = node.symbol();
	    if(isThrowable(symbol) && !isException(symbol)){
	    	 reportIssue(((ClassTree) tree).simpleName(), "Don't define a subclass of java.lang.Throwable which is not a subclass of java.lang.Exception");
	    }
	 }
	 private static boolean isThrowable(TypeSymbol symbol) {
		    if (THOWABLE_CLASSES.stream().anyMatch(symbol.type()::isSubtypeOf)) {
		      return true;
		    }
		    return symbol.metadata().annotations().stream().anyMatch(annotation -> annotation.symbol().type().fullyQualifiedName().startsWith("java.lang.Throwable"));
		  }
	 
	 private static boolean isException(TypeSymbol symbol) {
		    if (EXCEPTION_CLASSES.stream().anyMatch(symbol.type()::isSubtypeOf)) {
		      return true;
		    }
		    return symbol.metadata().annotations().stream().anyMatch(annotation -> annotation.symbol().type().fullyQualifiedName().startsWith("java.lang.Exception"));
		  }

}
