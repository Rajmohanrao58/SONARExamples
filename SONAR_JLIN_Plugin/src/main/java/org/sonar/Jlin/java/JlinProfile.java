package org.sonar.Jlin.java;

import org.sonar.Jlin.java.JlinImporter;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.java.Java;

public class JlinProfile  extends ProfileDefinition {
	
	 private final JlinImporter importer;

	  public JlinProfile(JlinImporter importer) {
	    this.importer = importer;
	  }

	@Override
	public RulesProfile createProfile(ValidationMessages messages) {
		 RulesProfile profile = importer.importProfile(null, messages);
		  profile.setName("JlinProfile_sonar");
	      profile.setLanguage(Java.KEY);
	      
		 return profile;
	}
	

}
