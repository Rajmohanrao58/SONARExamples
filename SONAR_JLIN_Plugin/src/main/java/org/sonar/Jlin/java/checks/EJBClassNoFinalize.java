package org.sonar.Jlin.java.checks;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol.TypeSymbol;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@Rule(key = "EJBClassNoFinalize")
public class EJBClassNoFinalize extends IssuableSubscriptionVisitor {

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
		 List<MethodTree> methods = ((ClassTree)tree).members().stream().filter(member -> member.is(Tree.Kind.METHOD)).map(member -> (MethodTree) member).collect(Collectors.toList());
         //Iterator<MethodTree> methIt = methods.iterator();
    	 Optional<MethodTree> finalizeMehtod = methods.stream().filter(EJBClassNoFinalize::isFinalize).findAny();
    	    
    
	    TypeSymbol symbol = node.symbol();
	    if (isEjb(symbol) && finalizeMehtod.isPresent()) {
	    	 reportIssue(((ClassTree) tree).simpleName(), "EJB classes must not declare a finalize method");
	    }
	  }
	 
	  private static boolean isEjb(TypeSymbol symbol) {
		    if (EJB_CLASSES.stream().anyMatch(symbol.type()::isSubtypeOf)) {
		      return true;
		    }
		    return symbol.metadata().annotations().stream().anyMatch(annotation -> annotation.symbol().type().fullyQualifiedName().startsWith("javax.ejb."));
		  }

	  private static boolean isFinalize(MethodTree methodTree) {
		    return "finalize".equals(methodTree.simpleName().name()) && methodTree.parameters().isEmpty();
		   
		  }

}
