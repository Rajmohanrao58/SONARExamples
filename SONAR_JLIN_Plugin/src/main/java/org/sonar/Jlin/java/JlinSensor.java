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
 * Created on October 10, 2015
 */
package org.sonar.Jlin.java;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.eclipse.equinox.launcher.Main;
import org.sonar.Jlin.java.ApiCompatibility.APICompatibilityUtil;
import org.sonar.Jlin.java.ApiCompatibility.IUsageOfAPI;
import org.sonar.Jlin.java.ApiCompatibility.MessageConverterLocal;
import org.sonar.Jlin.java.ApiCompatibility.StaticMessageFlags;
import org.sonar.Jlin.java.ApiCompatibility.UsageOfAPI;
import org.sonar.Jlin.java.util.StaticAttributes;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.java.api.JavaResourceLocator;
import org.sonar.Jlin.java.util.SensorUtil;


/**
 * @author rust
 * 
 */
public class JlinSensor implements Sensor {
	private static final Logger LOG = Loggers.get(JlinSensor.class);

	private  static  JavaResourceLocator javaResourceLocator;
//	private final File reportFile;
	private final FileSystem fs;

	private static  Set<String> activeXanRuleNames = new HashSet<>();

	private static   Map<String, NewIssue> createIssues = new HashMap<>();
	private static   Map<String, NewIssue> alreadyCreatedIssues = new HashMap<>();

	private static List<File> classFilesToAnalyze = null;
	public static  Project projectSonar;
	public static  SensorContext sensorContext;
	public static boolean extractFlag;
	/**
	 * The Xanitizer sensor
	 * 
	 * @param javaResourceLocator
	 * @param settings
	 * @param activeRules
	 * @param sensorContext
	 */
	public JlinSensor(final JavaResourceLocator javaResourceLocator, final Settings settings,
			final ActiveRules activeRules, final SensorContext sensorContext, final FileSystem fs) {
		this.javaResourceLocator = javaResourceLocator;
        this.fs=fs;
		for (final ActiveRule activeRule : activeRules.findAll()) {
			if (activeRule.ruleKey().repository().equals(JlinRulesDefinition.APICOMP_REPOSITORY_KEY)) {
				final String ruleAsString = activeRule.ruleKey().rule();
				activeXanRuleNames.add(ruleAsString);
			}
			
		
		}
	/*	if (activeXanRuleNames.isEmpty()) {
			
			 * If no rule is active, we do not need to read the report file
			 
			this.reportFile = null;
		} else {
			this.reportFile = SensorUtil.geReportFile(sensorContext, settings);
		}*/
	}

	@Override
	public boolean shouldExecuteOnProject(final Project project) {
		return  !activeXanRuleNames.isEmpty();
	}

