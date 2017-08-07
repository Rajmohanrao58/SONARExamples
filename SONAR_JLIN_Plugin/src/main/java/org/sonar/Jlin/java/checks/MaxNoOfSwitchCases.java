package org.sonar.Jlin.java.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.CaseGroupTree;
import org.sonar.plugins.java.api.tree.SwitchStatementTree;

@Rule(key = "MaxNoOfSwitchCases")
public class MaxNoOfSwitchCases extends BaseTreeVisitor implements JavaFileScanner  {
	
	 private JavaFileScannerContext context;
	 private static final int DEFAULT_MAX = 2;

	  @RuleProperty(
	    description = "Maximum number of cases",
	    defaultValue = "" + DEFAULT_MAX)
	  public int max = DEFAULT_MAX;

	  @Override
	  public void scanFile(JavaFileScannerContext context) {
	    this.context = context;
	    scan(context.getTree());
	  }

	  @Override
	  public void visitSwitchStatement(SwitchStatementTree tree) {
	    int count = 0;
	    for (CaseGroupTree caseGroup : tree.cases()) {
	      count += caseGroup.labels().size();
	    }
	    if (count > max) {
	      context.reportIssue(this, tree.switchKeyword(), "maximum number of cases exceeded");
	    }

	    super.visitSwitchStatement(tree);
	  }
}
