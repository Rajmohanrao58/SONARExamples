package org.sonar.Jlin.java.checks;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sonar.Jlin.java.util.AbstractMethodDetection;
import org.sonar.check.Rule;

import org.sonar.java.checks.helpers.MethodsHelper;

import org.sonar.java.matcher.MethodMatcher;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol.TypeSymbol;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.Tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@Rule(key = "UsgOfForbnMethInEJB")
public class UsgOfForbnMethInEJB extends IssuableSubscriptionVisitor {

	
	 private static final Set<String> EJB_CLASSES = ImmutableSet.of(
			    "javax.ejb.EnterpriseBean"
			  );
	
	 private boolean isEjb;
	
	 private List<MethodMatcher> matchers;

	  @Override
	  public List<Tree.Kind> nodesToVisit() {
	    return ImmutableList.of(Tree.Kind.METHOD_INVOCATION, Tree.Kind.NEW_CLASS , Tree.Kind.CLASS);
	  }

	  @Override
	  public void visitNode(Tree tree) {
		  if (tree.is(Tree.Kind.CLASS)){
			  isEjb = isEjb(((ClassTree)tree).symbol());
		  }
	    if (hasSemantic()) {
	      for (MethodMatcher invocationMatcher : matchers()) {
	        checkInvocation(tree, invocationMatcher);
	      }
	    }
	  }

	  private void checkInvocation(Tree tree, MethodMatcher invocationMatcher) {
	    if (tree.is(Tree.Kind.METHOD_INVOCATION)) {
	      MethodInvocationTree mit = (MethodInvocationTree) tree;
	      if (invocationMatcher.matches(mit)) {
	        onMethodInvocationFound(mit);
	      }
	    } else if (tree.is(Tree.Kind.NEW_CLASS)) {
	      NewClassTree newClassTree = (NewClassTree) tree;
	      if (invocationMatcher.matches(newClassTree)) {
	        onConstructorFound(newClassTree);
	      }
	    }
	  }

	

	 
	  protected void onConstructorFound(NewClassTree newClassTree) {
	    // Do nothing by default
	  }

	  private List<MethodMatcher> matchers() {
	    if (matchers == null) {
	      matchers = getMethodInvocationMatchers();
	    }
	    return matchers;
	  }
	
	  protected List<MethodMatcher> getMethodInvocationMatchers() {
		
		
	    return ImmutableList.<MethodMatcher>builder()
	        .add(MethodMatcher.create().typeDefinition("java.lang.System").name("exit").addParameter("int"))
	        .add(MethodMatcher.create().typeDefinition("java.lang.System").name("setErr").addParameter("java.io.PrintStream"))
	        .add(MethodMatcher.create().typeDefinition("java.lang.System").name("setIn").addParameter("java.io.InputStream"))
	        .add(MethodMatcher.create().typeDefinition("java.lang.System").name("setOut").addParameter("java.io.PrintStream"))
	        .add(MethodMatcher.create().typeDefinition("java.lang.System").name("load").addParameter("java.lang.String"))
	        .add(MethodMatcher.create().typeDefinition("java.lang.System").name("loadLibrary").addParameter("java.lang.String"))
	        .build();
	   
	  }

	  
	  protected void onMethodInvocationFound(MethodInvocationTree mit) {
		if(isEjb){
	    reportIssue(mit, "usage of forbidden Method inside an EJB");
		}
	  }
	  
	  private static boolean isEjb(TypeSymbol symbol) {
		   
		  if (EJB_CLASSES.stream().anyMatch((symbol).type()::isSubtypeOf)) {
		      return true;
		    }
		    return (symbol).metadata().annotations().stream().anyMatch(annotation -> annotation.symbol().type().fullyQualifiedName().startsWith("javax.ejb."));
		  }
	  	}