	@Override
	public void analyse(final Project project, final SensorContext sensorContext) {
		//assert reportFile != null;
		assert !activeXanRuleNames.isEmpty();
		projectSonar=project;
		this.sensorContext=sensorContext;
		Map<String,Set<String>> mapToFill = new HashMap<String,Set<String>>();
		Set<File> completeApiFiles = new HashSet<File>();
	
		
        try {
        	mapToFill=APICompatibilityUtil.evalClasslistParam(mapToFill);
			if(!checkClassfiles(javaResourceLocator, mapToFill)){
				LOG.info(" no API classes in this project ");
			}
			else{
				for(String file : StaticAttributes.apiFiles){
				completeApiFiles.add(new File(file));
				}
				File tempDirRoot = fs.workDir();
				File tempDirEclipse = new File(tempDirRoot,"eclipse");
				tempDirEclipse.mkdirs();
				File tempDirNew = new File(tempDirRoot,"new");
				tempDirNew.mkdirs();
				File tempDirResults = new File(tempDirRoot,"results");
				tempDirResults.mkdirs();
				copyConfiguration("C:\\maketools\\var\\ac\\1.0\\83ss2xwclogmabdv2e09aw6l9\\a506rqxjgudsb5sdyxj4lmobz\\data\\_\\jlin",tempDirEclipse);
				File eclipseFolder2 = new File(tempDirEclipse,"eclipse");
				File configFolder = new File(eclipseFolder2,"configuration");
				if((!configFolder.exists())|| (!configFolder.canWrite())){
					throw new RuntimeException(" Configuration directory invalid or not writable "+tempDirEclipse);
				}		
//				File tempDirF = super.getTempDir(getTestName());
				String tempDirS = tempDirEclipse.getCanonicalPath();
				// extract complete (current) api.jar into temp folder. Classes from this project will
				// overwrite the old values and in the result we have a complete api.jar
				// without missing classes. The mixture is not completely consistent, as some classes
				// are from NW.javadoc and therefore from the last nightly build, while others are
				// from this project (today). That's best we can get, but might create false positives
				// when running in DevStudio during a big refactoring. 
				extractCompleteAPI(completeApiFiles);
				// copy API classes to temp.
				
				copyClassfiles(javaResourceLocator,mapToFill);
				
				// Jar all classes in temp
				
				File folderClasses = fs.workDir();
				jarClasses(folderClasses, tempDirNew,null);
				
				for (String baseLine:StaticAttributes.baselineFiles){
					File baseLineFile= new File(baseLine);
					compareAndReport(baseLineFile, tempDirS, tempDirNew.getCanonicalPath(),tempDirResults.getCanonicalPath());
				}
				
				
			
				
				
				
				
				
				
				
				
				
				
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	

		//createIssuesAndMeasures(project, sensorContext, content);
	}

	public static void creteIssueFromJlinMessgae(StaticMessageFlags staticMessageFlags){
		createIssues = new HashMap<>();
		createIssuesAndMeasures(projectSonar, sensorContext, staticMessageFlags);
	}
	@SuppressWarnings("rawtypes")
	private static void createIssuesAndMeasures(final Project project, final SensorContext sensorContext,
			final StaticMessageFlags staticMessageFlags) {

		final Map<Metric, Map<Resource, Integer>> metricValues = new LinkedHashMap<>();

		// Generate issues for findings.
	
			boolean issueGenerated=generateIssueForFinding(staticMessageFlags, metricValues, project, sensorContext);
		
		
		for (final NewIssue issue : createIssues.values()) {
		for (final NewIssue alreadyissue : alreadyCreatedIssues.values()){
			
		}
		if(issueGenerated){
			issue.save();
		}
			
		}

		// Metrics: Counts of different findings.
		for (final Map.Entry<Metric, Map<Resource, Integer>> e : metricValues.entrySet()) {
			final Metric metric = e.getKey();
			for (final Map.Entry<Resource, Integer> e1 : e.getValue().entrySet()) {
				final Resource resource = e1.getKey();
				final Integer value = e1.getValue();

				if (value != 0) {
					final Measure measure = new Measure(metric, value.doubleValue());
					LOG.debug("Creating measure for metric " + measure.getMetricKey()
							+ ": adding value = " + value + " to resource " + resource.getName());
					sensorContext.saveMeasure(resource, measure);
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static boolean  generateIssueForFinding(final StaticMessageFlags staticMessageFlags,
			final Map<Metric, Map<Resource, Integer>> metricValuesAccu, final Project project,
			final SensorContext sensorContext) {
	
		if (!activeXanRuleNames.contains(staticMessageFlags._messageId)) {
			
			return false;
		}
	
		final InputFile inputFile = mkInputFileOrNull(staticMessageFlags, sensorContext);
		if (inputFile == null) {
			/*
			 * Do not generate issues without code location
			 */
			

			return false;
		}

		final boolean issueCreated = createNewIssue(inputFile, staticMessageFlags, sensorContext);

		if (issueCreated) {
			
			System.out.println("Issue Created");
			return true;
			//incrementMetrics(xanFinding, metricValuesAccu, project, sensorContext, inputFile);
		}
		return false;
		
	}

	/*@SuppressWarnings("rawtypes")
	private static void incrementMetrics(final StaticMessageFlags staticMessageFlags,
			final Map<Metric, Map<Resource, Integer>> metricValuesAccu, final Project project,
			final SensorContext sensorContext, final InputFile inputFile) {
		final Severity severity = SensorUtil.mkSeverity(staticMessageFlags);
		final Resource resourceToBeUsed = sensorContext.getResource(inputFile);

		final List<Metric> metrics = mkMetrics(xanFinding.getProblemType());
		for (final Metric metric : metrics) {
			incrementValueForResourceAndContainingResources(metric, resourceToBeUsed, project,
					metricValuesAccu);
		}

		final String matchCode = xanFinding.getMatchCode();
		if ("NOT".equals(matchCode)) {
			incrementValueForResourceAndContainingResources(
					XanitizerMetrics.getMetricForNewXanFindings(), resourceToBeUsed, project,
					metricValuesAccu);
		}

		final Metric metricForSeverity = XanitizerMetrics.getMetricForSeverity(severity);
		if (metricForSeverity != null) {
			incrementValueForResourceAndContainingResources(metricForSeverity, resourceToBeUsed,
					project, metricValuesAccu);
		}
	}
*/
	/*
	 * In SonarQube, line numbers must be strictly positive.
	 */
	/*private static int normalizeLineNo(final int lineNo) {
		if (lineNo <= 0) {
			return 1;
		}
		return lineNo;
	}
*/
	
	private static InputFile mkInputFileOrNull(final StaticMessageFlags staticMessageFlags,
			final SensorContext sensorContext) {

		if (staticMessageFlags== null || staticMessageFlags.equals(" ")) {
			return null;
		}
		
		final InputFile result = mkInputFileOrNullFromClass(staticMessageFlags, sensorContext);
		if (result == null) {
			//return mkInputFileOrNullFromPath(staticMessageFlags, sensorContext);
		}

		return result;
	}

	private static InputFile mkInputFileOrNullFromClass(final StaticMessageFlags staticMessageFlags,
			final SensorContext sensorContext) {

		final String classFQNOrNull = staticMessageFlags._packageAndClass;
		if (classFQNOrNull == null) {
			return null;
		}
		/*
		 * The API of the Java plugin has changed and returns InputFile directly
		 * since 4.0. To support previous versions, use reflection here and do
		 * an instance check.
		 */
		try {
			final Method findResource = javaResourceLocator.getClass()
					.getDeclaredMethod("findResourceByClassName", String.class);
			final Object resource = findResource.invoke(javaResourceLocator, classFQNOrNull);

			if (resource instanceof InputFile) {
				return (InputFile) resource;
			}

			if (resource instanceof Resource) {
				return mkInputFileOrNullFromResource((Resource) resource, sensorContext);
			}
		} catch (Exception e) {
			LOG.error("Could not call method 'findResourceByClassName' on Java resource locator!",
					e);
		}
		return null;
	}

	/*private static InputFile mkInputFileOrNullFromPath(final XMLReportNode node,
			final SensorContext sensorContext) {

		final FileSystem fs = sensorContext.fileSystem();

		
		 * First check, if the absolute file exists on the machine and then try
		 * to detect it in the project context
		 
		final String originalAbsoluteFile = node.getAbsolutePathOrNull();
		if (originalAbsoluteFile != null && new File(originalAbsoluteFile).isFile()) {
			final Iterable<InputFile> inputFilesIterable = sensorContext.fileSystem()
					.inputFiles(fs.predicates().hasAbsolutePath(originalAbsoluteFile));

			// Use first matching input file
			for (final InputFile inputFile : inputFilesIterable) {
				return inputFile;
			}
		}

		
		 * If the absolute path does not exist, create the relative path from
		 * the persistence string
		 
		final String relativePath = node.getRelativePathOrNull();
		if (relativePath != null) {
			final File absoluteFile = new File(fs.baseDir(), relativePath);
			final String absoluteFilePath = absoluteFile.getAbsolutePath();

			final Iterable<InputFile> inputFilesIterable = sensorContext.fileSystem()
					.inputFiles(fs.predicates().hasAbsolutePath(absoluteFilePath));

			// Use first matching input file
			for (final InputFile inputFile : inputFilesIterable) {
				return inputFile;
			}

		}
		return null;
	}
*/
	private static InputFile mkInputFileOrNullFromResource(final Resource resource,
			final SensorContext sensorContext) {
		final String relativePath = resource.getPath();
        System.out.println("In Testing mkInputFileOrNullFromResource");
		final FileSystem fs = sensorContext.fileSystem();

		final File absoluteFile = new File(fs.baseDir(), relativePath);
		final String absoluteFilePath = absoluteFile.getAbsolutePath();

		/*
		 * SonarQube 5.4 seems to sometimes enter an endless loop here, if a
		 * non-trivial predicate is given.
		 */
		
		final Iterable<InputFile> inputFilesIterable = sensorContext.fileSystem()
				.inputFiles(fs.predicates().hasAbsolutePath(absoluteFilePath));

		// Use first matching input file, or none.
		for (final InputFile inputFile : inputFilesIterable) {
			return inputFile;
		}

		return null;
	}

	private static  boolean createNewIssue(final InputFile inputFile, final StaticMessageFlags staticMessageFlags,
			final SensorContext sensorContext) {

		//final GeneratedProblemType pt = staticMessageFlags;
		final RuleKey ruleKey = RuleKey.of(JlinRulesDefinition.APICOMP_REPOSITORY_KEY, staticMessageFlags._messageId);
		
		
	//	final int lineNo = normalizeLineNo(xanFinding.getLocation().getLineNoOrMinus1());
		final Severity severity = SensorUtil.mkSeverity(staticMessageFlags);

		
	/*	final NewIssue alreadyCreatedIssue = alreadyCreatedIssues.get(issueKey);
		if (alreadyCreatedIssue != null) {

			addSecondaryLocation(alreadyCreatedIssue, staticMessageFlags, sensorContext);

			LOG.debug("Issue already exists: " + inputFile +"-"
					+  staticMessageFlags._messageId);
			return false;
		}*/

		final NewIssue newIssue = sensorContext.newIssue();
		newIssue.forRule(ruleKey);
		newIssue.overrideSeverity(severity);
		
       
		final NewIssueLocation newIssueLocation = newIssue.newLocation();
		newIssueLocation.on(inputFile);

		// If line number exceeds the current length of the file,
		// SonarQube will crash. So check length for robustness.
		//if (lineNo <= inputFile.lines()) {
		
			final TextRange textRange = inputFile.selectLine(1);
			newIssueLocation.at(textRange);
		//}

		newIssueLocation.message(staticMessageFlags._message);
		newIssue.at(newIssueLocation);
		final String issueKey = mkIssueKey(ruleKey, inputFile,staticMessageFlags._message);
		//addSecondaryLocation(newIssue, staticMessageFlags, sensorContext);

		createIssues.put(issueKey, newIssue);
		
		for(int i=0; i< alreadyCreatedIssues.size(); i++){
			
		}
		if(alreadyCreatedIssues.containsKey(issueKey)){
			return false;
		}
		
			alreadyCreatedIssues.put(issueKey, newIssue);
		
		
		System.out.println("Issue saved: " + inputFile + ":" +staticMessageFlags._messageId);
		
		return true;
	}

	/*private static void addSecondaryLocation(final NewIssue issue, final StaticMessageFlags staticMessageFlags,
			final SensorContext sensorContext) {
		final InputFile secondaryFile = mkInputFileOrNull(staticMessageFlags,
				sensorContext);
		if (secondaryFile != null) {
			final NewIssueLocation secondaryLocation = issue.newLocation();
			secondaryLocation.on(secondaryFile);
			secondaryLocation.message(staticMessageFlags._message);
			final int secondaryLine = normalizeLineNo(
					xanFinding.getSecondaryLocationOrNull().getLineNoOrMinus1());
			if (secondaryLine <= secondaryFile.lines()) {
				final TextRange textRange = secondaryFile.selectLine(1);
				secondaryLocation.at(textRange);
			//}
			issue.addLocation(secondaryLocation);
			
			LOG.debug("Added secondary location for finding " );
		}
	}*/

	private static String mkIssueKey(final RuleKey ruleKey, final InputFile file, final String message) {
		return ruleKey.toString() + ":" + file.toString()+":"+message ;
	}

/*	@SuppressWarnings("rawtypes")
	private static  List<Metric> mkMetrics(final StaticMessageFlags staticMessageFlags) {
		final List<Metric> result = new ArrayList<>();
		result.add(XanitizerMetrics.getMetricForAllXanFindings());

		final Metric metricOrNull = XanitizerMetrics.mkMetricForProblemType(staticMessageFlags);
		if (metricOrNull != null) {
			result.add(metricOrNull);
		}

		return result;
	}
*/
	/*@SuppressWarnings("rawtypes")
	private static void incrementValueForResourceAndContainingResources(final Metric metric,
			final Resource resource, final Project project,
			final Map<Metric, Map<Resource, Integer>> metricValuesAccu) {
		Resource runner = resource;
		while (runner != null) {
			incrementValue(metric, runner, metricValuesAccu);
			if (ResourceUtils.isFile(runner)) {
				// Go to directory.
				runner = runner.getParent();
			} else if (ResourceUtils.isDirectory(runner)) {
				// Go to project.
				runner = project;
			} else {
				// This is a project. No container.
				runner = null;
			}
		}
	}*/

	/*@SuppressWarnings("rawtypes")
	private static void incrementValue(final Metric metric, final Resource resource,
			final Map<Metric, Map<Resource, Integer>> metricValuesAccu) {
		Map<Resource, Integer> innerMap = metricValuesAccu.get(metric);
		if (innerMap == null) {
			innerMap = new LinkedHashMap<>();
			metricValuesAccu.put(metric, innerMap);
		}

		Integer value = innerMap.get(resource);
		if (value == null) {
			value = 0;
		}

		innerMap.put(resource, 1 + value);
	}*/

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	
	
	private static boolean checkClassfiles(JavaResourceLocator javaResourceLocator,Map<String,Set<String>> allApiClasses) throws IOException{
		  classFilesToAnalyze= new ArrayList<>(javaResourceLocator.classFilesToAnalyze());
		 

		    for (File file : javaResourceLocator.classpath()) {
		      //Will capture additional classes including precompiled JSP
		      if(file.isDirectory()) { // will include "/target/classes" and other non-standard folders
		        classFilesToAnalyze.addAll(scanForAdditionalClasses(file));
		      }

		     
		    }

		  
		boolean result = false;
		for(String csvName:allApiClasses.keySet()){
			Set<String> apiClasses = allApiClasses.get(csvName);
			for(String packageAndClass:apiClasses){
				for(File classFile:classFilesToAnalyze){
					
					String tmp = packageAndClass.replace('.', File.separatorChar);
					
					
					String tmp1 = tmp+".class";
					
					if(classFile.getAbsoluteFile().toString().contains(tmp1)){
										
											result = true;
					}
				}
			}
		}
		return result;
		
		
	}
	
	  private static List<File> scanForAdditionalClasses(File folder) throws IOException {
		    List<File> allFiles = new ArrayList<File>();
		    Queue<File> dirs = new LinkedList<File>();
		    dirs.add(folder);
		    while (!dirs.isEmpty()) {
		      File dirPoll = dirs.poll();
		      if(dirPoll == null) break; //poll() result could be null if the queue is empty.
		      for (File f : dirPoll.listFiles()) {
		        if (f.isDirectory()) {
		          dirs.add(f);
		        } else if (f.isFile()&& f.getName().endsWith(".class")) {
		          allFiles.add(f);
		        }
		      }
		    }
		    return allFiles;
		  }
	  
	  private void copyClassfiles(JavaResourceLocator javaResourceLocator, Map<String,Set<String>> apiClasses) throws IOException{
		  System.out.println("In CopyClassFiles");
			File tempDir = fs.workDir();
			for(String csvName:apiClasses.keySet()){
				Set<String> apiClass = apiClasses.get(csvName);
				for(String packageAndClass:apiClass){
					copyClassToTemp(packageAndClass, //csvName,
							javaResourceLocator,tempDir);
				}
			}
		}
	  
	  private void copyClassToTemp(String packageAndClass, //String csvName,
			  JavaResourceLocator javaResourceLocator, File tempDir) throws IOException {
		  
		  
		 

		   
		  
			for(File classFile:classFilesToAnalyze){
				
				String tmp = packageAndClass.replace('.', File.separatorChar);
				
				
				String tmp1 = tmp+".class";
				
				if(classFile.getAbsoluteFile().toString().contains(tmp1)){
					File toFile = new File(tempDir,tmp1);
					File toDir = toFile.getParentFile();
					toDir.mkdirs();
					String tmp2 = classFile.getAbsolutePath();
					String tmp3 = toFile.getAbsolutePath();
					APICompatibilityUtil.copyFile(tmp2,tmp3);
				}
			}
			
			
			
			
		}
	  
	  private void copyConfiguration(String toolsFolder, File tempDir) {
			System.out.println("Tools Folder"+toolsFolder);
			File toolsDir = new File(toolsFolder);
			File eclipseDir = new File(toolsDir,"eclipse");
			String jdkVersion = System.getProperty("java.version");
			if(jdkVersion.matches("1.8.*$")){
			eclipseDir = new File(toolsDir,"eclipse750");
			}
			
			File configDir = new File(eclipseDir,"configuration");
//			File configDir = new File("D:\\Raj\\p5556\\APICompatabiltiyChanges\\kmgmt_ext\\nw.api.reporting\\NW750EXT_04_COR\\gen\\dbg\\java\\packaged\\full\\_install_comparetool\\tool\\eclipse\\configuration");
			File tmp = new File(tempDir,"eclipse");
			File target = new File(tmp,"configuration");
			target.mkdirs();
			System.out.println("Copying eclipse configuration files to "+target.getAbsolutePath());
			APICompatibilityUtil.copyDir(configDir,target);
		}
	  
		private void extractCompleteAPI(Set<File> completeApiFiles){
			File tempDir1 = fs.workDir();
			File tempDir = new File(fs.baseDir().getAbsolutePath()+"\\ExtactedAPI");
			
			if(extractFlag == false){
			for(File apiName:completeApiFiles){
			System.out.println("Extracting.."+apiName.getAbsolutePath());
				APICompatibilityUtil.extractZip(apiName, tempDir);
			}
			extractFlag=true;
			}
			APICompatibilityUtil.copyDir(tempDir, tempDir1);
		}
		
		private void jarClasses(File inDir, File jarDir, File exceptionList) throws IOException {
		    String tempDirName = inDir.getAbsolutePath();
		    int tempDirLength = tempDirName.length();
		    Set<File> fileList = new HashSet<File>();
		    getClassFiles(inDir, fileList);
	        if (fileList != null && !fileList.isEmpty()) {
	        	String jarFileName = jarDir + File.separator + "api.jar";
		        File jarFile = new File(jarFileName);

		        // delete old version of file that might exist from last run
		        if(jarFile.exists()){
		        	jarFile.delete();
		        }
		        FileOutputStream out1 = null;
		        JarOutputStream out = null;
		        try {
		        	out1 = new FileOutputStream(jarFile);
		        	out = new JarOutputStream(out1);
		          //  System.out.println(getTestName()+" Creating jar file " + jarFileName);
		            Iterator<File> iter = fileList.iterator();
		            while ( iter.hasNext()) {	
		            	File classFile = iter.next();
		            	jarOneFile(classFile, out, tempDirLength, true);
		            }
		            // add the exception list if it is defined.
		            if(exceptionList!=null&&exceptionList.length()>0){
		            	File apiFolder = exceptionList.getParentFile();
		            	File parent = apiFolder.getParentFile(); // parent of "api" folder
		            	String parentName = parent.getAbsolutePath();
		            	int length = parentName.length();
		            	jarOneFile(exceptionList,out,length, false);
		            }
		           // System.out.println(getTestName()+" Created jar file " + jarFileName);
		        } catch (Exception ex) {
		        	System.err.println(ex.getMessage());
		            ex.printStackTrace();
		            throw new RuntimeException(ex.getMessage());//$JL-EXC$
		        }
		        finally{
		        	if(out!=null){
		        		out.close();  
		        	}
		        	if(out1!=null){
		        		out1.close();  
		        	}
	        }
		}
		}
		
		private void getClassFiles(File fileOrFolder,Set<File> result) {
			if(fileOrFolder.isDirectory()){
				File[] children = fileOrFolder.listFiles();
				for (File oneChild:children){
					getClassFiles(oneChild, result);
				}
			}
			else{
				String name = fileOrFolder.getAbsolutePath();
				if(name.endsWith(".class")){
					result.add(fileOrFolder);
				}
			}
		}
		
		
		private static void jarOneFile(File classFile,JarOutputStream out, int tempDirLength, boolean deleteSource) throws IOException{
			FileInputStream in1 = null;
			BufferedInputStream in = null;
			try{
				in1 = new FileInputStream(classFile);
		        in = new BufferedInputStream(in1);
	        String jarEntry = classFile.getAbsolutePath().substring(tempDirLength);
	        if (jarEntry.startsWith(File.separator)){
	            jarEntry = jarEntry.substring(1);
	        }
	        String jarEntry2 = jarEntry.replace(File.separatorChar, '/');// Bug in Eclipse Tool?
	        out.putNextEntry(new JarEntry(jarEntry2));
	            
	        // Transfer bytes from the file to the ZIP file
	        int len;
	        byte[] buf = new byte[1024];
	        while((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        // Complete the entry
	        out.closeEntry();
			}
			finally{
				if(in!=null){
	        in.close();
				}
				if(in1!=null){
					in1.close();
				}
			}
	        if(deleteSource){
	            if (!classFile.delete()){
	                System.err.println("File " + classFile + " cannot be deleted !");
	            }
	        }
		}
		
		
		
		private void compareAndReport(File baseLine, String tempDirEclipse, String tempDirDataNew , String tempDirResults) {
		//	System.out.println(getTestName()+" starting compareAndReport");

			//StaticAttributes attr = StaticAttributes.getAttributes(getTestName());
			// Start comparison, call eclipse tool
			File tmp1New = new File(tempDirDataNew+File.separator+"api.jar");
			if (!tmp1New.exists()){
				throw new RuntimeException(" local api.jar does not exist. "+tmp1New.getAbsolutePath());
			}
			URI tmpNew = tmp1New.toURI();
			String currentFile = tmpNew.toASCIIString();
			//String currentFile = "file:///C:/p3227/buildenv/BE.JLin/ct01all_stream/gen/dbg/java/packaged/public/_tc~jtools~jlin~java~globalanalysis/new/api.jar";
			URI tmp2Old = baseLine.toURI();
//			URI tmp2 = new URI("file",null, baseLineFileName,"version=1.0.0",null);
			String baseline = tmp2Old.toASCIIString();
			//String baseline = "file:///C:/p3227/buildenv/BE.JLin/ct01all_stream/gen/dbg/java/packaged/public/_tc~jtools~jlin~java~globalanalysis/old/api.jar";
			String newVersion = "1.1.0"; // default: EHP-Compatibility.
		/*	if ("SP_COMPATIBILITY".equalsIgnoreCase(attr.compatRules)){
				newVersion = "1.0.1";
			}
			else{
				if ("PATCH_COMPATIBILITY".equalsIgnoreCase(attr.compatRules)){
					newVersion = "1.0.1";
				}
				else{
					
				}			
			}*/
			
			String jdkVersion = System.getProperty("java.version");
			String installLocation = (jdkVersion.matches("1.8.*$")) ? "C:\\maketools\\var\\ac\\1.0\\83ss2xwclogmabdv2e09aw6l9\\a506rqxjgudsb5sdyxj4lmobz\\data\\_\\jlin"+File.separator+"eclipse750" : "C:\\maketools\\var\\ac\\1.0\\83ss2xwclogmabdv2e09aw6l9\\a506rqxjgudsb5sdyxj4lmobz\\data\\_\\jlin"+File.separator+"eclipse" ;
			String[] args = {
//					"-consolelog", // writes many stacktraces, even so it works in the end. Maybe usefull for debugging if it does not work.
					"-debug",
					"-configuration",tempDirEclipse+"/eclipse/configuration",
					"-install",installLocation,
//					"-install","D:\\Raj\\p5556\\APICompatabiltiyChanges\\kmgmt_ext\\nw.api.reporting\\NW750EXT_04_COR\\gen\\dbg\\java\\packaged\\full\\_install_comparetool\\tool\\eclipse",
//					"-data","C:/tmp/compatibility/tool/tmp/eclipse",
					"-application","com.sap.ide.apitools.Compare",
//					"-baselineFile",baseline,
					"-baselineFile",baseline+"?version=1.0.0",
//					"-currentFile",currentFile,
					"-currentFile",currentFile+"?version="+newVersion,
					"-reportFolder",tempDirResults,
					"-profileFactory","com.sap.ide.apitools.jarcomponent.jarFactory",
					"-currentVersion",newVersion};
			Main launcher = new Main();
		/*	if(isVerbose()){
				for(String tmp3:args){
					System.out.println(tmp3);
				}
			}*/
			int result = launcher.run(args);
			// Read results file and convert it into JLin messages
			String eclipseFile = tempDirResults+File.separator+"apiReport.xml";
			File tmp3 = new File(eclipseFile);
			//System.out.println(" Finished call to Eclipse, Result = "+result);
			int numEclipseMessages = 0;
			int numJLinMessages = 0;
			if (tmp3.exists()){
				System.out.println(" Converting messages ");
				
				IUsageOfAPI usage = StaticAttributes.data;
				MessageConverterLocal convert = new MessageConverterLocal(usage);
			  //  if(isVerbose()){
			  //  	convert.setVerbose(true);
			  //  }
				convert.setDebug(false);
//		MessagePriority priorities = new MessagePriority(attr.compatRules);
				/*IMessageWriter writer = new MessageWriterJLin(this,
						attr.apiClassesThisProject, attr.baseLink, super.isVerbose());*/
				//convert.initWriter(writer);
			    convert.setEclipseFile(eclipseFile);
			    if(StaticAttributes.KnownIncompatabilities!=null){
			    	convert.setKnownErors(StaticAttributes.KnownIncompatabilities);
			    }
//				convert.setCompatibilityRules();
				convert.execute();		
				numEclipseMessages = convert.getEclipseMessageCount();
				numJLinMessages = convert.getJLinMessageCount();
			}
			
			System.out.println(" Finished call to Eclipse, Result = "+result);
		
			//System.out.println(" Converting done for eclipse messages into  JLin messages");
		}

		
		public static void  initDataStaticAttribute(){
			 
			  Map<String, IUsageOfAPI.CodeClasses> classUsage;
		        Map<String, IUsageOfAPI.CodeClasses> elementUsage;
				classUsage = new HashMap<String, IUsageOfAPI.CodeClasses>();
				elementUsage = new HashMap<String, IUsageOfAPI.CodeClasses>();
				StaticAttributes.data=new UsageOfAPI(classUsage, elementUsage);
		  }
}
