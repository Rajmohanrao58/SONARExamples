package org.sonar.plugins.findbugs;

import org.sonar.api.Extension;
import org.sonar.api.config.Settings;

public class SonarProperties implements Extension {
	private String propertyvalue;
	public SonarProperties(Settings s){
		propertyvalue= s.getString("sonar.value");
		
	}

	public String getProperty(){
		return propertyvalue;
	}
}
