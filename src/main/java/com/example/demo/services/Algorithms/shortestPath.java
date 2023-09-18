package com.example.demo.services.Algorithms;

import java.util.*;

public class shortestPath {

    private static double calculateDistance(Coordinate c1, Coordinate c2) {
        //Haversine formula to calculate the distance between two coordinates oon the earths surface.
        double earthRadius = 6371; // Radius of the Earth in kilometers
        double lat1 = Math.toRadians(c1.x);
        double lon1 = Math.toRadians(c1.y);
        double lat2 = Math.toRadians(c2.x);
        double lon2 = Math.toRadians(c2.y);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    public static double findShortestTotalDistance(List<Coordinate> coordinates, Coordinate currentDestination) {
        int n = coordinates.size();
        List<Coordinate> shortestPath = null;
        double shortestDistance = Double.POSITIVE_INFINITY;

        // Generate all permutations of the coordinates starting from currentDestination
        List<List<Coordinate>> permutations = new ArrayList<>();
        generatePermutations(coordinates, new ArrayList<>(), currentDestination, permutations);

        // Find the shortest total distance among all permutations
        for (List<Coordinate> permutation : permutations) {
            double totalDistance = calculateDistance(currentDestination, permutation.get(0));
            for (int i = 0; i < n - 1; i++) {
                totalDistance += calculateDistance(permutation.get(i), permutation.get(i + 1));
            }
            if (totalDistance < shortestDistance) {
                shortestDistance = totalDistance;
            }
        }

        return shortestDistance;
    }

    private static void generatePermutations(List<Coordinate> coordinates, List<Coordinate> currentPermutation, Coordinate currentDestination, List<List<Coordinate>> permutations) {
        if (coordinates.isEmpty()) {
            permutations.add(new ArrayList<>(currentPermutation));
            return;
        }

        for (int i = 0; i < coordinates.size(); i++) {
            Coordinate coordinate = coordinates.get(i);
            currentPermutation.add(coordinate);
            List<Coordinate> remainingCoordinates = new ArrayList<>(coordinates);
            remainingCoordinates.remove(i);
            generatePermutations(remainingCoordinates, currentPermutation, currentDestination, permutations);
            currentPermutation.remove(coordinate);
        }
    }

    public static void main(String[] args) {
        // Example usage:
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(52.9539574, -1.1546937)); // Coordinate 1
        coordinates.add(new Coordinate(52.9354258, -1.1540254)); // Coordinate 2    2.06
        coordinates.add(new Coordinate(52.9447152, -1.16436)); // Coordinate 3
        coordinates.add(new Coordinate( 52.9482739, -1.1485477)); // Coordinate 4
        coordinates.add(new Coordinate(52.951975, -1.1524323)); // Coordinate 5

        Coordinate currentLocation = new Coordinate(52.9428103, -1.1793827);

        System.out.println(findShortestTotalDistance(coordinates, currentLocation));
    }
}

