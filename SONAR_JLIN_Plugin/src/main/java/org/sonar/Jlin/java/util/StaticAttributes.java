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
package org.sonar.Jlin.java.util;

import java.io.File;

import org.sonar.Jlin.java.ApiCompatibility.UsageOfAPI;


/**
 * @author C5242815
 * @param <UsageOfAPI>
 *
 */
public class StaticAttributes{
	public static String[] apiFiles={"\\\\production2.wdf.sap.corp\\naas\\java\\750\\NW.javadoc\\NW750EXT_SP_COR\\gen\\dbg\\java\\packaged\\public\\_pack_api\\api.jar"
			,"\\\\production2.wdf.sap.corp\\naas\\java\\750\\NW.javadoc\\NW750EXT_SP_COR\\gen\\dbg\\java\\packaged\\public\\_pack_api_CE\\api.jar"
			,"\\\\production2.wdf.sap.corp\\naas\\java\\750\\NW.javadoc\\NW750EXT_SP_COR\\gen\\dbg\\java\\packaged\\public\\_pack_api_EP\\api.jar"
			,"\\\\production2.wdf.sap.corp\\naas\\java\\750\\NW.javadoc\\NW750EXT_SP_COR\\gen\\dbg\\java\\packaged\\public\\_pack_api_KMC\\api.jar"
			,"\\\\production2.wdf.sap.corp\\naas\\java\\750\\NW.javadoc\\NW750EXT_SP_COR\\gen\\dbg\\java\\packaged\\public\\_pack_api_PI\\api.jar"};
	
	public static String[] classListFiles1 = {"\\\\production2.wdf.sap.corp\\naas\\java\\750\\nw.api.baselines\\NW750CORE_SP_COR\\gen\\dbg\\java\\packaged\\public\\_750_SP_initial\\NetWeaverEP\\com.sap.ep_classes.csv",
			"\\\\production2.wdf.sap.corp\\naas\\java\\750\\nw.api.baselines\\NW750CORE_SP_COR\\gen\\dbg\\java\\packaged\\public\\_750_SP_initial\\NetWeaverCE\\com.sap.NetWeaverCE_classes.csv",
			"\\\\production2.wdf.sap.corp\\naas\\java\\750\\nw.api.baselines\\NW750CORE_SP_COR\\gen\\dbg\\java\\packaged\\public\\_750_SP_initial\\NetWeaverKMC\\com.sap.kmc_classes.csv",
			"\\\\production2.wdf.sap.corp\\naas\\java\\750\\nw.api.baselines\\NW750CORE_SP_COR\\gen\\dbg\\java\\packaged\\public\\_750_SP_initial\\NetWeaverPI\\com.sap.pi_classes.csv"};
	
	
	public static String[] baselineFiles ={"\\\\production2.wdf.sap.corp\\naas\\java\\750\\nw.api.baselines\\NW750CORE_SP_COR\\gen\\dbg\\java\\packaged\\public\\_750_SP_initial\\NetWeaverCE\\api.jar"
			,"\\\\production2.wdf.sap.corp\\naas\\java\\750\\nw.api.baselines\\NW750CORE_SP_COR\\gen\\dbg\\java\\packaged\\public\\_750_SP_initial\\NetWeaverEP\\api.jar"
			,"\\\\production2.wdf.sap.corp\\naas\\java\\750\\nw.api.baselines\\NW750CORE_SP_COR\\gen\\dbg\\java\\packaged\\public\\_750_SP_initial\\NetWeaverKMC\\api.jar"
			,"\\\\production2.wdf.sap.corp\\naas\\java\\750\\nw.api.baselines\\NW750CORE_SP_COR\\gen\\dbg\\java\\packaged\\public\\_750_SP_initial\\NetWeaverPI\\api.jar"
	};
	public static String[] usageNoCall ={"@SAPWebDynproPart 4"
    ,"@sap.NoApi"
    ,"@noreference"
    ,"@sap.ApiForInheritance"
    ,"@sap.ApiForInheritanceCallingSuper"
    ,"@noimplement"
    ,"@noextend"
    ,"@nooverride"};
	public static String[] usageNoInherit ={"@SAPWebDynproPart 2"
    ,"@SAPWebDynproPart 4"
    ,"@sap.NoApi"
    ,"@noimplement"
    ,"@noextend"
    ,"@nooverride"
    ,"@sap.ApiForReference"
    ,"@noreference"};
	
	public static File KnownIncompatabilities=new File("\\\\production.wdf.sap.corp\\depot\\info\\jlin\\variants\\711\\knownIncompatibilities730.properties");
	
	public static String compRules="RELEASE_COMPATIBILITY";
	
	public static UsageOfAPI data = null;

	
	

}
