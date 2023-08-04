package com.example.demo;

import com.example.demo.services.Algorithms.Coordinate;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.example.demo.services.Algorithms.shortestPath.findShortestTotalDistance;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class shortestPathTest {

    @Test
    void testFindShortestPath() {
        List<Coordinate> coordinates = new ArrayList<>();

        coordinates.add(new Coordinate(52.952304, -1.1539236));
        coordinates.add(new Coordinate(52.92756749999999, -1.1602149));
        coordinates.add(new Coordinate(52.9447152, -1.16436));
        coordinates.add(new Coordinate(52.9514874, -1.2079374));
        coordinates.add(new Coordinate(52.95197, -1.1524323));
        coordinates.add(new Coordinate(52.9547335, -1.1938571));
        coordinates.add(new Coordinate(52.95548110000001, -1.1534742));
        coordinates.add(new Coordinate(52.9343679, -1.1628795));
        coordinates.add(new Coordinate(52.9536032, -1.1523079));
        coordinates.add(new Coordinate(52.947984, -1.1517388));
        coordinates.add(new Coordinate(52.9482739, -1.1485477));
        coordinates.add(new Coordinate(52.9536917999999, -1.1528439));
        coordinates.add(new Coordinate(52.9314309, -1.1813696));
        coordinates.add(new Coordinate(52.9539574, -1.1546937));
        coordinates.add(new Coordinate(52.95295600000001, -1.153436));
        coordinates.add(new Coordinate(52.9516463, -1.1542065));
        coordinates.add(new Coordinate(52.9557731, -1.1747454));
        coordinates.add(new Coordinate(52.93865539999999, -1.1879212));
        coordinates.add(new Coordinate(52.937935, -1.164975));
        coordinates.add(new Coordinate(52.9447152, -1.16436));
        coordinates.add(new Coordinate(52.9525071, -1.1517144));
        coordinates.add(new Coordinate(52.93865539999999, -1.1879212));
        coordinates.add(new Coordinate(52.9385089, -1.1716972));
        coordinates.add(new Coordinate(52.93818, -1.1873113));
        coordinates.add(new Coordinate(52.9514874, -1.2079374));
        coordinates.add(new Coordinate(52.935103, -1.1787915));
        coordinates.add(new Coordinate(52.9354258, -1.1540254));
        coordinates.add(new Coordinate(52.9549692, -1.1530009));
        coordinates.add(new Coordinate(52.9382503, -1.1875022));
        coordinates.add(new Coordinate(52.9475878, -1.183609));
        coordinates.add(new Coordinate(52.9413071, -1.1890411));
        coordinates.add(new Coordinate(52.9439098, -1.1500284));
        coordinates.add(new Coordinate(52.93539, -1.175493));
        coordinates.add(new Coordinate(52.94485400000001, -1.1629057));
        coordinates.add(new Coordinate(52.9385089, -1.1716972));


        Collections.shuffle(coordinates);
        List<Coordinate> subCoordinate = coordinates.subList(0, 5);
               Coordinate currentLocation = new Coordinate(52.9428103, -1.1793827);


             System.out.println(findShortestTotalDistance(subCoordinate, currentLocation));

    }
}

