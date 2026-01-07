package org.genevaers.extractgenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.genevaers.extractgenerator.codegenerators.ExtractorEntry;
import org.genevaers.extractgenerator.codegenerators.LT2JavaRecords;
import org.genevaers.genevaio.fieldnodes.MetadataNode;
import org.genevaers.genevaio.fieldnodes.Records2Dot;
import org.genevaers.genevaio.html.LTRecordsHTMLWriter;
import org.genevaers.genevaio.ltfile.LTFileReader;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTable;
import org.genevaers.genevaio.report.LogicTableTextWriter;
import org.genevaers.genevaio.vdpfile.VDPFileReader;
import org.genevaers.genevaio.vdpfile.VDPManagementRecords;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.GersFile;

import com.google.common.flogger.FluentLogger;

public class LT2Extractor {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private VDPManagementRecords vmrs;
    private List<ExtractorEntry> exrecs = new ArrayList<>();

    public void execute() {
        logger.atInfo().log("Read a logic table and generate Extractor code.");
        GersConfigration.initialise();
        Path root = Paths.get(GersConfigration.getCurrentWorkingDirectory());
        readVDP(root, GersConfigration.VDP_DDNAME, null);
        Path xlt = root.resolve(GersConfigration.XLT_DDNAME);
        generateExtract(root, xlt);
    }

    private void generateExtract(Path root, Path rc1) {
        MetadataNode recordsRoot = new MetadataNode();
        recordsRoot.setSource1(root.relativize(rc1.resolve(GersConfigration.XLT_DDNAME)).toString());
        readLT(root, recordsRoot, GersConfigration.XLT_DDNAME).getStream().forEach(lte -> LT2JavaRecords.processRecord(lte));
        ExtractorWriter.addJoinInitialisation(LT2JavaRecords.getJoins());
        logger.atInfo().log("XLT read from %s", rc1.toString());
        ExtractorWriter.write(LT2JavaRecords.getExrecs(), LT2JavaRecords.getInputDDnames(), LT2JavaRecords.getOutputLength(), LT2JavaRecords.getLrLength());
    }

    private void generateExtractFromLT(LTRecord lte) {
            LT2JavaRecords er = new LT2JavaRecords();
            ExtractorEntry exr = er.processRecord(lte);
            if(exr != null) {
                exrecs.add(exr);
            }
    }

    public void readVDP(Path vdpPath, String ddName, MetadataNode recordsRoot) {
        logger.atInfo().log("Read %s, from %s", ddName, vdpPath.toString());
        if (new GersFile().exists(vdpPath.toString())) {
            VDPFileReader vdpr = new VDPFileReader();
            vdpr.setRecordsRoot(recordsRoot);
            Path vdp = vdpPath.resolve(GersConfigration.VDP_DDNAME);
            vdpr.open(vdp, ddName);
            vdpr.addToRepsitory();
            vmrs = vdpr.getViewManagementRecords();
            try {
                logger.atInfo().log("Close %s", ddName);
                vdpr.close();
            } catch (IOException e) {
                logger.atSevere().log("VDP Close error %e", e.getMessage());
            }
        } else {
            logger.atSevere().log("VDP %s not found", ddName);
        }
    }

    public LogicTable readLT(Path ltPath, MetadataNode recordsRoot, String ddname) {
        logger.atInfo().log("Read LT %s", ltPath);
        LTFileReader ltr = new LTFileReader();
        ltr.setRecordsRoot(recordsRoot);
        ltr.open(ltPath, ddname);
        return ltr.makeLT();
    }

}
