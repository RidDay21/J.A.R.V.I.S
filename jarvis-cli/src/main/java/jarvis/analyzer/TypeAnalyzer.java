package jarvis.analyzer;

import jarvis.model.FieldInfo;
import jarvis.model.Schema;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

public class TypeAnalyzer {

    // Ссылка на общую мапу схем.
    // Она нужна, чтобы не парсить один и тот же класс дважды (избежать бесконечной рекурсии, если класс ссылается сам на себя)
    private final Map<String, Schema> schemaRegistry;

    public TypeAnalyzer(Map<String, Schema> schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
    }

    public Schema analyze(Class<?> clazz) {
        if (isSimpleType(clazz)) {
            return new Schema(getSchemaTypeName(clazz));
        }

        String className = clazz.getSimpleName();

        // Если мы уже разобрали этот класс ранее, просто возвращаем готовую схему
        if (schemaRegistry.containsKey(className)) {
            return schemaRegistry.get(className);
        }

        Schema schema = new Schema(className);
        // Сразу кладу схему в реестр до обхода полей.
        // Это спасет от стаковерфлоу, если класс A имеет поле типа A.
        schemaRegistry.put(className, schema);

        for (Field field : clazz.getDeclaredFields()) {
            // Пропускаю статические поля (нам не нужны константы или serialVersionUID в JSON)
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Class<?> fieldType = field.getType();
            String fieldName = field.getName();

            if (isSimpleType(fieldType)) {
                // Простое поле (String -> "string", BigDecimal -> "number")
                schema.addField(new FieldInfo(fieldName, getSchemaTypeName(fieldType)));
            } else {
                // Рекурсия
                // 1. Анализируем вложенный класс и добавляем его в реестр
                analyze(fieldType);
                // 2. Создал поле-ссылку в текущей схеме
                schema.addField(new FieldInfo(fieldName, "object", fieldType.getSimpleName()));
            }
        }

        return schema;
    }

    private String getSchemaTypeName(Class<?> type) {
        if (type.equals(String.class) || type.equals(java.util.UUID.class) || type.equals(Character.class) || type.equals(char.class)) {
            return "string";
        }
        if (Number.class.isAssignableFrom(type) || (type.isPrimitive() && type != boolean.class)) {
            return "number";
        }
        if (type.equals(Boolean.class) || type == boolean.class) {
            return "boolean";
        }
        if (type.getName().startsWith("java.time.") || type.equals(java.util.Date.class)) {
            return "date";
        }
        if (Collection.class.isAssignableFrom(type)) {
            return "array";
        }
        if (Map.class.isAssignableFrom(type)) {
            return "map";
        }
        return type.getSimpleName();
    }

    // Вспомогательный метод для определения, нужно ли лезть внутрь класса
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(String.class) ||
                type.equals(Integer.class) ||
                type.equals(Long.class) ||
                type.equals(Double.class) ||
                type.equals(Float.class) ||
                type.equals(Boolean.class) ||
                type.equals(Short.class) ||
                type.equals(Byte.class) ||
                type.equals(Character.class) ||
                type.equals(java.math.BigDecimal.class) ||
                type.equals(java.math.BigInteger.class) ||
                type.equals(java.util.UUID.class) ||
                type.equals(java.util.Date.class) ||
                type.getName().startsWith("java.time.") ||
                Collection.class.isAssignableFrom(type) ||
                Map.class.isAssignableFrom(type);
    }
}