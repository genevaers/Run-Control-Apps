package org.genevaers.genevaio.ltfile;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class DescriptionKeysCache {
	protected static Map<String, DescriptionKey> keysCache = new TreeMap<>();
	protected static Map<Integer, DescriptionKey> levelCache = new TreeMap<>();

    public static void writeKeyCacheText(Writer out) throws IOException {
		Iterator<Entry<String, DescriptionKey>> di = keysCache.entrySet().iterator();
		while (di.hasNext()) {
			Entry<String, DescriptionKey> de = di.next();
			out.write(String.format("%4s: %s\n\n", de.getKey(), de.getValue().getDescriptionKey() + length(de.getValue())));
		}

		out.write("\nKeys written\n");

    }

    private static String length(DescriptionKey key) {
		return key.getLength() > 0 ? " " + Integer.toString(key.getLength()) : "";
	}

	public static DescriptionKey add(String name, DescriptionKey k) {
		k.setName(name);
		DescriptionKey existing = keysCache.putIfAbsent(name, k);
		return existing != null ? existing : k;
    }

    public static DescriptionKey get(String name) {
        return keysCache.get(name);
    }

}
