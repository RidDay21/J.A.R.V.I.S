package jarvis.analyzer;

import jarvis.model.FieldInfo;
import jarvis.model.Schema;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

public class TypeAnalyzer {

    // Ссылка на общую мапу схем.
    // Она нужна, чтобы не парсить один и тот же класс дважды (и избежать бесконечной рекурсии, если класс ссылается сам на себя)
    private final Map<String, Schema> schemaRegistry;

    public TypeAnalyzer(Map<String, Schema> schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
    }

    public Schema analyze(Class<?> clazz) {
        String className = clazz.getSimpleName();

        // Если мы уже разобрали этот класс ранее, просто возвращаем готовую схему
        if (schemaRegistry.containsKey(className)) {
            return schemaRegistry.get(className);
        }

        Schema schema = new Schema(className);
        // СРАЗУ кладем схему в реестр ДО обхода полей.
        // Это спасет от StackOverflowError, если класс A имеет поле типа A.
        schemaRegistry.put(className, schema);

        for (Field field : clazz.getDeclaredFields()) {
            // Пропускаем статические поля (нам не нужны константы или serialVersionUID в JSON)
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Class<?> fieldType = field.getType();
            String fieldName = field.getName();

            if (isSimpleType(fieldType)) {
                // Простое поле (String, int, Long)
                schema.addField(new FieldInfo(fieldName, fieldType.getSimpleName()));
            } else {
                // СЛОЖНЫЙ ОБЪЕКТ - РЕКУРСИЯ!
                // 1. Анализируем вложенный класс и добавляем его в реестр
                analyze(fieldType);
                // 2. Создаем поле-ссылку в текущей схеме
                schema.addField(new FieldInfo(fieldName, "object", fieldType.getSimpleName()));
            }
        }

        return schema;
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
                // Упрощение: пока считаем коллекции "простыми" типами, чтобы не усложнять парсинг дженериков
                Collection.class.isAssignableFrom(type);
    }
}