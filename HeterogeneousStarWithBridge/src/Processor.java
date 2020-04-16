import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


public class Processor{

    private int id;
    private double performance;
    private String[] ticks;
    private Bridge bridge;
    private static int workTime = 0;

    public Processor(int id, double performance, Bridge bridge) {
        this.id = id;
        this.performance = performance;
        //емулюємо підключення до мосту
        this.bridge = bridge;
        ticks = new String[10000];
        for (int i = 0; i < 10000; i++) {
            ticks[i] = " ";
        }
    }

    public int getFirstAvailableTick(int start, int length) {
        //шукаємо вільне вікно
        for (int i = start; i < ticks.length; i++) {
            Boolean isFree = false;
            if (ticks[i] == " ") {
                //вільний тік знайдено
                isFree = true;
                //перевіряємо наступні тіки
                for (int j = i + 1; j < i + length; j++) {
                    if (ticks[j] != " ") {
                        isFree = false;
                        break;
                    }
                }
            }
            //вікно знайдено
            if (isFree) {
                return i;
            }
        }
        return 0;
    }

    public void immersion(Task task, int startIndex) {
        if (!task.getDependencies().isEmpty()){
            Set<Task> keys = task.getDependencies().keySet();
            Iterator<Task> itr = keys.iterator();
            while (itr.hasNext()) {
                Task dependency = itr.next();
                if (this != dependency.getProcessor()) {
                    int transferStart = dependency.getEndTime() + 1;
                    int transferLength = task.getDependencies().get(dependency);
                    int firstAvailableTickInBridge = bridge.getFirstAvailableTick(transferStart, transferLength);
                    for (int i = firstAvailableTickInBridge; i < firstAvailableTickInBridge + transferLength; i++) {
                        if(i == firstAvailableTickInBridge) {
                            bridge.getTicks()[i] = " Комутація П" + Integer.toString(dependency.getProcessor().getId()) + " та П" + Integer.toString(this.id)+ ". ";
                        }
                        bridge.getTicks()[i] += "Передача від Т" + Integer.toString(dependency.getId()) + " до Т" + Integer.toString(task.getId());
                        if(i == firstAvailableTickInBridge + transferLength - 1) {
                            bridge.getTicks()[i] += ". Комутацію завершено.";
                        }
                    }
                    startIndex = Math.max(startIndex, firstAvailableTickInBridge + transferLength);
                } else {
                    startIndex = Math.max(startIndex, dependency.getEndTime() + 1);
                }
            }
        }

        int taskLength = (int)Math.ceil(task.getRank()*performance);

        for (int i = startIndex; i < startIndex + taskLength; i++)
            ticks[i] = Integer.toString(task.getId());

        task.setEndTime(startIndex + taskLength - 1);
        task.setProcessor(this);
        if (task.getEndTime() > workTime) workTime = task.getEndTime();
    }

    public boolean isUsed() {
        for (int i = 0; i < ticks.length; i++)
            if (ticks[i] != " ")
                return true;
        return false;
    }

    public int getId() {
        return id;
    }

    public double getPerformance() {
        return performance;
    }

    public static int getWorkTime(){return workTime;}

    public String[] getTicks() {
        return ticks;
    }
}

