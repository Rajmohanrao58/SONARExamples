package org.sonar.Jlin.java.checks;

import java.awt.Window.Type;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.sonar.Jlin.java.restrictedcomponent.RCClass;
import org.sonar.Jlin.java.restrictedcomponent.RCMethod;
import org.sonar.Jlin.java.restrictedcomponent.RCPackage;
import org.sonar.Jlin.java.restrictedcomponent.RestrictedComponentUtil;
import org.sonar.Jlin.java.restrictedcomponent.RestrictedComponents;
import org.sonar.Jlin.java.util.XMLTool;
import org.sonar.check.Rule;
import org.sonar.java.matcher.MethodMatcher;
import org.sonar.java.matcher.NameCriteria;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ListTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.PackageDeclarationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TypeTree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import com.google.common.collect.ImmutableList;

@Rule(key = "RestrictedComponentCheck")
public class RestrictedComponentCheck extends IssuableSubscriptionVisitor  {
 private Pattern pattern = null;
 private List<MethodMatcher> matchers;
	static public final String TEST_NAME = "Restricted Components"; //$NON-NLS-1$

	static public final String PARAMETER_GRAMMAR_FILE = "C:\\Testing\\restrictionlist.xml"; //$NON-NLS-1$
	static public final String PARAMETER_ONE_MSG_PER_CU = "ONE_MSG_PER_CU"; //$NON-NLS-1$
	static public final String MSG_KEY_FORBIDDEN_COMPONENT = "rc.1"; //$NON-NLS-1$
	static public final String MSG_KEY_FORBIDDEN_ROOT = "rc.2"; //$NON-NLS-1$

	static public final String ERROR_PARAMETER_CALLER = "CALLER"; //$NON-NLS-1$
	static public final String ERROR_PARAMETER_CALLEE = "CALLEE"; //$NON-NLS-1$
	static public final String ERROR_PARAMETER_COMPONENT = "COMPONENT"; //$NON-NLS-1$

	/*private Stack<IReferenceTypeBinding> typeBndStack = new Stack<IReferenceTypeBinding>();
	private Set<String> componentNames = new HashSet<String>();
	private Set<IBinding> alreadyCheckedTypes = new HashSet<IBinding>();*/
	private Set<String> componentNames = new HashSet<String>();
	private boolean isMainMethod;
	private boolean oneMsgPerFile = false;
	private Set alreadyCheckedTypes= new HashSet<>();

	private RestrictedComponents rc;

	private File restrictedComponentsFile;
	private long fileLastMod;
	
	
	
	


	 
	 public void initialize() throws Exception{
		 try {
			
			 String grammarFile=PARAMETER_GRAMMAR_FILE;
				//oneMsgPerFile = ((Boolean) getInputParameter(PARAMETER_ONE_MSG_PER_CU))
				//		.booleanValue();
				// try to load by classloader first, then by filesystem
				//String grammarFile = (String) getInputParameter(PARAMETER_GRAMMAR_FILE);
				InputStream is = this.getClass().getClassLoader()
						.getResourceAsStream(grammarFile);
				
				if (is != null) {
					rc = new RestrictedComponents(XMLTool.parseStream(is));
					is.close();
					
				} else {
					File tempRestrictedComponentsFile = new File(grammarFile);
					long lastModified = tempRestrictedComponentsFile.lastModified();
	        if (lastModified == 0L) {
	          rc = null;
	          throw new Exception(new FileNotFoundException("config file: "  + grammarFile));
	        }
					if (!tempRestrictedComponentsFile
							.equals(restrictedComponentsFile)
							|| lastModified > fileLastMod) {
						fileLastMod = lastModified;
						restrictedComponentsFile = tempRestrictedComponentsFile;
						InputStream in = new BufferedInputStream(new FileInputStream(
								restrictedComponentsFile));
						rc = new RestrictedComponents(XMLTool.parseStream(in));
						
						in.close();
						RestrictedComponentUtil.getRestrictedComponent(rc);
					}
				}
				
			} catch (Exception e) {
	      // switch off the test
	      rc = null;
				throw new Exception(e);
			}
	 }


	 @Override
	  public void visitNode(Tree tree) {
		 try {
			initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 if(tree.is(Kind.VARIABLE)){
			System.out.println(((VariableTree)tree).simpleName().symbolType().fullyQualifiedName());
			 
		 }
		 if (tree.is(Tree.Kind.CLASS)){
		  ClassTree classTree= (ClassTree)tree;
		  
		  TypeTree superClass = classTree.superClass();
		    if (superClass != null) {
		      String superClassTypeName = superClass.symbolType().fullyQualifiedName();
		      checkIfDisallowed(superClassTypeName, superClass);
		    }
		  for (Tree member : classTree.members()) {
		      if (isVariable(member)) {
		    	  String variableTypeName = ((VariableTree)member).simpleName().symbolType().fullyQualifiedName();
		    	  System.out.println("Vaiables"+ variableTypeName);
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
		 
		
			 
			 for (MethodMatcher invocationMatcher : matchers()) {
			        checkInvocation(tree, invocationMatcher);
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





	@Override
	public List<Kind> nodesToVisit() {
		 return ImmutableList.of(Tree.Kind.METHOD_INVOCATION, Tree.Kind.NEW_CLASS , Tree.Kind.CLASS,Tree.Kind.VARIABLE);
	}


	  private void checkIfDisallowed(String className, Tree tree) {
		    
		   
		    for(String packages : RestrictedComponentUtil.restrictionComponents){
		   
			      try {
			        pattern = Pattern.compile(packages);
			      } catch (IllegalArgumentException e) {
			        throw new IllegalArgumentException("[" + getClass().getSimpleName() + "] Unable to compile the regular expression: " + packages, e);
			      }
			   
			    if ( pattern.matcher(className).matches()) {
			    	 reportIssue(tree, "Remove the Use of forbidden Component Or get an exception from JLin team.");
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
	  
	  protected void onMethodInvocationFound(MethodInvocationTree mit) {
			
		    reportIssue(mit, "Remove the Use of forbidden Component Or get an exception from JLin team.");
			
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
			
			List<MethodMatcher> methodMatchersList=new ArrayList<MethodMatcher>();
			for(String forbiddenElement : RestrictedComponentUtil.restrictionComponents){
				methodMatchersList.add(MethodMatcher.create().typeDefinition(forbiddenElement).name(NameCriteria.any()).withAnyParameters());
				
			}
		    return methodMatchersList;
		   
		  }
}
