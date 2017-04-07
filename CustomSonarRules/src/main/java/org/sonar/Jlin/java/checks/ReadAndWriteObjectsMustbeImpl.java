package org.sonar.Jlin.java.checks;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.*;
import org.sonar.check.Rule;


import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;

import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

@Rule(key = "ReadAndWriteObjectsMustbeImpl")
public class ReadAndWriteObjectsMustbeImpl extends IssuableSubscriptionVisitor {
	
	private static final String READOBJECT = "readObject";
	 private static final String WRITEOBJECT = "writeObject";

	 @Override
	  public void visitNode(Tree tree) {
		 Symbol.TypeSymbol symbol = ((ClassTree)tree).symbol();
		   // IdentifierTree simpleName = ((ClassTree)tree).simpleName();
		
			      
	    
	  
	    if (hasSemantic() && isSerializable(symbol.type())) {
	    	 List<MethodTree> methods = ((ClassTree)tree).members().stream().filter(member -> member.is(Tree.Kind.METHOD)).map(member -> (MethodTree) member).collect(Collectors.toList());
	         //Iterator<MethodTree> methIt = methods.iterator();
	    	 Optional<MethodTree> readMethod = methods.stream().filter(ReadAndWriteObjectsMustbeImpl::isReadMethod).findAny();
	    	    Optional<MethodTree> writeMethod = methods.stream().filter(ReadAndWriteObjectsMustbeImpl::isWriteMethod).findAny();
	    	    
	    	    if (readMethod.isPresent() && !writeMethod.isPresent()) {
	    	        reportIssue(readMethod.get().simpleName(),"Both ReadObjects() and writeObjects() must be implemented");
	    	      } else if (writeMethod.isPresent() && !readMethod.isPresent()) {
	    	        reportIssue(writeMethod.get().simpleName(), "Both ReadObjects() and writeObjects() must be implemented");
	    	      }
	         
	    }
	  }

	 private static boolean isSerializable(Type type) {
		    return type.isSubtypeOf("java.io.Serializable");
		  }
	 private static boolean isReadMethod(MethodTree methodTree) {
		 return READOBJECT.equals(methodTree.simpleName().name()) && hasSingleParam(methodTree, "java.io.ObjectInputStream") ;
		  }

		  private static boolean isWriteMethod(MethodTree methodTree) {
		    return WRITEOBJECT.equals(methodTree.simpleName().name()) && hasSingleParam(methodTree, "java.io.ObjectOutputStream") ;
		  }
		  

	  private static boolean hasSingleParam(MethodTree methodTree, String searchedParamType) {
	    
	    return methodTree.parameters().size() == 1 && methodTree.symbol().parameterTypes().get(0).is(searchedParamType);
	  }

	
	
	 

	@Override
	public List<Kind> nodesToVisit() {
		 return ImmutableList.of(Tree.Kind.CLASS);
	}

}
