package kl.rest.service.util;

public class BooleanUtils {
    public static boolean string2Boolean(String strBool) {
        switch (strBool) {
            case "true":
                return true;
            case "1":
                return true;
        }
        return false;
    }
}
