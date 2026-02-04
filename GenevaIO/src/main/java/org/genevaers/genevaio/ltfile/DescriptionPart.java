package org.genevaers.genevaio.ltfile;

import java.util.ArrayList;
import java.util.List;

public class DescriptionPart {
    protected String descriptionKey;
    protected List<DescriptionKey> keys = new ArrayList<>();

    DescriptionPart(String d) {
        descriptionKey = d;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

}
