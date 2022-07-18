package dev.shendel.aseka.core.cucumber.parser;

import dev.shendel.aseka.core.Constants;
import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.util.StringUtil;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum Type {
    STRING(Constants.STRING_REGEX),
    BIG_DECIMAL(Constants.NOT_USED),
    TIMESTAMP(Constants.NOT_USED),
    BOOLEAN(Constants.NOT_USED),
    INT(Constants.INT_REGEX),
    FLOAT(Constants.FLOAT_REGEX),
    DOUBLE(Constants.NOT_USED),
    STRING_ARRAY(Constants.STRING_ARRAY_REGEX),
    INT_ARRAY(Constants.INT_ARRAY_REGEX),
    FLOAT_ARRAY(Constants.FLOAT_ARRAY_REGEX),
    UNKNOWN(Constants.NOT_USED);

    private final String operandPattern;

    public Object parseObject(String string) {
        return parseObject(this, string);
    }

    public static Type getByOperand(String operand) {
        for (Type type : values()) {
            if (operand == null) {
                return UNKNOWN;
            } else if (operand.matches(type.operandPattern)) {
                return type;
            }
        }
        throw new AsekaException("Can't find type by operand: {}", operand);
    }

    public static Object parseObject(Type type, String object) {
        if (object == null) {
            return null;
        }
        switch (type) {
            case STRING:
                return object;
            case BIG_DECIMAL:
                return new BigDecimal(object);
            case TIMESTAMP:
                return Timestamp.valueOf(object);
            case BOOLEAN:
                return Boolean.valueOf(object);
            case INT:
                return Integer.parseInt(object);
            case FLOAT:
                return Float.parseFloat(object);
            case DOUBLE:
                return Double.parseDouble(object);
            case STRING_ARRAY:
                return StringUtil.parseArray(object);
            case INT_ARRAY:
                return StringUtil.parseArray(object)
                        .stream()
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
            case FLOAT_ARRAY:
                return StringUtil.parseArray(object).stream()
                        .map(Float::parseFloat)
                        .collect(Collectors.toList());
            case UNKNOWN:
                return null;
            default:
                throw new AsekaException("Type '{}' isn't supported for parsing", type);
        }
    }
}
