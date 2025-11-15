package com.ehhthan.mmobuffs.api.stat;

public final class StatValue {

    private final double value;
    private final ValueType type;

    public StatValue(String input) {
        if (input.endsWith("%")) {
            this.type = ValueType.RELATIVE;
            input = input.substring(0, input.length() - 1);
        } else {
            this.type = ValueType.FLAT;
        }
        this.value = Double.parseDouble(input);
    }

    public StatValue(double value) {
        this(value, ValueType.FLAT);
    }

    public StatValue(double value, ValueType type) {
        this.value = value;
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public ValueType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type == ValueType.FLAT ? String.valueOf(value) : value + "%";
    }

    public enum ValueType {
        FLAT,
        RELATIVE
    }
}
