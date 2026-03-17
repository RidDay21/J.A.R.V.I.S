package jarvis.model;

public class Parameter {
    public String name;
    public String in; // "query", "path", "header"
    public String type;
    public boolean required;

    public Parameter(String name, String in, String type, boolean required) {
        this.name = name;
        this.in = in;
        this.type = type;
        this.required = required;
    }
}
