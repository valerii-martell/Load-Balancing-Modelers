import java.util.*;

/**
 * Created by kirintor830 on 25.12.2017.
 */
public class Program {

    private int id;
    private int workTime;
    private int uRank;
    private int end;
    private Processor processor;
    private Hashtable<Program,Integer> senders;
    private Hashtable<Program,Integer> receivers;

    public int getId() {
        return id;
    }

    public int getWorkTime() {
        return workTime;
    }

    public int getURank() {
        return uRank;
    }

    public Hashtable<Program, Integer> getSenders() {
        return senders;
    }

    public Hashtable<Program, Integer> getReceivers() {
        return receivers;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Processor getProcessor() {
        return processor;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    public Program(int id, int workTime) {
        this.id = id;
        this.workTime = workTime;
        uRank = 0;
        senders = new Hashtable<Program, Integer>();
        receivers = new Hashtable<Program, Integer>();
    }

    //рекурсивно вираховуємо rank up
    public int uRankCalculate() {
        if (!receivers.isEmpty()) {
            int max = 0;
            Set<Program> keys = receivers.keySet();
            Iterator<Program> itr = keys.iterator();
            while (itr.hasNext()) {
                Program receiver = itr.next();
                max = Math.max(max, receiver.uRankCalculate() + receivers.get(receiver));
            }
            if (workTime + max > uRank)    {   uRank = workTime + max;    }
        }
        else {
            if (workTime > uRank)  {   uRank = workTime;  }
        }
        return uRank;
    }
}
