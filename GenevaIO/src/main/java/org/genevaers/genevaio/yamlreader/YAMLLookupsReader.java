package org.genevaers.genevaio.yamlreader;

import java.nio.file.Path;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;

public class YAMLLookupsReader extends YAMLReaderBase{

	private LookupPath lookup;

    public void addNamedLookupToRepo(String environmentName, String name) {
		YAMLizer.setEnvironmentName(environmentName);
		Path lksPath = YAMLizer.getLookupsPath();
		Path lkPath = lksPath.resolve(name +".yaml");
		YAMLLookupTransfer LkTxf = YAMLizer.readYamlLookup(lkPath);
		lookup = new LookupPath();
		lookup.setID(LkTxf.getId());
		lookup.setStatus(1);
		lookup.setName(LkTxf.getName());
		lookup.setTargetLFid(LkTxf.getTargetLF());
		lookup.setTargetLRid(LkTxf.getTargetLR());
		requiredLRs.add(LkTxf.getTargetLR());
		requiredLFs.add(LkTxf.getTargetLF());
		Repository.getLookups().add(lookup, LkTxf.getId(), LkTxf.getName());
		LkTxf.getSteps().stream().forEach(st -> addStepToLookup(st, lookup));
    }

	private void addStepToLookup(YAMLLookupPathStepTransfer st, LookupPath lookup) {
		LookupPathStep lkps = new LookupPathStep();
		lkps.setRequired(true);
		lkps.setSourceLRid(st.getSourceLRId());
		lkps.setStepNum(st.getSequenceNumber());
		lkps.setTargetLFid(st.getTargetLF());
		lkps.setTargetLRid(st.getTargetLR());
		requiredLFs.add(st.getTargetLF());
		requiredLRs.add(st.getTargetLR());
		st.getSources().stream().forEach(s -> addKeySourceToStep(s, lkps));
		lookup.addStep(lkps);
	}

	private void addKeySourceToStep(YAMLLookupPathSourceFieldTransfer s, LookupPathStep lkps) {
		LookupPathKey lkpk = new LookupPathKey();
		lkpk.setDatatype(DataType.fromdbcode(s.getDataType()));
		lkpk.setDateTimeFormat(s.getDateCode() != null ? DateCode.fromdbcode(s.getDateCode()) : DateCode.NONE);
		lkpk.setDecimalCount(s.getDecimalPlaces().shortValue());
		lkpk.setFieldId(s.getSourceXLRFLDId());
		lkpk.setFieldLength(s.getLength().shortValue());
		lkpk.setKeyNumber(s.getKeySeqNbr().shortValue());
		lkpk.setRounding(s.getScalingFactor().shortValue());
		lkpk.setSigned(s.getSigned());
		lkpk.setJustification(JustifyId.NONE);
		lkpk.setComponentId(lookup.getID());
		//lkpk.setStartPosition(s.get); missing?
		lkps.addKey(lkpk);
	}

}
