import service.TDService;

import java.util.TimerTask;

public class ScheduledTask extends TimerTask {
    int i = 0;
    int j = 0;
    public void run() {
        try {
            System.out.println("before executing " + i);
            i++;
            TDService.execute();
            System.out.println("after executing " + j);
            j++;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
