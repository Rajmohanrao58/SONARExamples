package org.sonar.Jlin.java.checks;

import java.util.List;
import java.util.Set;

import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import com.google.common.collect.ImmutableSet;

public class NonSerEJBMetParam extends IssuableSubscriptionVisitor {


	 private static final Set<String> EJB_CLASSES = ImmutableSet.of(
			    "javax.ejb.EnterpriseBean"
			  );
	@Override
	public List<Kind> nodesToVisit() {
		// TODO Auto-generated method stub
		return null;
	}

}
