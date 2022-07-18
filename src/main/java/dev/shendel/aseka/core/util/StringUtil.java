package dev.shendel.aseka.core.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@UtilityClass
public final class StringUtil {

    public static List<String> parseArray(String operand) {
        return Splitter.on(",")
                       .trimResults(
                               CharMatcher.is('[')
                                          .or(CharMatcher.is(']'))
                                          .or(CharMatcher.is('"'))
                                          .or(CharMatcher.whitespace())
                       )
                       .splitToList(operand);
    }

    public static String trimQuotes(String operand) {
        if (operand == null) {
            return null;
        }
        return CharMatcher.is('"')
                          .or(CharMatcher.is('\''))
                          .or(CharMatcher.whitespace())
                          .trimFrom(operand);
    }

    public static String format(String message, Object... args) {
        return MessageFormatter.arrayFormat(message, args).getMessage();
    }

    public static String getCurrentDate() {
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        return format.format(currentDate);
    }

}
