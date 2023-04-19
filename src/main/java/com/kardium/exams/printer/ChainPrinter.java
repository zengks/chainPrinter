package com.kardium.exams.printer;

/*
 * Copyright Kardium Inc. 2023.
 */

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fill out this class to implement the {@link Printer} interface.
 */
public class ChainPrinter implements Printer {
    private static final int PRINT_HAMMER_GAP_IN_MILLIMETER = 4;
    private static final int TOTAL_NUM_OF_SOLENOIDS = 8;
    private static final int SUCCESSIVE_ONES_GAP_IN_MILLIMETER = 2;
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
       StringBuilder d_line = new StringBuilder();
       for(int i=0; i < 8 && i<line.length(); i++) {
           char c = line.charAt(i);
           if(c == '0' || c == '1' || c == ' ') {
               d_line.append(c);
           }else{
               d_line.append('0');
           }
       }
       println(d_line.toString());
    }

    @Override
    public String pprintln(String line) {

        int[] result = new int[line.length()];

        ArrayList<HashMap> solenoids = new ArrayList<>();
        ArrayList<HashMap> targetSols = new ArrayList<>();

        // Create 8 solenoids objects to represent each solenoid
        // Create 8 value objects to determine the final printing order
        for(int i=0; i<TOTAL_NUM_OF_SOLENOIDS; i++) {
            HashMap<String, Integer> solenoid = new HashMap<>();
            HashMap<String, Integer> targetSol = new HashMap<>();
            solenoid.put("sol_index", i);
            solenoid.put("positionCounter", 1);
            solenoid.put("value", 0);
            solenoid.put("self_count", 0);
            solenoids.add(solenoid);

            targetSol.put("targetSolenoid", i);
            targetSol.put("targetPositionCounter", 1);
            targetSol.put("targetValue", Integer.parseInt(String.valueOf(line.charAt(i))));
            targetSols.add(targetSol);
        }

        // Initialize 8 solenoid with their initial values
        // Assume the first solenoid is aligned dead-center with the hammer with '0' state
        for(HashMap sol: solenoids) {
            int index = (int) sol.get("sol_index");
            if(index == 0) {
                sol.put("positionCounter", 1);
                sol.put("value", 0);
                sol.put("self_count", 2);
            }
            if(index == 1) {
                sol.put("positionCounter", 2);
                sol.put("value", 1);
                sol.put("self_count", 1);
            }
            if(index == 2) {
                sol.put("positionCounter", -1);
                sol.put("value", 1);
                sol.put("self_count", 4);
            }
            if(index == 3) {
                sol.put("positionCounter", 0);
                sol.put("value", 0);
                sol.put("self_count", 3);
            }
            if(index == 4) {
                sol.put("positionCounter", 1);
                sol.put("value", 1);
                sol.put("self_count", 2);
            }
            if(index == 5) {
                sol.put("positionCounter", 2);
                sol.put("value", 0);
                sol.put("self_count", 1);
            }
            if(index == 6) {
                sol.put("positionCounter", -1);
                sol.put("value", 0);
                sol.put("self_count", 4);
            }
            if(index == 7) {
                sol.put("positionCounter", 0);
                sol.put("value", 1);
                sol.put("self_count", 3);
            }
        }

        HashMap first_sol = targetSols.get(0);
        first_sol.put("targetSolenoid", 0);
        first_sol.put("targetPositionCounter", 1);

        int prevPosCounter = 1;

        // Traverse through the given input line and determine what the final target values should be for the solenoids.
        for(int i=1; i<line.length(); i++) {
            HashMap curSol = targetSols.get(i);
            int parsedInt = Integer.parseInt(String.valueOf(line.charAt(i)));
            if(line.charAt(i-1) == '1' && line.charAt(i) == '1' && prevPosCounter < 1) {
                curSol.put("targetSolenoid", i-1);
                curSol.put("targetPositionCounter", prevPosCounter + SUCCESSIVE_ONES_GAP_IN_MILLIMETER);
                curSol.put("targetValue", parsedInt);
                prevPosCounter += SUCCESSIVE_ONES_GAP_IN_MILLIMETER;
            }else if (line.charAt(i-1) == '1' && line.charAt(i) == '1' && prevPosCounter >= 1 && prevPosCounter <= 2) {
                curSol.put("targetSolenoid", i);
                curSol.put("targetPositionCounter", (prevPosCounter + SUCCESSIVE_ONES_GAP_IN_MILLIMETER) / 4);
                curSol.put("targetValue", parsedInt);
                prevPosCounter = (prevPosCounter + SUCCESSIVE_ONES_GAP_IN_MILLIMETER) / 4;
            }else if(line.charAt(i-1) == '0' && line.charAt(i) == '1') {
                curSol.put("targetPositionCounter", prevPosCounter);
                curSol.put("targetSolenoid", i);
                curSol.put("targetValue", parsedInt);
            }else if(line.charAt(i-1) == '1' && line.charAt(i) == '0') {
                curSol.put("targetPositionCounter", prevPosCounter);
                curSol.put("targetSolenoid", i);
                curSol.put("targetValue", parsedInt);
            }else if(line.charAt(i-1) == '0' && line.charAt(i) == '0') {
                curSol.put("targetPositionCounter", prevPosCounter);
                curSol.put("targetSolenoid", i);
                curSol.put("targetValue", parsedInt);
            }
        }

        int resultLength = 0;

        while(resultLength < line.length()) {

            // Check which solenoids match the targeted values.
            for(int i=0; i<line.length(); i++) {
                HashMap currentSol = solenoids.get(i);
                HashMap currentTarget = targetSols.get(i);

                int curPosCounter = (int) currentSol.get("positionCounter");
                int curTargetPosCounter = (int) currentTarget.get("targetPositionCounter");

                int curValue = (int) currentSol.get("value");
                int curTargetValue = (int) currentTarget.get("targetValue");

                if(curPosCounter == curTargetPosCounter && curValue == curTargetValue) {
                    driver.fire(i);
                    resultLength++;
                    result[i] = curValue;
                }
            }

            if(resultLength == line.length()) {
                break;
            }

            driver.step();

            // After stepper moves 1mm to the left, adjust values corresponding to each solenoid.
            for(int i=0; i<solenoids.size(); i++) {
                HashMap currentSol = solenoids.get(i);
                int counter = (int) currentSol.get("positionCounter");
                int cur_value = (int) currentSol.get("value");
                int cur_self_count = (int) currentSol.get("self_count");

                if(counter == -1) {
                    currentSol.put("positionCounter", 2);
                }else {
                    currentSol.put("positionCounter", counter - 1);
                }

                if(cur_self_count == 4) {
                    currentSol.put("self_count", 1);
                    currentSol.put("value", cur_value ^ 1);
                }else{
                    currentSol.put("self_count", cur_self_count + 1);
                }
            }
        }

        if(resultLength == line.length()) {
            driver.linefeed();
        }

        return Arrays.stream(result).mapToObj(String::valueOf).collect(Collectors.joining(""));
    }
}
