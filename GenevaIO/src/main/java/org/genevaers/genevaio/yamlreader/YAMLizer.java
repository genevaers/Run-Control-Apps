package org.genevaers.genevaio.yamlreader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.genevaers.utilities.GersConfigration;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.flogger.FluentLogger;

public class YAMLizer {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());;
	private static String environmentName;
	
    protected static Set<Integer> requiredLFs = new TreeSet<>();
    protected static Set<Integer> requiredPFs = new TreeSet<>();
    protected static Set<Integer> requiredLRs = new TreeSet<>();
    protected static Set<Integer> requiredExits = new TreeSet<>();

    public static LogicalRecordYAMLBean readYamlLR(Path input) {
    	LogicalRecordYAMLBean txfr = null;
        yamlMapper.findAndRegisterModules();
        yamlMapper.setSerializationInclusion(Include.NON_NULL);
        logger.atInfo().log("Read %s", input);
        // cache.contains ? cache.get : read from disk
        try {
        		return yamlMapper.readValue(input.toFile(), LogicalRecordYAMLBean.class);
        } catch (IOException e) {
            logger.atSevere().log("read yaml failed for type %s", e.getMessage());
        };
        return txfr;
    }

	public static YAMLLookupTransfer readYamlLookup(Path input) {
        yamlMapper.findAndRegisterModules();
        yamlMapper.setSerializationInclusion(Include.NON_NULL);
        logger.atInfo().log("Read %s", input);
        // cache.contains ? cache.get : read from disk
        try {
        	return yamlMapper.readValue(input.toFile(), YAMLLookupTransfer.class);
        } catch (IOException e) {
            logger.atSevere().log("read yaml failed for type %s ", e.getMessage());
        };
        return null;
    }
    

    
    // public static void writeYaml(Path output, SAFRTransfer txfr) {
    //     try {
    //         yamlMapper.writeValue(output.toFile(), txfr);
    //         logger.atInfo().log("Write yaml %s ", output);
    //     } catch (IOException e) {
    //         logger.atSevere().log("write yaml failed\n%s", e.getMessage());
    //     }
    // }

	public static Path getLookupsPath() {
		return GersConfigration.getGersHome().resolve(environmentName).resolve("lks");
	}

	public static Path getLRsPath() {
		return GersConfigration.getGersHome().resolve(environmentName).resolve("lrs");
	}

	public static Path getLFsPath() {
		return GersConfigration.getGersHome().resolve(environmentName).resolve("lfs");
	}

	public static Path getPFsPath() {
		return GersConfigration.getGersHome().resolve(environmentName).resolve("pfs");
	}

	public static Path getExitsPath() {
		return GersConfigration.getGersHome().resolve(environmentName).resolve("exits");
	}

	public static Path getCRsPath() {
		return GersConfigration.getGersHome().resolve(environmentName).resolve("crs");
	}

	public static Path getViewsPath() {
		return GersConfigration.getGersHome().resolve(environmentName).resolve("views");
	}

	public static void setEnvironmentName(String environmentName) {
		YAMLizer.environmentName = environmentName;
	}

	public static String getEnvironmentName() {
		return environmentName;
	}
    
    public static Set<Integer> getRequiredLRs() {
        return requiredLRs;
    }

    public static YAMLLogicalFileTransfer readLf(File lf) {
        yamlMapper.findAndRegisterModules();
        yamlMapper.setSerializationInclusion(Include.NON_NULL);
        logger.atInfo().log("Read %s", lf);
        // cache.contains ? cache.get : read from disk
        try {
        	return yamlMapper.readValue(lf, YAMLLogicalFileTransfer.class);
        } catch (IOException e) {
            logger.atSevere().log("read lf yaml failed %s ", e.getMessage());
        };
        return null;
    }

    public static PhysicalFileTransfer readYamlPf(Path pf) {
        yamlMapper.findAndRegisterModules();
        yamlMapper.setSerializationInclusion(Include.NON_NULL);
        logger.atInfo().log("Read %s", pf);
        // cache.contains ? cache.get : read from disk
        try {
        	return yamlMapper.readValue(pf.toFile(), PhysicalFileTransfer.class);
        } catch (IOException e) {
            logger.atSevere().log("read pf yaml failed %s ", e.getMessage());
        };
        return null;
    }
}
