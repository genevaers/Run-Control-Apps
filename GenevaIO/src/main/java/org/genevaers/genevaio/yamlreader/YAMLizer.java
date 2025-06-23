package org.genevaers.genevaio.yamlreader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

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

    public static LogicalRecordYAMLBean readYaml(Path input, String ctype) {
    	LogicalRecordYAMLBean txfr = null;
        yamlMapper.findAndRegisterModules();
        yamlMapper.setSerializationInclusion(Include.NON_NULL);
        logger.atInfo().log("Read %s %s", ctype, input);
        // cache.contains ? cache.get : read from disk
        try {
        	switch(ctype) {
        	// case ControlRecord:
        	// 	return  yamlMapper.readValue(input.toFile(), ControlRecordTransfer.class);
        	// case UserExitRoutine:
        	// 	return  yamlMapper.readValue(input.toFile(), UserExitRoutineTransfer.class);
        	// case PhysicalFile:
        	// 	return yamlMapper.readValue(input.toFile(), PhysicalFileTransfer.class);
        	// case LogicalFile:
        	// 	return yamlMapper.readValue(input.toFile(), YAMLLogicalFileTransfer.class);
        	case "lr":
        		return yamlMapper.readValue(input.toFile(), LogicalRecordYAMLBean.class);
        	// case LookupPath:
        	// 	return yamlMapper.readValue(input.toFile(), YAMLLookupTransfer.class);
        	// case View:
        	// 	return yamlMapper.readValue(input.toFile(), YAMLViewTransfer.class);
        	}
        } catch (IOException e) {
            logger.atSevere().log("read yaml failed for type %s %s", ctype, e.getMessage());
        };
        return txfr;
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
}
