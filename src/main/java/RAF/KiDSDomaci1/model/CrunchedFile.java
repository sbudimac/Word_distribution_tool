package RAF.KiDSDomaci1.model;

import java.util.Map;
import java.util.concurrent.Future;

public class CrunchedFile {
    private String name;
    private Future<Map<String, Long>> repetitions;

    public CrunchedFile(String name, Future<Map<String, Long>> repetitions) {
        this.name = name;
        this.repetitions = repetitions;
    }
}
