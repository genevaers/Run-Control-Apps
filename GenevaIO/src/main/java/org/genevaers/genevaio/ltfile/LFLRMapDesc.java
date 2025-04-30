package org.genevaers.genevaio.ltfile;

public class LFLRMapDesc extends DescriptionKey{

    public LFLRMapDesc() {
        super("LF_ID/LR_ID", 0);
        DescriptionKeysCache.add("LF_ID", new RowNumKey(5));
        DescriptionKeysCache.add("LR_ID", new RowNumKey(5));
    }

}
