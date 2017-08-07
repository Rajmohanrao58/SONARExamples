/*
 * SonarQube Findbugs Plugin
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.Jlin.java;

import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileImporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.java.Java;

import com.google.common.collect.Iterables;

public class JlinImporter extends ProfileImporter {

  private final RuleFinder ruleFinder;
  private static final Logger LOG = LoggerFactory.getLogger(JlinImporter.class);

  public JlinImporter(RuleFinder ruleFinder) {
    super(JlinRulesDefinition.JLIN_REPOSITORY_KEY, "JLin_Custom_Rules");
    setSupportedLanguages(Java.KEY);
    this.ruleFinder = ruleFinder;
  }

  @Override
  public RulesProfile importProfile(Reader findbugsConf, ValidationMessages messages) {
    RulesProfile profile = RulesProfile.create();
    try {
    

     for(Rule rule: rules()){
    	 profile.activateRule(rule, RulePriority.MAJOR);
    	 LOG.debug("Testing Rule Name"+rule.getName());
     }

      return profile;
    } catch (Exception e) {
      String errorMessage = "The Findbugs configuration file is not valid";
      messages.addErrorText(errorMessage + " : " + e.getMessage());
      LOG.error(errorMessage, e);
      return profile;
    }
  }

 
  private static RulePriority getPriorityFromSeverity(String severity) {
    if (Severity.INFO.equals(severity)) {
      return RulePriority.INFO;
    } else if (Severity.MAJOR.equals(severity)) {
      return RulePriority.MAJOR;
    } else if (Severity.BLOCKER.equals(severity)) {
      return RulePriority.BLOCKER;
    }
    return null;
  }

  private Iterable<Rule> rules() {
    return Iterables.concat(
      ruleFinder.findAll(RuleQuery.create().withRepositoryKey(JlinRulesDefinition.JLIN_REPOSITORY_KEY)),
      ruleFinder.findAll(RuleQuery.create().withRepositoryKey(JlinRulesDefinition.APICOMP_REPOSITORY_KEY))
     );
  }

}
