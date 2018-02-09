package com.zork.exceptionhandle;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Hello world!
 *
 */


public class App {
    public static void main( String[] args ) {
//        System.out.println( "Hello World!" );
        // 20180131 23:57:23

        String time = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
        System.out.println(time);

        String a = "123456789";
        System.out.println(a.substring(0,6));
    }
}
