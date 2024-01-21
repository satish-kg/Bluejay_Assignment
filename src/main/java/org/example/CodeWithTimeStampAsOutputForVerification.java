package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class CodeWithTimeStampAsOutputForVerification {

    private static final String EXCELFILE = "C:\\Users\\satis\\Downloads\\Assignment_Timecard.xlsx";

    public static void main (String[] args){

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

        try{
            FileInputStream file = new FileInputStream(new File(EXCELFILE));
            Workbook workbook = new XSSFWorkbook(file);

//            Assuming there is always one sheet
            Sheet sheet = workbook.getSheetAt(0);
//            Iterator<Sheet> sheets = workbook.sheetIterator();
//            Sheet sheet = sheets.next();

//            Creating a map to store employee position and name
            Map<String, String> employeeNameAndPositionMap = new HashMap<>();

//            Creating a map to store employee data of entry-exit times
            Map<String, List<Date>> employeeData = new HashMap<>();

//            Creating a map of Date and employee name for consecutive days counting
//            Map<String, List<>>


//            Map for time between shifts
            Map<String, List<Date>> shiftTimeData = new HashMap<>();

//            Read data from the sheet
            for (Row row : sheet){
                if(row.getRowNum() == 0) continue; //Skipping the header row

                String employeeName = row.getCell(7).getStringCellValue();  //As emmployee name is provided in 8th column
                String employeePosition = row.getCell(0).getStringCellValue();  //As employee position is provided in 1st column
//                System.out.println("Employee Name : "+employeeName);
//                System.out.println("Row No. : " + row.getRowNum());

//              NEED TO CHECK FOR BLANK CELL
                Cell timeInCell = row.getCell(2);  //As TimeIn is in 3rd column
//                System.out.println(timeInCell.getCellType());
                Cell timeOutCell = row.getCell(3);  //As TimeOut is in 4th column

//              Adding data to name-position map
                if (!employeeNameAndPositionMap.containsKey(employeeName)){
                    employeeNameAndPositionMap.put(employeeName, employeePosition);
                }
//              Adding data to the entry-exit map
                if (!employeeData.containsKey(employeeName)){
                    employeeData.put(employeeName, new ArrayList<>());
                }

//              Adding data to shiftTime map
                if (!shiftTimeData.containsKey(employeeName)) {
                    shiftTimeData.put(employeeName, new ArrayList<>());
                }

//                System.out.println("Parsed dates : \n");
                if (timeInCell.getCellType() == CellType.NUMERIC && timeOutCell.getCellType() == CellType.NUMERIC){
                    Date timeIn = timeInCell.getDateCellValue();
                    employeeData.get(employeeName).add(timeIn);
                    Date timeOut = timeOutCell.getDateCellValue();
                    employeeData.get(employeeName).add(timeOut);
//                    DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
//                    Date dateOnly = formatter.parse(formatter.format(timeIn.getTime()));
//                    System.out.println(dateOnly);
                }
            }

            boolean flag = true;
            for (Map.Entry<String, List<Date>> entry : employeeData.entrySet()) {
                if (Objects.equals(entry.getKey(), "GaXCes, EXias XEpez")) {
//                if(entry.getKey() == "GaXCes, EXias XEpez"){
//                    System.out.println(entry.getKey() + " : \n");
//                    List<Date> dates = entry.getValue();
//                    while (flag) {
//                        for (int x = 0; x < dates.size(); x++) {
//                            System.out.println(dates.get(x).getTime());
//                        }
//                        flag = false;
//                    }
                }
            }

//           Performing analysis
            analyzeData(employeeData, employeeNameAndPositionMap);


            workbook.close();
            file.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void analyzeData(Map<String, List<Date>> employeeData, Map<String, String> employeeNameAndPositionMap){
//        Example : Print employee names who worked for 7 consecutive days
//        System.out.println("\n");
        System.out.println("Employees who worked for 7 consecutive days: \n");
        for(Map.Entry<String, List<Date>> entry : employeeData.entrySet()) {
//            System.out.println("\nChecking consecutive days for "+entry.getKey());
            List<Date> dates = entry.getValue();
//           Taking value of 'n' as 7 for '7 consecutive days'
            int n = 7;
            ShiftTimeDifferenceData shiftTimeDifferenceData = hasConsecutiveDays (7, dates);
            if (shiftTimeDifferenceData.timeDifference != -1){
//                System.out.println("Employees who worked for 7 consecutive days: " + entry.getKey());
//                System.out.println("Name: " + entry.getKey() + ", Position: " + employeeNameAndPositionMap.get(entry.getKey()));
                System.out.println("Name : "+ entry.getKey()+", Position : "+employeeNameAndPositionMap.get(entry.getKey()));
            }
        }

//        Example : Print employee names with less than 10 hours between shifts but greater than 1 hour
        System.out.println("\n");
        System.out.println("Employees with less than 10 hours between shifts but greater than 1 hour: \n");
        for (Map.Entry<String, List<Date>> entry : employeeData.entrySet()) {
            List<Date> dates = entry.getValue();
            ShiftTimeDifferenceData shiftTimeDifferenceData = hasTimeBetweenShifts(dates);
            if(shiftTimeDifferenceData.timeDifference != -1){
                System.out.println("Name : "+ entry.getKey()+", Position : "+employeeNameAndPositionMap.get(entry.getKey())+", Time Difference : "+shiftTimeDifferenceData.timeDifference+", InTime of shift : "+shiftTimeDifferenceData.inTime+", OutTime of previous shift :  "+ shiftTimeDifferenceData.outTime);
            }
        }

        System.out.println("\n");
        System.out.println("Employee who worked for more than 14 hours in a single shift: \n");
        for (Map.Entry<String, List<Date>> entry : employeeData.entrySet()) {
            List<Date> dates = entry.getValue();
            ShiftTimeDifferenceData shiftTimeDifferenceData = new ShiftTimeDifferenceData();
            shiftTimeDifferenceData = hasHoursInSingleShift(dates);
            if (shiftTimeDifferenceData.timeDifference != -1) {
                System.out.println("Name : "+ entry.getKey()+", Position : "+employeeNameAndPositionMap.get(entry.getKey())+", Time Difference : "+shiftTimeDifferenceData.timeDifference+", InTime of shift : "+shiftTimeDifferenceData.inTime+", OutTime of shift :  "+ shiftTimeDifferenceData.outTime);
            }
        }
    }

//    private static ShiftTimeDifferenceData hasConsecutiveDays (int n, List<Date> dates) {
//        ShiftTimeDifferenceData shiftTimeDifferenceData = new ShiftTimeDifferenceData();
//        if (dates.size() < 7) {
//            shiftTimeDifferenceData.timeDifference = -1;
//            return shiftTimeDifferenceData;
//        }
//        for (int i = 1; i < dates.size() - 1; i+=2) {
//            System.out.println(dates.get(i) +" "+ dates.get(i).getTime());
//            shiftTimeDifferenceData.timeDifference = (dates.get(i + 1).getTime() - dates.get(i).getTime()) / ( 60 * 60 * 1000);
//            if (shiftTimeDifferenceData.timeDifference < 24) {
//                shiftTimeDifferenceData.inTime = dates.get(i+1);
//                shiftTimeDifferenceData.outTime = dates.get(i);
//                return shiftTimeDifferenceData;
//            }
//        }
//        shiftTimeDifferenceData.timeDifference = -1;
//        return shiftTimeDifferenceData;
//    }

    private static ShiftTimeDifferenceData hasConsecutiveDays (int n, List<Date> dates) {
        ShiftTimeDifferenceData shiftTimeDifferenceData = new ShiftTimeDifferenceData();
        if (dates.size() < 7) {
            shiftTimeDifferenceData.timeDifference = -1;
            return shiftTimeDifferenceData;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Set<String> consecutiveDaysSet = new HashSet<>();

        for (Date date : dates) {
            String formattedDate = dateFormat.format(date);
            consecutiveDaysSet.add(formattedDate);
        }

        if (hasN_consecutiveDays(n, consecutiveDaysSet)) {
            shiftTimeDifferenceData.inTime = dates.get(0);                  //First entry of 7 consecutive days
            shiftTimeDifferenceData.outTime = dates.get(dates.size()-2);   //Last entry of 7 consecutive days
            shiftTimeDifferenceData.timeDifference = 7;
            return shiftTimeDifferenceData;
        }
//        for (int i = 1; i < dates.size() - 1; i+=2) {
//
//            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
////            Date dateOnly = formatter.(formatter.format(dates.get(i).getTime()));
//            System.out.println(" "+dates.get(i).getClass() +" "+ dates.get(i).getTime());
//            shiftTimeDifferenceData.timeDifference = (dates.get(i + 1).getTime() - dates.get(i).getTime()) / ( 60 * 60 * 1000);
//            if (shiftTimeDifferenceData.timeDifference < 24) {
//                shiftTimeDifferenceData.inTime = dates.get(i+1);
//                shiftTimeDifferenceData.outTime = dates.get(i);
//                return shiftTimeDifferenceData;
//            }
//        }
        shiftTimeDifferenceData.timeDifference = -1;
        return shiftTimeDifferenceData;
    }

    private static boolean hasN_consecutiveDays (int n, Set<String> dateSet) {
        Calendar calendar = Calendar.getInstance();

        for (String date : dateSet) {
            try {
                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date));
                int consecutiveDaysCount = 0;

                for (int i = 1; i <= n; i++) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    String nextDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

                    if (dateSet.contains(nextDate)) {
                        consecutiveDaysCount++;
                    } else {
                        break;
                    }
                }

                if (consecutiveDaysCount == n) {
                    return true;
                }
            } catch (ParseException e){
                e.printStackTrace();
            }
        }
        return false;
    }

//    private static boolean hasTimeBetweenShifts(List<Date> dates) {
//        // Implement logic to check time between shifts
//        for (int i = 0; i < dates.size() - 1; i++) {
////            System.out.println(dates.get(i));
//            long diffInSeconds = (dates.get(i + 1).getTime() - dates.get(i).getTime()) / 1000;
//            if (diffInSeconds > 3600 && diffInSeconds < 10 * 3600) {
//                return true;
//            }
//        }
//        return false;
//    }

    private static ShiftTimeDifferenceData hasTimeBetweenShifts(List<Date> dates) {
        // Implement logic to check time between shifts
        ShiftTimeDifferenceData shiftTimeDifferenceData = new ShiftTimeDifferenceData();
        for (int i = 2; i < dates.size() - 1; i+=2) {
//            System.out.println(dates.get(i));
            long diffInSeconds = (dates.get(i).getTime() - dates.get(i-1).getTime()) / 1000;
            if (diffInSeconds > 3600 && diffInSeconds < 10 * 3600) {
                shiftTimeDifferenceData.timeDifference = diffInSeconds;
                shiftTimeDifferenceData.inTime = dates.get(i+1);
                shiftTimeDifferenceData.outTime = dates.get(i);
                return shiftTimeDifferenceData;
            }
        }
        shiftTimeDifferenceData.timeDifference = -1;
        return shiftTimeDifferenceData;
    }

//    private static boolean hasHoursInSingleShift(List<Date> dates) {
//        // Implement logic to check hours in a single shift
//        for (int i = 0; i < dates.size() - 2; i += 2) {
//            long diffInSeconds = (dates.get(i + 2).getTime() - dates.get(i).getTime()) / 1000;
//            if (diffInSeconds / 3600 > 14) {
//                return true;
//            }
//        }
//        return false;
//    }

    private static ShiftTimeDifferenceData hasHoursInSingleShift(List<Date> dates) {
        // Implement logic to check hours in a single shift
        ShiftTimeDifferenceData shiftTimeDifferenceData = new ShiftTimeDifferenceData();
        for (int i = 1; i < dates.size(); i += 2) {
            shiftTimeDifferenceData.timeDifference = ((dates.get(i).getTime() - dates.get(i-1).getTime()) / (3600*1000));
            if (shiftTimeDifferenceData.timeDifference > 14) {
                shiftTimeDifferenceData.inTime = dates.get(i-1);
                shiftTimeDifferenceData.outTime = dates.get(i);
                return shiftTimeDifferenceData;
            }
        }
        shiftTimeDifferenceData.timeDifference = -1;
        return shiftTimeDifferenceData;
    }
}
