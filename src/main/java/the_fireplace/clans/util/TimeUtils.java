package the_fireplace.clans.util;

import java.util.Date;

public class TimeUtils {
    public static String getFormattedTime(long timeStamp) {
        return new Date(timeStamp).toString();
    }
}
