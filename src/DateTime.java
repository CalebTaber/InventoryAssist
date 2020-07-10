import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class DateTime {

    private int month, day, year;
    private int hour, minute;

    public static final DateTime DEFAULT = new DateTime("01/01/2000 01:00");

    public DateTime(String dateTime) {
        String[] separated = dateTime.split(" ");
        String[] d = separated[0].split("/");
        month = Integer.parseInt(d[0]);
        day = Integer.parseInt(d[1]);
        year = Integer.parseInt(d[2]);

        String[] t = separated[1].split(":");
        hour = Integer.parseInt(t[0]);
        minute = Integer.parseInt(t[1]);
    }

    public int compare(DateTime compare) {
        if (compare.year < year) return -1;
        else if (compare.year > year) return 1;

        if (compare.month < month) return -1;
        else if (compare.month > month) return 1;

        if (compare.day < day) return -1;
        else if (compare.day > day) return 1;

        if (compare.hour < hour) return -1;
        else if (compare.hour > hour) return 1;

        if (compare.minute < minute) return -1;
        else if (compare.minute > minute) return 1;
        else return 0;
    }

    public int minutesUntil(DateTime t) {
        if (compare(t) > 0) return 0;

        int minutesUntil = 0;
        minutesUntil += (t.year - year) * 525960;
        minutesUntil += (t.month - month) * 43830;
        minutesUntil += (t.day - day) * 1440;
        minutesUntil += (t.hour - hour) * 60;
        minutesUntil += t.minute - minute;

        return minutesUntil;
    }

    public void write(BufferedWriter writer) throws IOException {
        writer.write("Due:" + toString() + "\n");
    }

    public String toString() {
        DecimalFormat two = new DecimalFormat("00");
        DecimalFormat four = new DecimalFormat("0000");
        return two.format(month) + "/" + two.format(day) + "/" + four.format(year) + " " + two.format(hour) + ":" + two.format(minute);
    }

}
