package RAF.KiDSDomaci1.model;

public class FileContent {
    private String name;
    private String content;

    public FileContent(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
