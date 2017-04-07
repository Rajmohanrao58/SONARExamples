package org.sonar.Jlin.java.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.TypeTree;
import org.sonar.plugins.java.api.tree.VariableTree;

import com.google.common.collect.ImmutableList;

@Rule(key = "NonEclipseApiUsed")
public class NonEclipseApiUsed extends IssuableSubscriptionVisitor{
	 private List<Tree> reportedTrees = new ArrayList<>();
	
	
	 private static final String DISALLOWED_PKG = "org.eclipse.*";
			  ;
	// public String disallowedClasses = "java.lang.ClassLoader,java.io.File, java.lang.SecurityManager, java.lang.Thread,java.io.FileDescriptor, java.lang.Runtime";
	  private Pattern pattern = null;
	@Override
	public List<Kind> nodesToVisit() {
		 return ImmutableList.of(Tree.Kind.CLASS);
	}
	@Override
	  public void visitNode(Tree tree) {
		  ClassTree classTree= (ClassTree)tree;
		
		  TypeTree superClass = classTree.superClass();
		    if (superClass != null) {
		      String superClassTypeName = superClass.symbolType().fullyQualifiedName();
		      checkIfDisallowed(superClassTypeName, superClass);
		    }
		  for (Tree member : classTree.members()) {
		      if (isVariable(member)) {
		    	  String variableTypeName = ((VariableTree)member).type().symbolType().fullyQualifiedName();
				  checkIfDisallowed(variableTypeName, ((VariableTree)member).type());
		      }
		      
		      if (isMethod(member)) {
		    	  MethodTree methodTree = (MethodTree)member;
		    	  if(!methodTree.parameters().isEmpty()){
		    		  for(VariableTree arguments : methodTree.parameters()){
		    			  String variableTypeName = ((VariableTree)arguments).type().symbolType().fullyQualifiedName();
						  checkIfDisallowed(variableTypeName, ((VariableTree)arguments).type());
		    		  }
		    		  
		    	  }
				  if (methodTree.returnType() != null ) {
				      String returnTypeName = methodTree.returnType().symbolType().fullyQualifiedName();
				      checkIfDisallowed(returnTypeName, methodTree.returnType());
				    }
		      }
		      
		      if (isNewClass(member)) {
		    	  NewClassTree newClassTree= (NewClassTree)member;
				  String newClassTypeName = newClassTree.identifier().symbolType().fullyQualifiedName();
				    Tree parent = newClassTree.parent();
				    if (parent != null && !parent.is(Tree.Kind.VARIABLE)) {
				      checkIfDisallowed(newClassTypeName, newClassTree);
				    }
				    
		      }
		  }
		      
		      
		  }
		  
	 private static boolean isVariable(Tree tree) {
		    return tree.is(Tree.Kind.VARIABLE);
		  }

	 private static boolean isMethod(Tree tree) {
		    return tree.is(Tree.Kind.METHOD);
		  }

	 private static boolean isNewClass(Tree tree) {
		    return tree.is(Tree.Kind.NEW_CLASS);
		  }

		
		  
		
		  
		
		 
	
	  private void checkIfDisallowed(String className, Tree tree) {
		    
		   
		    
		   
			      try {
			        pattern = Pattern.compile(DISALLOWED_PKG);
			      } catch (IllegalArgumentException e) {
			        throw new IllegalArgumentException("[" + getClass().getSimpleName() + "] Unable to compile the regular expression: " + DISALLOWED_PKG, e);
			      }
			   
			    if (pattern.matcher(className).matches()) {
			    	String[] packComp=className.split(".");
			    	for(int i=2;i<packComp.length;i++){
			    		if(packComp[i].equalsIgnoreCase("internal")){
			    			reportIssue(tree, "Non Eclipse API class used.");
			    			
			    		}
			    	}
			    	
			    	 
			    }
		    
		  }
	  


	
}
	  
	  
