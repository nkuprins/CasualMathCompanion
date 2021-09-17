/*
 * Copyright 2021 Nikita Kuprins
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nikitakuprins.mathCompanion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Calculations {

    private static final List<Character> mathSigns = Arrays.asList('+', '-', '*', 'x', '/', '÷');

    // Valid format is: 2 / 3 + 1 * 4
    // No redundant spaces or unclear symbols.
    // All numbers are only integers.
    private static final Pattern validFormat = Pattern.compile("^-?\\d+( [+\\-*/] -?\\d+)+$");

    // Check if some expression has division or multiplication,
    // is that is true, then we should split it again
    private static boolean hasStrongOperations = false;

    private Calculations() {}

    public static long countOfDigits(String str) {
        return str.chars()
                .filter(Character::isDigit)
                .count();
    }

    public static long countOfSigns(String str) {
        return str.chars()
                .mapToObj(c -> (char) c)
                .filter(mathSigns::contains)
                .count();
    }

    public static Set<Character> getExpressionSigns(String str) {
        String[] array = str.split(" ");
        return Arrays.stream(array)
                .filter(s -> s.length() == 1)
                .map(s -> s.charAt(0))
                .filter(mathSigns::contains)
                .collect(Collectors.toSet());
    }

    public static boolean isValidFormat(String str) {
        Matcher matcher = validFormat.matcher(str);
        return matcher.matches();
    }

    public static BigDecimal calculate(BigDecimal num1, BigDecimal num2, String operation) {
        switch (operation) {
            case "+":
                return num1.add(num2);
            case "-":
                return num1.subtract(num2);
            case "x": case "*":
                return num1.multiply(num2);
            case "÷": case "/":
                return num1.divide(num2, 4, RoundingMode.HALF_UP);
            default:
                return BigDecimal.ZERO;
        }
    }

    // Splits expression by math order
    // Example: 2 * 3 | - | 1 / 3 | + | 2
    // 1) 2 * 3
    // 2) 1 / 3
    // 3) -
    // 4) + 2
    private static String[] splitByOperationsOrder(String expression) {
        if (hasStrongOperations) {
            return expression.split(" ");
        }

        return expression.split("(?<=[^/*])\\s(?=[^/*])");
    }

    // If variable s is still an expression and not a number,
    // then calls method calculate() to solve it.
    private static BigDecimal convertExpression(String s) {
        if (s.chars().anyMatch(item -> item == ' ')) {
            hasStrongOperations = true;
            return calculate(s);
        }

        return new BigDecimal(s);
    }

    public static BigDecimal calculate(String expression) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal temp;
        String operation = "+";
        String[] array = splitByOperationsOrder(expression);

        for (String s : array) {
            if (s.length() == 1 && mathSigns.contains(s.charAt(0))) {
                // Variable s is an operation symbol
                operation = s;
            } else {
                // Variable s is number or expression(with / or * operations)
                temp = convertExpression(s);
                result = calculate(result, temp, operation);
            }
        }

        hasStrongOperations = false;
        return result;
    }
}
