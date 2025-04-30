package org.genevaers.genevaio.ltfile;

public class JoinMappingDesc extends DescriptionPart{

    public JoinMappingDesc() {
        super("Join_Mapping");
        keys.add(DescriptionKeysCache.add(descriptionKey, new JMappingKey(0)));
     }

}
