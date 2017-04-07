package org.sonar.Jlin.java.checks;

import java.util.List;

import javax.annotation.Nullable;

import org.sonar.check.Rule;
import org.sonar.java.resolve.JavaType;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Symbol.MethodSymbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.ReturnStatementTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.TypeTree;

import com.google.common.collect.ImmutableList;

@Rule(key = "HashMethodReturnsConstant")
public class HashMethodReturnsConstant extends IssuableSubscriptionVisitor {
	
	  @Override
	  public List<Kind> nodesToVisit() {
	    return ImmutableList.of(Kind.METHOD);
	  }

	  @Override
	  public void visitNode(Tree tree) {
	   MethodTree methodTree = (MethodTree)tree;
	 
	   if(isHashCodeMethod(methodTree)){
		   TypeTree ReturntypeTree= methodTree.returnType();
			  Type type= ReturntypeTree.symbolType();
	    	 
		  
		   if (isConstantType(type)) {
		          reportIssue(methodTree, "hashCode() Mehtod returns constant");
		        }
		   
		   
	   }
	    
	    } 
	   
	  

	  
	  private static boolean isHashCodeMethod(MethodTree methodTree){
		  return "hashCode".equals(methodTree.simpleName().name()) && methodTree.parameters().isEmpty() && returnsInt(methodTree);
	    	
	    }
	  private static boolean returnsInt(MethodTree tree) {
		   TypeTree typeTree = tree.returnType();
		    return typeTree != null && typeTree.symbolType().isPrimitive(org.sonar.plugins.java.api.semantic.Type.Primitives.INT);
		  }

	  private static boolean isConstantType(Type symbolType) {
		    return symbolType.isPrimitive() || symbolType.is("java.lang.String") || ((JavaType) symbolType).isPrimitiveWrapper();
		  }
}
