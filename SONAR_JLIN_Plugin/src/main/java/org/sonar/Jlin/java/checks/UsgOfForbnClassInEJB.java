package org.sonar.Jlin.java.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.sonar.check.Rule;

import org.sonar.java.checks.helpers.ExpressionsHelper;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.semantic.Symbol.TypeSymbol;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.CatchTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.TypeTree;
import org.sonar.plugins.java.api.tree.VariableTree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@Rule(key = "UsgOfForbnClassInEJB")
public class UsgOfForbnClassInEJB extends IssuableSubscriptionVisitor{
	 private List<Tree> reportedTrees = new ArrayList<>();
	 private boolean isEjb;
	 private static final Set<String> EJB_CLASSES = ImmutableSet.of(
			    "javax.ejb.EnterpriseBean"
			  );
	 private static final Set<String> DISALLOWED_CLASSES = ImmutableSet.of(
			 "java.lang.ClassLoader","java.io.File", "java.lang.SecurityManager", "java.lang.Thread,java.io.FileDescriptor", "java.lang.Runtime"
			  );
	 public String disallowedClasses = "java.lang.ClassLoader,java.io.File, java.lang.SecurityManager, java.lang.Thread,java.io.FileDescriptor, java.lang.Runtime";
	  private Pattern pattern = null;
	@Override
	public List<Kind> nodesToVisit() {
		 return ImmutableList.of(Tree.Kind.CLASS);
	}
	@Override
	  public void visitNode(Tree tree) {
		  ClassTree classTree= (ClassTree)tree;
		  isEjb=isEjb(classTree.symbol());
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
		    
		    if (isEjb && DISALLOWED_CLASSES.contains(className)) {
		     reportIssue(tree, "Remove the use of this forbidden Class in EJB.");
		    }
		  }
	  
	  private static boolean isEjb(TypeSymbol symbol) {
		   
		  if (EJB_CLASSES.stream().anyMatch((symbol).type()::isSubtypeOf)) {
		      return true;
		    }
		    return (symbol).metadata().annotations().stream().anyMatch(annotation -> annotation.symbol().type().fullyQualifiedName().startsWith("javax.ejb."));
		  }
	 /*
	  
			  public String disallowedClasses = "java.lang.ClassLoader,java.io.File, java.lang.SecurityManager, java.lang.Thread,java.io.FileDescriptor, java.lang.Runtime";
			  private Pattern pattern = null;
			  private JavaFileScannerContext context;

			  @Override
			  public void scanFile(JavaFileScannerContext context) {
			    this.context = context;
			    if (context.getSemanticModel() != null) {
			      scan(context.getTree());
			    }
			  }

			  @Override
			  public void visitVariable(VariableTree variableTree) {
			    String variableTypeName = variableTree.type().symbolType().fullyQualifiedName();
			    checkIfDisallowed(variableTypeName, variableTree.type());
			    super.visitVariable(variableTree);
			  }

			  @Override
			  public void visitMethod(MethodTree methodTree) {
			    if (methodTree.returnType() != null ) {
			      String returnTypeName = methodTree.returnType().symbolType().fullyQualifiedName();
			      checkIfDisallowed(returnTypeName, methodTree.returnType());
			    }
			    super.visitMethod(methodTree);
			  }

			  @Override
			  public void visitNewClass(NewClassTree newClassTree) {
			    String newClassTypeName = newClassTree.identifier().symbolType().fullyQualifiedName();
			    Tree parent = newClassTree.parent();
			    if (parent != null && !parent.is(Tree.Kind.VARIABLE)) {
			      checkIfDisallowed(newClassTypeName, newClassTree);
			    }
			    super.visitNewClass(newClassTree );
			  }

			  @Override
			  public void visitClass(ClassTree classTree) {
				  isEjb=isEjb(classTree.symbol());
			    TypeTree superClass = classTree.superClass();
			    if (superClass != null) {
			      String superClassTypeName = superClass.symbolType().fullyQualifiedName();
			      checkIfDisallowed(superClassTypeName, superClass);
			    }
			    super.visitClass(classTree);
			  }

			  private void checkIfDisallowed(String className, Tree tree) {
			    if (pattern == null) {
			      try {
			        pattern = Pattern.compile(disallowedClasses);
			      } catch (IllegalArgumentException e) {
			        throw new IllegalArgumentException("[" + getClass().getSimpleName() + "] Unable to compile the regular expression: " + disallowedClasses, e);
			      }
			    }
			    if (isEjb && pattern.matcher(className).matches()) {
			      context.reportIssue(this, tree, "Remove the use of this forbidden Class in EJB.");
			    }
			  }
			  
			  
			  
	  private static boolean isEjb(TypeSymbol symbol) {
		   
		  if (EJB_CLASSES.stream().anyMatch((symbol).type()::isSubtypeOf)) {
		      return true;
		    }
		    return (symbol).metadata().annotations().stream().anyMatch(annotation -> annotation.symbol().type().fullyQualifiedName().startsWith("javax.ejb."));
		  }

	  private static boolean isForbiddenPackage(String reference) {
		    return reference.startsWith("java.awt.") || reference.startsWith("javax.swing.") || reference.startsWith("java.lang.reflect.");
		   	 }
*/
	
}
	  
	  
