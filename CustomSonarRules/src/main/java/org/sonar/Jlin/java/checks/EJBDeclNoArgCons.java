package org.sonar.Jlin.java.checks;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Symbol.TypeSymbol;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Modifier;
import org.sonar.plugins.java.api.tree.ModifierKeywordTree;
import org.sonar.plugins.java.api.tree.Tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@Rule(key = "EJBDeclNoArgCons")
public class EJBDeclNoArgCons extends IssuableSubscriptionVisitor {

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
	    
	    if (isEjb(symbol) ) {
	    	boolean hasImplecitCons = true;
	    	for (MethodTree explicitConstructor : getExplicitConstructors(node)) {
	    	    
	    		hasImplecitCons = false;
	    	    }
	    	 Collection<Symbol> constructors = node.symbol().lookupSymbols("<init>");
	    	
	         boolean hasNoArgConstructor = false;
	         for (Symbol constructor : constructors) {
	        	  if (constructor.isMethodSymbol()) {
	        	        Symbol.MethodSymbol method = (Symbol.MethodSymbol) constructor;
	        	        if (method.parameterTypes().isEmpty() && method.isPublic()) {
	        	        	hasNoArgConstructor=true;
	        	        }
	        	      }
	    	
	    
	  }
	         if (hasImplecitCons|| !hasNoArgConstructor) {
	        	 reportIssue(((ClassTree) tree).simpleName(), "EJB classes must declare a public no-args constructor");
	           }
	    }
	 }
	 
	  private static boolean isEjb(TypeSymbol symbol) {
		    if (EJB_CLASSES.stream().anyMatch(symbol.type()::isSubtypeOf)) {
		      return true;
		    }
		    return symbol.metadata().annotations().stream().anyMatch(annotation -> annotation.symbol().type().fullyQualifiedName().startsWith("javax.ejb."));
		  }


	  private static List<MethodTree> getExplicitConstructors(ClassTree classTree) {
	    ImmutableList.Builder<MethodTree> builder = ImmutableList.builder();
	    for (Tree member : classTree.members()) {
	      if (isConstructor(member)) {
	        builder.add((MethodTree) member);
	      }
	    }
	    return builder.build();
	  }

	  private static boolean isConstructor(Tree tree) {
	    return tree.is(Tree.Kind.CONSTRUCTOR);
	  }

	  
}
