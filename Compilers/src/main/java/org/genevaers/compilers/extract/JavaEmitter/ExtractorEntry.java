package org.genevaers.compilers.extract.JavaEmitter;

public class ExtractorEntry {
    String entry;

    public ExtractorEntry(String v) {
        entry = v;
    }

    public String getEntryString() {
        return entry != null ? entry : "";
    }
}
