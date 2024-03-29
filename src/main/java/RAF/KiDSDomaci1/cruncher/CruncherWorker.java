package RAF.KiDSDomaci1.cruncher;

import RAF.KiDSDomaci1.app.Config;
import RAF.KiDSDomaci1.view.MainView;

import java.util.*;
import java.util.concurrent.RecursiveTask;

public class CruncherWorker extends RecursiveTask<Map<String, Long>> {
    private MainView mainView;
    private int counterDataLimit;
    private String content;
    private int arity;
    private int start;
    private int length;
    private boolean subJob;

    public CruncherWorker(MainView mainView, String content, int arity, int start, int length, boolean subJob) {
        this.counterDataLimit = Integer.parseInt(Config.getProperty("counter_data_limit"));
        this.mainView = mainView;
        this.content = content;
        this.arity = arity;
        this.start = start;
        this.length = length;
        this.subJob = subJob;
    }

    @Override
    protected Map<String, Long> compute() {
        try {
            Map<String, Long> crunchingResult = new HashMap<>();
            if (subJob) {
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
                List<CruncherWorker> subWorkers = new ArrayList<>();
                int previousScopeIndex = 0;
                for (int L = counterDataLimit; L < length; L += counterDataLimit) {
                    char c = content.charAt(L);
                    if (c != ' ' && c != '\t' && c != '\n') {
                        L++;
                    }
                    subWorkers.add(new CruncherWorker(mainView, content, arity, previousScopeIndex, L, true));
                    previousScopeIndex = L + 1;
                }
                for (CruncherWorker subWorker : subWorkers) {
                    subWorker.fork();
                }
                CruncherWorker lastWorker = new CruncherWorker(mainView, content, arity, previousScopeIndex, length, true);
                Map<String, Long> lastResult = lastWorker.compute();
                List<Map<String, Long>> subWorkerResults = new ArrayList<>();
                for (CruncherWorker subWorker : subWorkers) {
                    subWorkerResults.add(subWorker.join());
                }
                for (Map<String, Long> subWorkerResult : subWorkerResults) {
                    for (Map.Entry<String, Long> entry : subWorkerResult.entrySet()) {
                        lastResult.merge(entry.getKey(), entry.getValue(), Long::sum);
                    }
                }
                return lastResult;
            }
            return crunchingResult;
        } catch (OutOfMemoryError e) {
            mainView.stopApp();
        }
        return null;
    }

    private void insertBag(LinkedList<String> words, Map<String, Long> crunchingResult) {
        List<String> sortedWords = (LinkedList<String>) words.clone();
        Collections.sort(sortedWords);
        String bag = String.join(", ", sortedWords);
        if (crunchingResult.containsKey(bag)) {
            crunchingResult.put(bag, crunchingResult.get(bag) + 1L);
        } else {
            crunchingResult.put(bag, 1L);
        }
    }
}
