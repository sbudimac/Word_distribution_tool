package RAF.KiDSDomaci1.cruncher;

import RAF.KiDSDomaci1.app.Config;

import java.util.Map;
import java.util.concurrent.RecursiveTask;

public class CruncherWorker extends RecursiveTask<Map<String, Long>> {
    private int counterDataLimit;
    private String content;
    private int airity;

    public CruncherWorker(String content, int airity) {
        this.counterDataLimit = Integer.parseInt(Config.getProperty("counter_data_limit"));
        this.content = content;
        this.airity = airity;
    }

    @Override
    protected Map<String, Long> compute() {
        return null;
    }
}
