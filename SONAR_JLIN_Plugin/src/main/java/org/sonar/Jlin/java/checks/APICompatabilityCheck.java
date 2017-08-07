/** 
 * SonarQube Xanitizer Plugin
 * Copyright 2012-2016 by RIGS IT GmbH, Switzerland, www.rigs-it.ch.
 * mailto: info@rigs-it.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Created on Apr 13, 2017
 *
 */
package org.sonar.Jlin.java.checks;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.sonar.Jlin.java.JlinSensor;
import org.sonar.Jlin.java.ApiCompatibility.IUsageOfAPI;
import org.sonar.Jlin.java.util.StaticAttributes;
import org.sonar.check.Rule;
import org.sonar.java.ast.visitors.PublicApiChecker;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.VariableTree;

import com.google.common.collect.ImmutableList;


/**
 * @author C5242815
 *
 */
@Rule(key = "APICompatabilityCheck")
public class APICompatabilityCheck extends IssuableSubscriptionVisitor {
	


	/* (non-Javadoc)
	 * @see org.sonar.java.ast.visitors.SubscriptionVisitor#nodesToVisit()
	 */
	@Override
	public List<Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.CLASS,Tree.Kind.INTERFACE);
		   
	}
	
	  @Override
	  public void visitNode(Tree tree) {
		  
	ClassTree classTree= (ClassTree) tree;
	List<Tree> members=classTree.members();
	String comments= PublicApiChecker.getApiJavadoc(classTree);
	
	processComments(comments, classTree.symbol().type().fullyQualifiedName(), null);
	
	for(Tree classMembers : members){
		  if (classMembers.is(Tree.Kind.METHOD)){
			  MethodTree methodTree=(MethodTree) classMembers;
			  String commentsMethod = PublicApiChecker.getApiJavadoc(methodTree);
			 
			  processComments(commentsMethod, classTree.symbol().type().fullyQualifiedName(), null);
			 
			 
		  
	  }
		  if (classMembers.is(Tree.Kind.VARIABLE)){
			  VariableTree variableTree=(VariableTree) classMembers;
			  String commentsVariable = PublicApiChecker.getApiJavadoc(variableTree);
			 
			  processComments(commentsVariable, classTree.symbol().type().fullyQualifiedName(), null);
			  
		  }
	}
	  } 
	  
	  
	  private void processComments(String comments, String packageAndClass, String element) {
			
		
			JlinSensor.initDataStaticAttribute();
			// check if any taglet is used
			boolean noCall=false;
			Iterator<String> it1 = Arrays.asList(StaticAttributes.usageNoCall).iterator();
			while(it1.hasNext()){
				String oneTaglet = it1.next();
				if(comments != null){
				if(comments.contains(oneTaglet)){
					noCall=true;
				}
				}
			}
			boolean noInherit=false;
			Iterator<String> it2 = Arrays.asList(StaticAttributes.usageNoInherit).iterator();
			while(it2.hasNext()){
				String oneTaglet = it2.next();
				if(comments != null){
				if(comments.contains(oneTaglet)){
					noInherit=true;
				}
				}
			}
			IUsageOfAPI.CodeClasses tmp= IUsageOfAPI.CodeClasses.getValue(!noCall, !noInherit);
			
			if(element==null){
				
//				attr.data.addUsageClass(packageAndClass, IUsageOfAPI.CodeClasses.NO_API);// just for debugging.	
				StaticAttributes.data.addUsageClass(packageAndClass, tmp);
				
				
			}
			else{
				
//				attr.data.addUsageElement(packageAndClass, element, IUsageOfAPI.CodeClasses.NO_API);// just for debugging.
				StaticAttributes.data.addUsageElement(packageAndClass, element, tmp);
				 
			}
		}

	  
}
