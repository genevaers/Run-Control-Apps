package org.genevaers.genevaio.ltfile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DescriptionKey {
    protected String name;
    protected String descriptionKey;
    private List<DescriptionKey> children = new ArrayList<>();
    private int length;
    private boolean referenced;
    private boolean printed = false;
    private String separator = " ";

    DescriptionKey(String d, int l) {
        descriptionKey = d;
        length = l;
    }

    public String getDescriptionKey() {
        if(printed) {
            return "";
        } else {
            printed = true;
            return descriptionKey + " " + (children.size() == 0 ? length : "");
        }
    }

    // public void add(String name, DescriptionKey k) {
    //     this.name = name;
    //     DescriptionKey newKey = DescriptionKeysCache.add(name, k);
    //     if(newKey == null) {
    //         children.add(k);
    //     }
    // }

    public void addChild(DescriptionKey c) {
        children.add(c);
    }

    public int getLength() {
        int calclength = 0;
        if(children.size() > 0) {
            for (DescriptionKey descriptionKey : children) {
                calclength += descriptionKey.getLength();
            }
            if(calclength > length) {
                //error warn
            }
        }
        return length == 0 ? calclength : length; 
    }

    public void setLength(int length) {
        this.length = length;
    }


    public Iterator<DescriptionKey> getIterator() {
        return children.iterator();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setReferenced(boolean referenced) {
        this.referenced = referenced;
    }

    public boolean isReferenced() {
        return referenced;
    }

    public String getFormatString() {
        boolean addSeparator = false;
        StringBuilder sb = new StringBuilder();
        if(children.size() == 0) {
            sb.append("%");
            sb.append(length);
            sb.append(descriptionKey.startsWith("Int") ? "d": "s");
        } else {
            Iterator<DescriptionKey> ci = children.iterator();
            while (ci.hasNext()) {
                if(addSeparator) {
                    sb.append(separator);
                }
                DescriptionKey c = ci.next();
                sb.append(c.getFormatString());
                addSeparator = true;
            }
        }
        return sb.toString(); 
    }

    public String getLogString(LTRecord ltr) {
        //how can we get the correct ltr fields?
        return "";
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
