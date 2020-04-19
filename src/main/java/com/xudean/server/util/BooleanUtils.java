package com.xudean.server.util;

public class BooleanUtils {
    public static boolean string2Boolean(String strBool) {
        switch (strBool.toLowerCase()) {
            case "true":
            case "1":
                return Boolean.TRUE;
        }
        return false;
    }
}
