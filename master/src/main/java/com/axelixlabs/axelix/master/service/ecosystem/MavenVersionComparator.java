/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.ecosystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Orders the versions of a library the way Maven does, so that e.g. {@code 1.10} comes after {@code 1.9},
 * {@code 2.0-RC1} comes before {@code 2.0}, and {@code 6.5.3.Final} equals {@code 6.5.3}.
 * <p>
 * A version is split into numeric and textual items at every {@code .}, {@code -}, {@code _} and every switch
 * between digits and letters. Numbers compare numerically. Known qualifiers rank as
 * {@code alpha < beta < milestone < rc < snapshot < release < sp}, where {@code ga}, {@code final} and
 * {@code release} all mean the release itself. Unknown qualifiers (e.g. {@code jre}, {@code android}) rank after
 * the known ones and compare alphabetically among themselves. A missing item counts as the release.
 *
 * @author Mikhail Polivakha
 */
public final class MavenVersionComparator implements Comparator<String> {

    public static final MavenVersionComparator INSTANCE = new MavenVersionComparator();

    private static final int RELEASE_RANK = 5;

    private static final Map<String, Integer> QUALIFIER_RANKS = Map.ofEntries(
            Map.entry("alpha", 0),
            Map.entry("a", 0),
            Map.entry("beta", 1),
            Map.entry("b", 1),
            Map.entry("milestone", 2),
            Map.entry("m", 2),
            Map.entry("rc", 3),
            Map.entry("cr", 3),
            Map.entry("snapshot", 4),
            Map.entry("", RELEASE_RANK),
            Map.entry("ga", RELEASE_RANK),
            Map.entry("final", RELEASE_RANK),
            Map.entry("release", RELEASE_RANK),
            Map.entry("sp", 6));

    private static final int UNKNOWN_QUALIFIER_RANK = 7;

    private MavenVersionComparator() {}

    @Override
    public int compare(String left, String right) {
        List<String> leftItems = itemsOf(left);
        List<String> rightItems = itemsOf(right);

        for (int i = 0; i < Math.max(leftItems.size(), rightItems.size()); i++) {
            String leftItem = i < leftItems.size() ? leftItems.get(i) : "";
            String rightItem = i < rightItems.size() ? rightItems.get(i) : "";

            int result = compareItems(leftItem, rightItem);
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    private static int compareItems(String left, String right) {
        boolean leftNumeric = isNumeric(left);
        boolean rightNumeric = isNumeric(right);

        if (leftNumeric && rightNumeric) {
            return compareNumbers(left, right);
        }
        // a missing numeric item is a zero, e.g. 1.0 equals 1.0.0
        if (leftNumeric) {
            return right.isEmpty() ? compareNumbers(left, "0") : 1;
        }
        if (rightNumeric) {
            return left.isEmpty() ? compareNumbers("0", right) : -1;
        }

        int rankDifference = Integer.compare(rankOf(left), rankOf(right));
        if (rankDifference != 0) {
            return rankDifference;
        }
        return rankOf(left) == UNKNOWN_QUALIFIER_RANK ? left.compareTo(right) : 0;
    }

    private static int compareNumbers(String left, String right) {
        String leftDigits = stripLeadingZeros(left);
        String rightDigits = stripLeadingZeros(right);
        // compare by length first, so that numbers too large for a long still compare correctly
        int lengthDifference = Integer.compare(leftDigits.length(), rightDigits.length());
        return lengthDifference != 0 ? lengthDifference : leftDigits.compareTo(rightDigits);
    }

    private static String stripLeadingZeros(String number) {
        int start = 0;
        while (start < number.length() - 1 && number.charAt(start) == '0') {
            start++;
        }
        return number.substring(start);
    }

    private static int rankOf(String qualifier) {
        return QUALIFIER_RANKS.getOrDefault(qualifier, UNKNOWN_QUALIFIER_RANK);
    }

    private static boolean isNumeric(String item) {
        return !item.isEmpty() && item.chars().allMatch(Character::isDigit);
    }

    private static List<String> itemsOf(String version) {
        List<String> items = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (char character : version.toLowerCase(Locale.ROOT).toCharArray()) {
            if (character == '.' || character == '-' || character == '_') {
                flush(current, items);
                continue;
            }
            if (!current.isEmpty() && Character.isDigit(character) != Character.isDigit(current.charAt(0))) {
                flush(current, items);
            }
            current.append(character);
        }
        flush(current, items);

        // trailing zeros and release qualifiers carry no ordering information: 1.0.0.Final equals 1
        while (!items.isEmpty() && isNullItem(items.getLast())) {
            items.removeLast();
        }
        return items;
    }

    private static boolean isNullItem(String item) {
        return isNumeric(item) ? stripLeadingZeros(item).equals("0") : rankOf(item) == RELEASE_RANK;
    }

    private static void flush(StringBuilder current, List<String> items) {
        if (!current.isEmpty()) {
            items.add(current.toString());
            current.setLength(0);
        }
    }
}
