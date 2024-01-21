package org.example;
import java.io.*;
import java.util.*;
import java.lang.*;

public class ShiftTimeDifferenceData {

    float timeDifference;
    Date inTime;
    Date outTime;

    public void printShiftTimeDifferenceData() {
        System.out.println("Time Difference : "+timeDifference+" seconds, inTime : "+inTime+", outTime : "+outTime);
    }

    public void clearData(){
        timeDifference = 0;
        inTime = null;
        outTime = null;

    }
}
