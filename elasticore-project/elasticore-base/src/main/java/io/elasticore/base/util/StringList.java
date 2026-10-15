package io.elasticore.base.util;


/**
 *
 */
public class StringList {

    private StringBuilder stringBuilder;



    private String prefix;

    private String delimeter = ",";

    private String postfix;

    private boolean isStart= false;

    private StringList(String delimeter, String postfix) {
        this.stringBuilder = new StringBuilder();
        this.delimeter = delimeter;
        this.postfix = postfix;
    }

    public static StringList create() {
        return new StringList(",",null);
    }

    public static StringList create(String delimeter) {
        return new StringList(delimeter, null);
    }
    public static StringList create(String delimeter, String postfix) {
        return new StringList(delimeter, postfix);
    }

    public StringList append(String prefix) {
        this.stringBuilder.append(prefix);
        return this;
    }

    public StringList add(Object obj) {
        if(!isStart) isStart=true;
        else {
            this.stringBuilder.append(delimeter);
        }
        if(obj==null)
            this.stringBuilder.append("null");
        else
            this.stringBuilder.append(obj.toString());

        return this;
    }

    @Override
    public String toString() {
        if(this.postfix == null)
            return this.stringBuilder.toString();
        else
            return this.stringBuilder.toString()+postfix;
    }
}
