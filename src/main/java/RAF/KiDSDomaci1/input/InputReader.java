package RAF.KiDSDomaci1.input;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class InputReader implements Callable<String> {
    private String path;

    public InputReader(String path) {
        this.path = path;
    }

    @Override
    public String call() throws Exception {
        File file = new File(path);
        FileInputStream stream = new FileInputStream(file);
        byte[] content = new byte[(int) file.length()];
        stream.read(content);
        stream.close();
        String fileContent = new String(content, StandardCharsets.US_ASCII);
        return fileContent;
    }
}
