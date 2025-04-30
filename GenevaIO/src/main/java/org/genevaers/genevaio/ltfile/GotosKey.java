package org.genevaers.genevaio.ltfile;

public class GotosKey extends DescriptionKey {

    public GotosKey(int l) {
        super("TrueGoto FalseGoto", l);
        DescriptionKeysCache.add("TrueGoto", new RowNumKey(5));
        DescriptionKeysCache.add("FalseGoto", new RowNumKey(5));
    }
}
