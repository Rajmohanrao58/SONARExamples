package org.sonar.Jlin.java.ApiCompatibility;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.sonar.Jlin.java.util.StaticAttributes;

public class APICompatibilityUtil {
//	private static final String TIMESTAMP_FILENAME = "JLinTimestamps.properties";
	//private static boolean extractingMessageGenerated = false;
	public static Set<String>  readFile(File oneListFile) {
		Set<String> result;
System.out.println("checking file "+oneListFile.getName());
		String name = oneListFile.getName();
		int beginIndex = name.length();
		String name2 = name.substring(beginIndex-3);
		if("zip".equalsIgnoreCase(name2)){
			result = readZipFile(oneListFile);
		}
		else{
			if("csv".equalsIgnoreCase(name2)){
				result = readCSVFile(oneListFile);
			}
			else{
				throw new RuntimeException("unknown extension; expected csv or zip, file "+oneListFile);
			}
		}
System.out.println("checking file "+oneListFile.getName() +" has enties: "+result.size());
		return result;
	}
	
	// Read a csv file with classnames and store it in a set
		private static Set<String> readCSVFile(File oneListFile) {
			Set<String> result = new HashSet<String>();
			try {
	//System.out.println("checking file "+oneListFile.getName());
				FileReader tmp2=null;
				BufferedReader rd=null;
				try{
		        tmp2 = new FileReader(oneListFile);
		        rd = new BufferedReader(tmp2);
		        String oneLine = rd.readLine();
		        while(oneLine!=null){
		        	result.add(oneLine);
//		        	if(oneLine.indexOf('$')>0){
//		        		System.out.println("Inner Class found "+oneLine);
//		        	}
		        	oneLine = rd.readLine();
		        }
				}
				finally{
					if(rd!=null){
						rd.close();
					}
					if(tmp2!=null){
						tmp2.close();
					}
				}
			} catch (FileNotFoundException e) {
				//Properties params = new Properties();
				//params.put("GENERAL_MESSAGE", "File not found. searching for: "+oneListFile.getAbsolutePath());
				//addError("apidiff.36.Log", params,null);
				e.printStackTrace();
			} catch (IOException e) {
				//Properties params = new Properties();
				//params.put("GENERAL_MESSAGE", "IO Exception while reading: "+oneListFile.getAbsolutePath());
				//addError("apidiff.36.Log", params,null);
				e.printStackTrace();
			}	
			return result;
		}

		// Read a zip file with classnames and store it in a set
		private static Set<String> readZipFile(File oneListFile) {
			Set<String> result = new HashSet<String>();
		    try {
		    	// do extract
		    	InputStream tmp = null;
		    	ZipInputStream zipinputstream = null;
	            try{
	                tmp = new FileInputStream(oneListFile);
	                zipinputstream = new ZipInputStream(tmp);
	                ZipEntry zipentry= zipinputstream.getNextEntry();
	    			while(zipentry!=null) {
	    				InputStreamReader tmp2=null; 
	    				BufferedReader bf=null;
	    				try{
	    					tmp2 = new InputStreamReader(zipinputstream,"UTF-8");// not sure about "UTF-8"
	    					bf = new BufferedReader(tmp2);
//	                      String entryName = zipentry.getName();
//	                      RandomAccessFile rf = new RandomAccessFile(entryName,"r");               
	    					String line;
	    					while ((line =bf.readLine()) !=null){
	    						result.add(line);
	    					}
	                        zipinputstream.closeEntry();
//	                      tmp2.close();
	                        zipentry = zipinputstream.getNextEntry();
	    				}
	    				finally{
	    					if(bf!=null){
	    						bf.close();
	    					}
	    					if(tmp2!=null){
	    						tmp2.close();
	    					}
	    				}
	    		 	}
	            }
	            finally{
	            	if(zipinputstream!=null){
	            		zipinputstream.close();
	            	}
	            	if(tmp!=null){
	            		tmp.close();
	            	}
	            }
//	            bf.close(); 
			} catch (IOException e) {
				//Properties params = new Properties();
				//params.put("GENERAL_MESSAGE", "IO Exception while reading: "+oneListFile.getAbsolutePath());
				//addError("apidiff.36.Log", params,null);
				e.printStackTrace();
			}	
			return result;
		}
		
