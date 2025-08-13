package org.genevaers.engine.lookups;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JoinsRepo {
    private static Map<String, Join> joins = new HashMap<>();

    public static void addJoin(Join jn) {
        joins.computeIfAbsent(jn.getLflrid(), j -> addNewJoin(jn));
    }

    private static Join addNewJoin(Join j) {
        return j;
    }

    public static Join getJoin(String id) {
        return joins.get(id);
    }

    public static Collection<Join> getJoins() {
        return joins.values();
    }
}
