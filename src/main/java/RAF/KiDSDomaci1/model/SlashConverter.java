package RAF.KiDSDomaci1.model;

public class SlashConverter {

    public static String currentFileName(String filePath) {
        String regexPath = filePath.replace("\\", "/");
        return regexPath.split("/")[regexPath.split("/").length - 1];
    }
}