		public static Map<String,Set<String>> evalClasslistParam(Map<String,Set<String>> mapToFill){
			
			
				
				if (StaticAttributes.classListFiles1!=null){
					for(String oneName:StaticAttributes.classListFiles1){
						File oneListFile = new File(oneName);
						String key;
						Set<String> tmp;
						try {
							key = oneListFile.getCanonicalPath();
							if(mapToFill.containsKey(key)){
								// do nothing, file is already loaded.
							}
							else{
								if(oneListFile.exists()&&oneListFile.isFile()){
									tmp = readFile(oneListFile);
									//tmp.removeAll(ignoreAPIClasses);
									mapToFill.put(key, tmp);
								}				
							}
						} catch (IOException e) {
						//	Properties params = new Properties();
							//params.put("GENERAL_MESSAGE", "IO Exception while reading: "+oneListFile.getAbsolutePath());
						//	addError("apidiff.36.Log", params,null);
							e.printStackTrace(System.out);
						}
					}
				}
				return mapToFill;
			
		}
		
		public static void copyDir(File fromDir, File targetDir) {
			File[] files = fromDir.listFiles();
			//System.out.println("Source Directory"+fromDir);
			//System.out.println("Source Directory size"+files.length);
			for(File oneFile:files){
				String name = oneFile.getName();
				if(oneFile.isDirectory()){
					File target = new File(targetDir,name);
					target.mkdirs();
					copyDir(oneFile,target);
				}
				else{
					String from = oneFile.getAbsolutePath();
					String to = targetDir+File.separator+name;
					copyFile(from, to);
				}
			}
		}
		
		
		public static void copyFile(String fromFileName, String toFileName) {
			File input = new File(fromFileName);
	        long last_mod = input.lastModified();
	        
			File output = new File(toFileName);
			InputStream inStr=null;
			BufferedInputStream in=null;
			FileOutputStream outStr=null;
			BufferedOutputStream out=null;
			try {
				try{
					inStr = new FileInputStream(input);
					in = new BufferedInputStream(inStr);
					outStr = new FileOutputStream(output);
					out = new BufferedOutputStream(outStr);
					int c;
					while((c=in.read())!=-1){
						out.write(c);
					}
		            
		            output.setLastModified(last_mod);
				}
				finally{
					if(inStr!=null){
						inStr.close();
					}
					if(in!=null){
						in.close();
					}
					if(out!=null){
						out.close();
					}
					if(outStr!=null){
						outStr.close();	
					}
				}
			} catch (FileNotFoundException e) {//$JL-EXC$
				// that is not really a bug, but lets inform the user.
			/*	if (log.isVerbose()){
					log.logMessage("Info: unable to copy file "+fromFileName, null);
				}*/
				// JLin does not use SAP logging in this release. Sorry. 
			} catch (IOException e) {//$JL-EXC$
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());//$JL-EXC$
			} 
		}
		
		public static final void extractZip(File zipfileF, File outDir) {
			Enumeration<? extends ZipEntry> entries;
		    try {
		    	
		    	// check if extract is required
		    	/*long lastMod = zipfileF.lastModified();
		    	String key = zipfileF.getName();
				File file = new File(outDir,TIMESTAMP_FILENAME);
				Properties prop = readTimestamps(file);
				String lastExtract = prop.getProperty(key); // ignore path, only name of zip is relevant.
				if (lastExtract!=null){
					long lastExtractL = Long.parseLong(lastExtract);
					if (lastExtractL>=lastMod){
					//	if (log.isVerbose()){
							if (!extractingMessageGenerated){
								//log.logMessage("Not extracting "+key+" buffer "+outDir.getAbsolutePath()+"is up to date. This is the last message for this project", null);
								extractingMessageGenerated = true;
							}
					//	}
						return;
					}
				}
				
	    		prop.setProperty(key,Long.toString(lastMod));*/

//	    		String zipName = zipfileF.getAbsolutePath();
//	    		assumptions.layerRefDirIsUsed(zipName);
//	    		assumptions.localDirIsUsed(zipName, true);
//	    		assumptions.localDirIsUsed(outDir.getAbsolutePath(), false);
		    	// do extract
				ZipFile zipFile = new ZipFile(zipfileF);
				entries = zipFile.entries();

				while(entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();

					if(entry.isDirectory()) {
						File folder = new File(outDir,entry.getName());
						folder.mkdirs();
					}
					else{
						InputStream in = zipFile.getInputStream(entry);
						OutputStream out = makedirs(outDir,entry.getName());
						copyInputStream(in,out);
					}
			 	}
			 	zipFile.close();
			 //	writeTimestamps(outDir,prop);
		   	} catch (IOException ex) {
				Properties msgProps = new Properties();
				String message = ex.getMessage();
				if ((message==null)||(message.length()==0)){
					message = "Exception of class "+ex.getClass().getName();
				}
				msgProps.setProperty("GENERAL_MESSAGE", message);
				//log.addError("apidiff.36.Severe", msgProps, null);
				ex.printStackTrace();
			}
		}
		
		public static final void copyInputStream(InputStream in, OutputStream out)
				throws IOException
				{
					byte[] buffer = new byte[1024];
					int len;

					while((len = in.read(buffer)) >= 0){
						out.write(buffer, 0, len);
					}
					in.close();
					out.close();
				}
		
		public static OutputStream makedirs(File outDir,String name) throws FileNotFoundException{
			String tmp1 = name.replace(File.separatorChar,'/');
			String[] folders = tmp1.split("/");
			File outFile = outDir;
			for (int i = 0; i<folders.length-1;i++){
				outFile = new File(outFile,folders[i]);
			}
			outFile.mkdirs();
			outFile= new File(outFile,folders[folders.length-1]);
			OutputStream tmp = new FileOutputStream(outFile);
			OutputStream out = new BufferedOutputStream(tmp);
			return out;
		}

		/*private static Properties readTimestamps(File file) throws IOException{
			Properties result = new Properties();
			if (file.exists()){
				FileInputStream input = new FileInputStream(file);
				long timeStamp = file.lastModified();
				String timeString=Long.toString(timeStamp);
				try{
					result.load(input);
					result.put(TIMESTAMP_FILENAME,timeString);// only keep timestamp if loading was successfull.
				}
				catch(IllegalArgumentException ex){
					boolean repair = file.delete();
					String message = "Error: unable to load file "+file.getPath()+" because of "+ex.getMessage();
					if (repair){
						message = message+"; deleted.";
					}
					else{
						message = message+"; unable to delete.";
					}
					Properties prop = new Properties();
					prop.put("GENERAL_MESSAGE",message);
					//log.addError("apidiff.36.Severe", prop, null);
					System.out.println(message);
				}
				finally{
					input.close();
				}
			}
			else{
				System.out.println("File "+TIMESTAMP_FILENAME+" not found. Must only happen once per JLin run.");
				// TODO: Add assertion-check.
			}
			return result;
		}*/
		
		
	/*	private static void writeTimestamps(File outDir, Properties prop) throws IOException{
			// update the cache
			File file = new File(outDir,TIMESTAMP_FILENAME);
			File tmpFile = File.createTempFile("JLinTimestamps",".properties", outDir); 
			OutputStream out=new FileOutputStream(tmpFile);
			prop.store(out,"This file is generated by JLin tests GlobalAnalysis. Do only delete it together with the complete folder.");
			out.close(); // now the outdated timestamp from loading the file is in the file.
			String tmp = prop.getProperty(TIMESTAMP_FILENAME);
			long ram = 1; // smaller than any existing file timestamp, but bigger than the timestamp of a nonexisting file.
			if (tmp!=null){
				ram = Long.parseLong(tmp);// last modified time from reading the file.
			}
			long disk = file.lastModified(); // last modified time now.
			boolean replace = false;
			if(ram>=disk){
				// file on disk is unchanged or missing-->replace
				replace=true;
				System.out.println("Timestamp from Disk is unchanged "+ram+" "+disk);
			}
			else{
				// some other process changed the file already.
				// Combine both properties and write, if the combined data is bigger than the one on disk.
				Properties fromDisk = readTimestamps(file);
				if (prop.size()>fromDisk.size()){
					prop.putAll(fromDisk);
					replace = true;
				}
			}
			if(replace){
				if (file.exists()){
					file.delete(); // If file does not exist, do nothing.
				}
				boolean success = tmpFile.renameTo(file);	// if rename fails, and file does not exist, we run into a problem.
				if (!success&&(disk>0)){
					if (log.isVerbose()){
						Properties prop2 = new Properties();
						prop2.put("GENERAL_MESSAGE","unable to rename "+tmpFile.getName());
						log.addError("apidiff.36.Severe", prop2, null);
						System.out.println("Error in renaming: "+tmpFile.getAbsolutePath()+" to "+file.getAbsolutePath());
					}
				}
			}
		}*/
		
		
}
