package org.genevaers.genevaio.yamlreader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.GersEnvironment;

public class YAMLEnvironmentReader {
	private static Map<Integer, String> environs = new TreeMap<>();
	private static Map<String, String> environsByName = new TreeMap<>();

 	public String getEnvironment(Integer id) {
		return environs.get(id);
	}

	public String getEnvironment(String name)  {
		return environsByName.get(name);
	}
	
	public List<String> queryAllEnvironments()  {
		List<String> result = new ArrayList<String>();
		environs.clear();
		File[] envs = GersConfigration.getGersHome().toFile().listFiles();
		if(envs.length > 0) {
			Stream.of(envs)
		    	      .filter(file -> file.isDirectory() && !file.getName().startsWith("."))
		    	      .forEach(env -> addToResults(result, env));
		} else {
			Path defaultPath = GersConfigration.getGersHome().resolve("DefaultYAML_Env");
			defaultPath.toFile().mkdirs();
			String environmentName = "DefaultYAML_Env";
			environs.put(1, environmentName);
			environsByName.put(environmentName, environmentName);
			result.add(environmentName);
		}
		return result;
	}
	
	private Object addToResults(List<String> result, File env) {
		String environmentName = null;
		try {
		    BasicFileAttributes attr = Files.readAttributes(env.toPath(), BasicFileAttributes.class);
		    FileTime fileTime = attr.creationTime();
		    Date crDate = new Date(fileTime.toMillis());
		    int id = environs.size()+1; 
			environmentName = env.getName();
			environs.put(id, environmentName);
			environsByName.put(environmentName, environmentName);
			result.add(environmentName);
		} catch (IOException ex) {
		    // handle exception
		}
		return environmentName;
	}

    public void makeEnvironment(String name) {
        GersConfigration.getGersHome().resolve(name).toFile().mkdirs();
    }
}
