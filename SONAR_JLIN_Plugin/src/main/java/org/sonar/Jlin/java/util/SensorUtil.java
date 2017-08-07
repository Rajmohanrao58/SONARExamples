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
 * Created on 11.07.2016
 *
 */
package org.sonar.Jlin.java.util;

import org.sonar.Jlin.java.ApiCompatibility.StaticMessageFlags;
import org.sonar.api.batch.rule.Severity;



/**
 * @author nwe
 *
 */
public class SensorUtil {

//	private static final Logger LOG = Loggers.get(SensorUtil.class);

	

	private SensorUtil() {
		// hide constructor
	}




	/**
	 * Returns the severity of a SonarQube issue for the given Xanitizer finding
	 * @param xanFinding
	 * @return
	 */
	public static Severity mkSeverity(final StaticMessageFlags staticMessageFlags) {
		
		switch (staticMessageFlags._prio) {
		case 1:
			return Severity.BLOCKER;
		case 2:
			return Severity.CRITICAL;
		case 3:
			return Severity.MAJOR;
			
		default:
			return Severity.MINOR;
		}
	}

	/*private static Severity mkSeverityFromRating(final XMLReportFinding xanFinding) {
		// For the rest, we use the rating.
		final double rating = xanFinding.getRating();
		if (rating > 7) {
			return Severity.CRITICAL;
		}
		if (rating > 4) {
			return Severity.MAJOR;
		}
		if (rating > 1) {
			return Severity.MINOR;
		}
		return Severity.INFO;
	}
	
	*/

}
