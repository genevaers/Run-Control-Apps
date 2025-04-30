package org.genevaers.genevaio.ltfile;

public class TargetColumnDesc extends DescriptionPart {

    TargetColumnDesc() {
        super("Targ_Column");
        keys.add(DescriptionKeysCache.add(descriptionKey, new LFLRMapDesc()));
    }

}
