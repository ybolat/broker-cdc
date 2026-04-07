package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Main {
    public record Coordinates(double x, double y){}

    public static void main(String[] args) {
        List<Double> numbers = new ArrayList<>();

//        for (int i = 0; i < 180; i++) {
//            numbers.add(new Random(10).nextInt());
//        }

        System.out.println(numbers);
    }

    List<Coordinates> getCoordinatesList(List<Double> distanceList) {
        List<Coordinates> result = new ArrayList<>();

        for (int i = 0; i < distanceList.size(); i++) {
            double x = Math.sin(i) * distanceList.get(i);
            double y = Math.cos(i) * distanceList.get(i);
            result.add(new Coordinates(x, y));
        }

        return result;
    }
}