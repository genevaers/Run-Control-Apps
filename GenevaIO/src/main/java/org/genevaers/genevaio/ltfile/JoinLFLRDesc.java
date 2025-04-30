package org.genevaers.genevaio.ltfile;

public class JoinLFLRDesc extends DescriptionPart{

    public JoinLFLRDesc() {
        super("LF_LR");
        keys.add(DescriptionKeysCache.add(descriptionKey, new LFLRMapDesc()));
    }

}
