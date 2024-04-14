import java.sql.SQLException;
import java.util.*;


public class Main {
    public static void main(String[] args) throws SQLException, InterruptedException {
        Timer time = new Timer();
        ScheduledTask st = new ScheduledTask();
        time.schedule(st, 0, 30000);  // task repeating every 30 sec
    }
}
