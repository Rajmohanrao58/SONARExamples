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

import java.util.List;

import org.sonar.Jlin.java.checks.AsstStatShdNotBeUsed;
import org.sonar.Jlin.java.checks.DefSubThrNotExp;
import org.sonar.Jlin.java.checks.EJBClassMustBePublic;
import org.sonar.Jlin.java.checks.EJBClassMustNotBeFinal;
import org.sonar.Jlin.java.checks.EJBClassNoFinalize;
import org.sonar.Jlin.java.checks.EJBDeclNoArgCons;
import org.sonar.Jlin.java.checks.HashMethodReturnsConstant;
import org.sonar.Jlin.java.checks.MaxNoOfSwitchCases;
import org.sonar.Jlin.java.checks.NonEclipseApiUsed;
import org.sonar.Jlin.java.checks.ReadAndWriteObjectsMustbeImpl;
import org.sonar.Jlin.java.checks.ResBdleGetBdleUsesDftLoc;
import org.sonar.Jlin.java.checks.ThrCgtIsNotSubExp;
import org.sonar.Jlin.java.checks.ThrThrIsNotSubExp;
import org.sonar.Jlin.java.checks.UsgOfForbnClassInEJB;
import org.sonar.Jlin.java.checks.UsgOfForbnMethInEJB;
import org.sonar.Jlin.java.checks.UsgOfForbnPackInEJB;
import org.sonar.plugins.java.api.JavaCheck;

import com.google.common.collect.ImmutableList;

public final class RulesList {

  private RulesList() {
  }

  public static List<Class> getChecks() {
    return ImmutableList.<Class>builder().addAll(getJavaChecks()).addAll(getJavaTestChecks()).build();
  }

  public static List<Class<? extends JavaCheck>> getJavaChecks() {
    return ImmutableList.<Class<? extends JavaCheck>>builder()
       .add(ReadAndWriteObjectsMustbeImpl.class)
      .add(HashMethodReturnsConstant.class)
      .add(MaxNoOfSwitchCases.class)
      .add(EJBClassMustBePublic.class)
      .add(EJBClassMustNotBeFinal.class)
      .add(EJBClassNoFinalize.class)
      .add(EJBDeclNoArgCons.class)
      .add(UsgOfForbnPackInEJB.class)
      .add(UsgOfForbnClassInEJB.class)
      .add(DefSubThrNotExp.class)
      .add(ThrThrIsNotSubExp.class)
      .add(ThrCgtIsNotSubExp.class)
      .add(UsgOfForbnMethInEJB.class)
      .add(NonEclipseApiUsed.class)
      .add(ResBdleGetBdleUsesDftLoc.class)
      .add(AsstStatShdNotBeUsed.class)
      .build();
  }

  public static List<Class<? extends JavaCheck>> getJavaTestChecks() {
    return ImmutableList.<Class<? extends JavaCheck>>builder()
      .build();
  }
}
