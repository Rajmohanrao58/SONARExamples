package org.sonar.Jlin.java.checks;

import java.util.List;
import java.util.Set;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol.TypeSymbol;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Modifier;
import org.sonar.plugins.java.api.tree.ModifierKeywordTree;
import org.sonar.plugins.java.api.tree.Tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@Rule(key = "EJBClassMustNotBeFinal")
public class EJBClassMustNotBeFinal extends IssuableSubscriptionVisitor {

	 private static final Set<String> EJB_CLASSES = ImmutableSet.of(
			    "javax.ejb.EnterpriseBean"
			  );
	@Override
	public List<Tree.Kind> nodesToVisit() {
		 return ImmutableList.of(Tree.Kind.CLASS);
	}
	
	 @Override
	  public void visitNode(Tree tree) {
	    ClassTree node = (ClassTree) tree;
	    TypeSymbol symbol = node.symbol();
	    if (isEjb(symbol) && isFinal(node)) {
	    	 reportIssue(((ClassTree) tree).simpleName(), "EJB classes must not be declared final");
	    }
	  }
	 
	  private static boolean isEjb(TypeSymbol symbol) {
		    if (EJB_CLASSES.stream().anyMatch(symbol.type()::isSubtypeOf)) {
		      return true;
		    }
		    return symbol.metadata().annotations().stream().anyMatch(annotation -> annotation.symbol().type().fullyQualifiedName().startsWith("javax.ejb."));
		  }

	  private static boolean isFinal(ClassTree tree) {
		    boolean isFinal = false;
		   

		    for (ModifierKeywordTree modifierKeywordTree : tree.modifiers().modifiers()) {
		      Modifier modifier = modifierKeywordTree.modifier();
		      if (modifier == Modifier.FINAL) {
		    	  isFinal = true;
		      }
		    }
		    return isFinal ;
		  }

}
