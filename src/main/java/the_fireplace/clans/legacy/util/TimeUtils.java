package the_fireplace.clans.legacy.util;

import java.util.Date;

public class TimeUtils {
    public static String getFormattedTime(long timeStamp) {
        return new Date(timeStamp).toString();
    }
}
