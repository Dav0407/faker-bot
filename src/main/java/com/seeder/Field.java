package com.seeder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.function.BiFunction;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode(of = {"fieldName", "fieldType"})
public class Field {
    private final String fieldName;
    private final FieldType fieldType;
    private final BiFunction<Integer, Integer, Object> func;
    private int min;
    private int max;

    public Field(String fieldName, FieldType fieldType, int min, int max) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.func = FakerApplicationService.functions.get(fieldType);
        this.min = min;
        this.max = max;
    }

    public String getPatternAsJson() {
        return fieldType.getRowAsJson(fieldName, func.apply(min, max));
    }

    @Override
    public String toString() {
        return "\033[1;92m%s : %s \033[0m\n".formatted(fieldName, fieldType.name());
    }
}