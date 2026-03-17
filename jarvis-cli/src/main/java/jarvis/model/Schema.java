package jarvis.model;

import java.util.ArrayList;
import java.util.List;

public class Schema {
    public String type; // Название класса, например "OrderRequest"
    public List<FieldInfo> fields = new ArrayList<>();

    public Schema(String type) {
        this.type = type;
    }

    public void addField(FieldInfo field) {
        this.fields.add(field);
    }
}
