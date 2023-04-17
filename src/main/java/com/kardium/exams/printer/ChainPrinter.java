package com.kardium.exams.printer;

/*
 * Copyright Kardium Inc. 2023.
 */

import java.util.LinkedHashSet;

/**
 * Fill out this class to implement the {@link Printer} interface.
 */
public class ChainPrinter implements Printer {
    public static final int PRINT_HAMMER_GAP_IN_MILLIMETER = 4;
    private final ChainPrinterDriver driver;

    public ChainPrinter(ChainPrinterDriver driver) {
        this.driver = driver;
    }

    @Override
    public void println(String line) {

        if(!line.matches("^[0-1]+$")) {
            throw new IllegalArgumentException("The line can only contains digits '0' or '1'");
        }

        int s1 = 0;
        int s2;
        char startDigit = line.charAt(0);

        LinkedHashSet<Integer> matched_set = new LinkedHashSet<>();
        LinkedHashSet<Integer> unMatched_set = new LinkedHashSet<>();

        do{
            s2 = s1 + PRINT_HAMMER_GAP_IN_MILLIMETER;

            if(line.charAt(s1) == startDigit) {
                matched_set.add(s1);
                driver.fire(s1);
            }else{
                unMatched_set.add(s1);
            }

            startDigit ^= 1;

            if(line.charAt(s2) == startDigit) {
                matched_set.add(s2);
                driver.fire(s2);
            }else{
                unMatched_set.add(s2);
            }

            driver.step();
            s1 += 1;

        }while(s2 < line.length() - 1);

        while(unMatched_set.size() > 0) {

            Integer unMatched[] = new Integer[unMatched_set.size()];
            unMatched = unMatched_set.toArray(unMatched);

            s1 = unMatched[0];
            s2 = s1 + PRINT_HAMMER_GAP_IN_MILLIMETER;
            unMatched_set.remove(s1);

            if (line.charAt(s1) == startDigit) {
                matched_set.add(s1);
                driver.fire(s1);
            } else {
                unMatched_set.add(s1);
            }

            startDigit ^= 1;

            s2 = s2 >= line.length() ? s2 - line.length() : s2;

            if (!(matched_set.contains(s2))) {
                if (line.charAt(s2) == startDigit) {
                    matched_set.add(s2);
                    driver.fire(s2);
                } else {
                    unMatched_set.add(s2);
                }
            }

            driver.step();

        }

        if(matched_set.size() == line.length()) {
            driver.linefeed();
        }
    }

    @Override
    public void dprintln(String line) {
       String d_line = line.replaceAll("[2-9]", "0");
       println(d_line);
    }

    @Override
    public void pprintln(String line) {
        // REPLACE THIS WITH YOUR CODE
//        println(line);
    }
}
