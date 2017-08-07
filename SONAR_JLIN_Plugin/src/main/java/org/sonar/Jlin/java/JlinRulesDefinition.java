/*
 * SonarQube Java Custom Rules Example
 * Copyright (C) 2016-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.Jlin.java;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.annotation.Nullable;

import org.sonar.Jlin.java.ApiCompatibility.GeneratedProblemType;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.java.Java;
import org.sonar.squidbridge.annotations.AnnotationBasedRulesDefinition;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;


/**
 * Declare rule metadata in server repository of rules. 
 * That allows to list the rules in the page "Rules".
 */
public class JlinRulesDefinition implements RulesDefinition {

  public static final String JLIN_REPOSITORY_KEY = "jlin-java";
  public static final String LANGUAGE_KEY = Java.KEY;

  public static final String APICOMP_REPOSITORY_KEY = "API_Compatibility";
	private static final String APICOMP_TAG = "apicompatibility";
	//private static final String SERVER_CONFIG_TAG = "server-configuration";
	private static final String SECURITY_TAG = "security";

  private final Gson gson = new Gson();

  @Override
  public void define(Context context) {
    NewRepository jlinRepository = context
      .createRepository(JLIN_REPOSITORY_KEY, Java.KEY)
      .setName("Jlin Repository");

  
    new AnnotationBasedRulesDefinition(jlinRepository, Java.KEY)
      .addRuleClasses(/* don't fail if no SQALE annotations */ false, RulesList.getChecks());

    for (NewRule rule : jlinRepository.rules()) {
      String metadataKey = rule.key();
     
      // Setting internal key is essential for rule templates (see SONAR-6162), and it is not done by AnnotationBasedRulesDefinition from
      // sslr-squid-bridge version 2.5.1:
      rule.setInternalKey(metadataKey);
      rule.setHtmlDescription(readRuleDefinitionResource(metadataKey + ".html"));
      addMetadata(rule, metadataKey);
    }
    
    jlinRepository.done();
    NewRepository apicompRepository = context
    	      .createRepository(APICOMP_REPOSITORY_KEY, Java.KEY)
    	      .setName("API_Compatibility");
   
    for (final GeneratedProblemType problemType : GeneratedProblemType.values()) {
		final NewRule newRule = apicompRepository.createRule(problemType.getPresentationName());
		newRule.setName(problemType.getPresentationName());
		newRule.setHtmlDescription(problemType.getDescription());
		newRule.setSeverity(Severity.MAJOR);
		newRule.setStatus(RuleStatus.READY);
		newRule.setTags(APICOMP_TAG, SECURITY_TAG);
	
	}
  apicompRepository.done();
 
   
  }



  @Nullable
  private static String readRuleDefinitionResource(String fileName) {
    URL resource = JlinRulesDefinition.class.getResource("/org/sonar/l10n/java/rules/jlin/" + fileName);
    if (resource == null) {
      return null;
    }
    try {
      return Resources.toString(resource, Charsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read: " + resource, e);
    }
  }

  private void addMetadata(NewRule rule, String metadataKey) {
    String json = readRuleDefinitionResource("Jlin_rule.json");
    if (json != null) {
     HighRuleMetadata highMetadata = gson.fromJson(json, HighRuleMetadata.class);
     
     for(int i=0; i<highMetadata.RuleMetadata.length; i++){
    	
    	if(rule.key().equalsIgnoreCase(highMetadata.RuleMetadata[i].ruleKey)){
    		
    	
    		
    		     
    	      rule.setSeverity(highMetadata.RuleMetadata[i].defaultSeverity.toUpperCase(Locale.US));
    	      rule.setName(highMetadata.RuleMetadata[i].title);
    	      rule.setTags(highMetadata.RuleMetadata[i].tags);
    	      rule.setStatus(RuleStatus.valueOf(highMetadata.RuleMetadata[i].status.toUpperCase(Locale.US)));

    	      if (highMetadata.RuleMetadata[i].remediation != null) {
    	        // metadata.remediation is null for template rules
    	        rule.setDebtRemediationFunction(highMetadata.RuleMetadata[i].remediation.remediationFunction(rule.debtRemediationFunctions()));
    	        rule.setGapDescription(highMetadata.RuleMetadata[i].remediation.linearDesc);
    	      } 
    	}
    	
     }
   
   
    }
  }

  private static class HighRuleMetadata {
	  
	  RuleMetadata [] RuleMetadata; 
   
  }

  
 
  
  private static class RuleMetadata {
	  String ruleKey;
	  String title;
	    String status;
	    @Nullable
	    Remediation remediation;

	    String[] tags;
	    String defaultSeverity;
	  
  }
  private static class Remediation {
    String func;
    String constantCost;
    String linearDesc;
    String linearOffset;
    String linearFactor;

    private DebtRemediationFunction remediationFunction(DebtRemediationFunctions drf) {
      if (func.startsWith("Constant")) {
        return drf.constantPerIssue(constantCost.replace("mn", "min"));
      }
      if ("Linear".equals(func)) {
        return drf.linear(linearFactor.replace("mn", "min"));
      }
      return drf.linearWithOffset(linearFactor.replace("mn", "min"), linearOffset.replace("mn", "min"));
    }
  }
}
