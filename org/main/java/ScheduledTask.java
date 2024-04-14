import service.TDService;

import java.util.TimerTask;

public class ScheduledTask extends TimerTask {
    public void run() {
        try {
            TDService.execute();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
