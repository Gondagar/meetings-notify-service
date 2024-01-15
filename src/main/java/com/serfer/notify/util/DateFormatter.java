package com.serfer.notify.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatter {


    public static String getCurrentDateTime(){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String date = LocalDateTime.now().format(formatter);
        return  date;
    }


    public static String getCurrentTime(LocalDateTime localDateTime){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String date = localDateTime.format(formatter);
        return  date;
    }

}
