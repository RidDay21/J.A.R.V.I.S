package jarvis.model;

public class FieldInfo {
    public String name;  // Например, "itemId"
    public String type;  // Например, "Long" или "object", если это вложенный класс
    public String ref;   // Ссылка на другой класс, если type == "object" (например, "AddressDTO")

    // Конструктор для простых типов (String, Long, int)
    public FieldInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    // Конструктор для вложенных объектов
    public FieldInfo(String name, String type, String ref) {
        this.name = name;
        this.type = type;
        this.ref = ref;
    }
}
