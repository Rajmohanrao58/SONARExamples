package org.sonar.Jlin.java.util;


import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sonar.Jlin.java.ApiCompatibility.IUsageOfAPI;
import org.sonar.Jlin.java.ApiCompatibility.MessageConverterLocal;
import org.sonar.Jlin.java.ApiCompatibility.UsageOfAPI;
import org.sonar.Jlin.java.checks.RestrictedComponentCheck;



public class Test {
	Set<File> completeApiFiles = new HashSet<File>();
	String[] apiFiles={"\\\\production2.wdf.sap.corp\\naas\\java\\750\\NW.javadoc\\NW750EXT_SP_COR\\gen\\dbg\\java\\packaged\\public\\_pack_api\\api.jar"
			,"\\\\production2.wdf.sap.corp\\naas\\java\\750\\NW.javadoc\\NW750EXT_SP_COR\\gen\\dbg\\java\\packaged\\public\\_pack_api_CE\\api.jar"
			,"\\\\production2.wdf.sap.corp\\naas\\java\\750\\NW.javadoc\\NW750EXT_SP_COR\\gen\\dbg\\java\\packaged\\public\\_pack_api_EP\\api.jar"
			,"\\\\production2.wdf.sap.corp\\naas\\java\\750\\NW.javadoc\\NW750EXT_SP_COR\\gen\\dbg\\java\\packaged\\public\\_pack_api_KMC\\api.jar"
			,"\\\\production2.wdf.sap.corp\\naas\\java\\750\\NW.javadoc\\NW750EXT_SP_COR\\gen\\dbg\\java\\packaged\\public\\_pack_api_PI\\api.jar"};

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		RestrictedComponentCheck r=new RestrictedComponentCheck();
		r.initialize();
		
	}
	public static void  initDataStaticAttribute(){
		 
		  Map<String, IUsageOfAPI.CodeClasses> classUsage;
	        Map<String, IUsageOfAPI.CodeClasses> elementUsage;
			classUsage = new HashMap<String, IUsageOfAPI.CodeClasses>();
			elementUsage = new HashMap<String, IUsageOfAPI.CodeClasses>();
			StaticAttributes.data=new UsageOfAPI(classUsage, elementUsage);
	  }
}
