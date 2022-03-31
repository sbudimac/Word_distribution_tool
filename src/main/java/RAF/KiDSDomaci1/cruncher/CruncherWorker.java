package RAF.KiDSDomaci1.cruncher;

import RAF.KiDSDomaci1.app.Config;

import java.util.*;
import java.util.concurrent.RecursiveTask;

public class CruncherWorker extends RecursiveTask<Map<String, Long>> {
    private int counterDataLimit;
    private String content;
    private int arity;
    private int start;
    private int length;
    private boolean smallJob;

    public CruncherWorker(String content, int arity, int start, int length, boolean smallJob) {
        this.counterDataLimit = Integer.parseInt(Config.getProperty("counter_data_limit"));
        this.content = content;
        this.arity = arity;
        this.start = start;
        this.length = length;
    }

    @Override
    protected Map<String, Long> compute() {
        Map<String, Long> crunchingResult = new HashMap<>();
        if (smallJob) {
            LinkedList<String> currentBag = new LinkedList<>();
            if (length - start <= arity) {
                return crunchingResult;
            } else {
                int pointer = start;
                int previousWordIndex = start;
                int wordNumber = 0;
                for (; pointer < length; pointer++) {
                    if (wordNumber < arity) {
                        char c = content.charAt(pointer);
                        if (c == ' ' || c == '\t' || c == '\n') {
                            currentBag.add(content.substring(previousWordIndex, pointer));
                            previousWordIndex = pointer + 1;
                            wordNumber++;
                        }
                    } else {
                        break;
                    }
                }
                if (pointer == length) {
                    currentBag.add(content.substring(previousWordIndex, pointer));
                    insertBag(currentBag, crunchingResult);
                    return crunchingResult;
                }
                insertBag(currentBag, crunchingResult);
                for (; pointer < length; pointer++) {
                    char c = content.charAt(pointer);
                    if (c == ' ' || c == '\t' || c == '\n') {
                        currentBag.remove(0);
                        currentBag.add(content.substring(previousWordIndex, pointer));
                        previousWordIndex = pointer + 1;
                        insertBag(currentBag, crunchingResult);
                    }
                }
                if (pointer == length) {
                    currentBag.remove(0);
                    currentBag.add(content.substring(previousWordIndex, pointer));
                    insertBag(currentBag, crunchingResult);
                }
            }
        } else {

        }
        return crunchingResult;
    }

    private void insertBag(LinkedList<String> words, Map<String, Long> crunchingResult) {
        List<String> sortedWords = (LinkedList<String>) words.clone();
        Collections.sort(sortedWords);
        String bag = String.join(", ", sortedWords);
        if (crunchingResult.containsKey(bag)) {
            crunchingResult.put(bag, crunchingResult.get(bag) + 1);
        } else {
            crunchingResult.put(bag, 1L);
        }
    }
}
