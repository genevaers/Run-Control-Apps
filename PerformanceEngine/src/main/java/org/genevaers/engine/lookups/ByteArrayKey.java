package org.genevaers.engine.lookups;

import java.util.Arrays;

class ByteArrayKey {
    private final byte[] data;
    private final int hashCode;

    public ByteArrayKey(byte[] data, int offset, int length) {
        this.data = Arrays.copyOfRange(data, offset, length); // Ensure immutability
        this.hashCode = Arrays.hashCode(this.data); // Pre-compute hash code
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ByteArrayKey that = (ByteArrayKey) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}