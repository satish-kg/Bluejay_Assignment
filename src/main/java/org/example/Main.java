package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class App {

    private static final String EXCELFILE = "C:\\Users\\satis\\Documents\\IdeaProjects\\BluejayAssignment\\src\\main\\java\\org\\example\\Assignment_Timecard.xlsx";

    public static void main(String[] args) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

        try {
            FileInputStream file = new FileInputStream(new File(EXCELFILE));
            Workbook workbook = new XSSFWorkbook(file);

            //            Assuming there is always one sheet
            Sheet sheet = workbook.getSheetAt(0);

            //            Creating a map to store employee position and name
            Map<String, String> employeeNameAndPositionMap = new HashMap<>();

            //            Creating a map to store employee data of entry-exit times
            Map < String, List<Date>> employeeData = new HashMap<>();

            //            Map for time between shifts
            Map < String, List<Date>> shiftTimeData = new HashMap<>();

            //            Read data from the sheet
            for (Row row: sheet) {

                if (row.getRowNum() == 0) continue; //Skipping the header row
                String employeeName = row.getCell(7).getStringCellValue(); //As emmployee name is provided in 8th column
                String employeePosition = row.getCell(0).getStringCellValue(); //As employee position is provided in 1st column

                //              NEED TO CHECK FOR BLANK CELL
                Cell timeInCell = row.getCell(2); //As TimeIn is in 3rd column
                Cell timeOutCell = row.getCell(3); //As TimeOut is in 4th column

                //              Adding data to name-position map
                if (!employeeNameAndPositionMap.containsKey(employeeName)) {
                    employeeNameAndPositionMap.put(employeeName, employeePosition);
                }

                //              Adding data to the entry-exit map
                if (!employeeData.containsKey(employeeName)) {
                    employeeData.put(employeeName, new ArrayList<>());
                }

                if (timeInCell.getCellType() == CellType.NUMERIC && timeOutCell.getCellType() == CellType.NUMERIC) {
                    Date timeIn = timeInCell.getDateCellValue();
                    employeeData.get(employeeName).add(timeIn);
                    Date timeOut = timeOutCell.getDateCellValue();
                    employeeData.get(employeeName).add(timeOut);
                }
            }

            //           Performing analysis
            analyzeData(employeeData, employeeNameAndPositionMap);

            //            Closing as we have completed the task.
            workbook.close();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyzeData(Map < String, List<Date>> employeeData, Map<String, String> employeeNameAndPositionMap) {

        //        Example : Print employee names who worked for 7 consecutive days
        System.out.println("Employees who worked for 7 consecutive days: \n");
        //        Iterating through hashmap to load attendance data for every employee
        for (Map.Entry < String, List<Date>> entry: employeeData.entrySet()) {
            //            Taking out in-and-out timings of every employee
            List<Date> dates = entry.getValue();
            //           Taking value of 'n' as 7 for '7 consecutive days'
            int n = 7;

            if (hasConsecutiveDays(n, dates)) {
                //                System.out.println("Employees who worked for 7 consecutive days: " + entry.getKey());
                //                System.out.println("Name: " + entry.getKey() + ", Position: " + employeeNameAndPositionMap.get(entry.getKey()));
                System.out.println("Name : " + entry.getKey() + ", Position : " + employeeNameAndPositionMap.get(entry.getKey()));
            }
        }

        //        Example : Print employee names with less than 10 hours between shifts but greater than 1 hour
        System.out.println("\n");
        System.out.println("Employees with less than 10 hours between shifts but greater than 1 hour: \n");
        //        Iterating through hashmap to load attendance data for every employee
        for (Map.Entry < String, List<Date>> entry: employeeData.entrySet()) {
            //            Taking out in-and-out timings of every employee
            List<Date> dates = entry.getValue();
            //            Creating an object of custom class 'ShiftTimeDifferenceData to retrieve return data such as 'time difference', 'in-time' and 'out-time' from the function
            ShiftTimeDifferenceData shiftTimeDifferenceData = hasTimeBetweenShifts(dates);
            //            Performing check to print only correct and required enties
            if (shiftTimeDifferenceData.timeDifference != -1) {
                //                Required output
                System.out.println("Name : " + entry.getKey() + ", Position : " + employeeNameAndPositionMap.get(entry.getKey()));
                //                Output containing employee name, position, time difference, in-timme and out-time
                //                System.out.println("Name : "+ entry.getKey()+", Position : "+employeeNameAndPositionMap.get(entry.getKey())+", Time Difference : "+shiftTimeDifferenceData.timeDifference+", InTime of shift : "+shiftTimeDifferenceData.inTime+", OutTime of previous shift :  "+ shiftTimeDifferenceData.outTime);
            }
        }

        System.out.println("\n");
        System.out.println("Employee who worked for more than 14 hours in a single shift: \n");
        //        Iterating through hashmap to load attendance data for every employee//        Iterating through hashmap to load attendance data for every employee
        for (Map.Entry < String, List<Date>> entry: employeeData.entrySet()) {
            //            Taking out in-and-out timings of every employee
            List<Date> dates = entry.getValue();
            //            Creating an object of custom class 'ShiftTimeDifferenceData to retrieve return data such as 'time difference', 'in-time' and 'out-time' from the function
            ShiftTimeDifferenceData shiftTimeDifferenceData = hasHoursInSingleShift(dates);
            //            Performing check to print only correct and required enties
            if (shiftTimeDifferenceData.timeDifference != -1) {
                System.out.println("Name : " + entry.getKey() + ", Position : " + employeeNameAndPositionMap.get(entry.getKey()));
                //                Output containing employee name, position, time difference, in-timme and out-time
                //                System.out.println("Name : "+ entry.getKey()+", Position : "+employeeNameAndPositionMap.get(entry.getKey())+", Time Difference : "+shiftTimeDifferenceData.timeDifference+", InTime of shift : "+shiftTimeDifferenceData.inTime+", OutTime of previous shift :  "+ shiftTimeDifferenceData.outTime);
            }
        }
    }

    //    Function to verify if the employee has attended office for 'n' consecutive days
    private static boolean hasConsecutiveDays(int n, List<Date> dates) {
        //         Creating an object of custom class 'ShiftTimeDifferenceData to retrieve time data such as 'time difference', 'in-time' and 'out-time'
        ShiftTimeDifferenceData shiftTimeDifferenceData = new ShiftTimeDifferenceData();
        //        As we need 'n' consecutive days of attendance, the date-list size must be at least '2n' as date-list contains both 'int-time' and 'out-time'.
        if (dates.size()<14) {
            return false;
        }

        //        Creating a simple date format object to format the dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //        Creating a set to created newly formatted and unique dates
        Set<String> consecutiveDaysSet = new HashSet<>();

        //        Putting dates in date-set after formatting
        for (Date date: dates) {
            String formattedDate = dateFormat.format(date);
            consecutiveDaysSet.add(formattedDate);
        }

        //        sending int 'n' and date-set to other function to verifiy if employee has attended office for 'n' consecutive days.
        if (hasN_consecutiveDays(n, consecutiveDaysSet)) {
            return true;
        }

        return false;
    }

    private static boolean hasN_consecutiveDays(int n, Set<String> dateSet) {
        //        Creating instance of java.util.Calendar
        Calendar calendar = Calendar.getInstance();

        //        Iterating through set of dates to check for 'n' consecutive dates.
        for (String date: dateSet) {
            try {
                //                Setting calendar's time with date of 'dateset'
                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date));
                //                Counter for consecutive days count
                int consecutiveDaysCount = 0;

                //                Iterating over a for loop 7 times
                for (int i = 1; i <= n; i++) {
                    //                    Setting calendar's time one day ahead of current date from 'dateset'
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    //                    Parsing this one day ahead time from calendar as string
                    String nextDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

                    //                    Checking if the 'dateset' contains this 'one day ahead' date
                    if (dateSet.contains(nextDate)) {
                        //                        if yes, we increase consecutive days counter by 1
                        consecutiveDaysCount++;
                    } else {
                        //                        if no, we reset the counter to 0 and continue to iterate through dates from 'dateset'
                        consecutiveDaysCount = 0;
                    }
                }

                //                If the counter reaches count of 'n', we return true
                if (consecutiveDaysCount == n) {
                    return true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        //        returning false as employee hadn't attended office for 7 consecutive days.
        return false;
    }

    private static ShiftTimeDifferenceData hasTimeBetweenShifts(List<Date> dates) {
        // Implement logic to check time between shifts
        ShiftTimeDifferenceData shiftTimeDifferenceData = new ShiftTimeDifferenceData();

        for (int i = 2; i < dates.size() - 1; i += 2) {
            //            System.out.println(dates.get(i));
            long diffInSeconds = (dates.get(i).getTime() - dates.get(i - 1).getTime()) / 1000;

            if (diffInSeconds > 3600 && diffInSeconds  <  10 * 3600) {
                //                Setting time-difference, in-time and out-time in return object.
                shiftTimeDifferenceData.timeDifference = diffInSeconds;
                shiftTimeDifferenceData.inTime = dates.get(i + 1);
                shiftTimeDifferenceData.outTime = dates.get(i);
                return shiftTimeDifferenceData;
            }
        }

        shiftTimeDifferenceData.timeDifference = -1;
        return shiftTimeDifferenceData;
    }

    private static ShiftTimeDifferenceData hasHoursInSingleShift(List<Date> dates) {
        // Implement logic to check hours in a single shift
        ShiftTimeDifferenceData shiftTimeDifferenceData = new ShiftTimeDifferenceData();

        for (int i = 1; i < dates.size(); i += 2) {
            //            Setting time-difference in return object
            shiftTimeDifferenceData.timeDifference = ((dates.get(i).getTime() - dates.get(i - 1).getTime()) / (3600 * 1000));

            if (shiftTimeDifferenceData.timeDifference > 14) {
                //                Setting in-time and out-time in return object.
                shiftTimeDifferenceData.inTime = dates.get(i - 1);
                shiftTimeDifferenceData.outTime = dates.get(i);
                return shiftTimeDifferenceData;
            }
        }

        shiftTimeDifferenceData.timeDifference = -1;
        return shiftTimeDifferenceData;
    }
}