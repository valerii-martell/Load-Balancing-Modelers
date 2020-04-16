import java.util.*;

/**
 * Created by kirintor830 on 09.12.2017.
 */
public class Task {

    private int id;
    private int rank;
    private Hashtable<Task,Integer> dependencies;
    private Hashtable<Task,Integer> adjectives;
    private int rankUp;
    private int rankDown;
    private int endTime;
    private Processor processor;

    public Task(int id, int rank) {
        this.id = id;
        this.rank = rank;
        rankUp = 0;
        rankDown = 0;
        dependencies = new Hashtable<Task, Integer>();
        adjectives = new Hashtable<Task, Integer>();
    }

    public int computeRankUp() {
        if (!adjectives.isEmpty()) {
            int max = 0;
            Set<Task> keys = adjectives.keySet();
            Iterator<Task> itr = keys.iterator();
            while (itr.hasNext()) {
                Task adjective = itr.next();
                max = Math.max(max, adjective.computeRankUp() + adjectives.get(adjective));
            }
            if (rank + max > rankUp)    {   rankUp = rank + max;    }
        }
        else {
            if (rank > rankUp)  {   rankUp = rank;  }
        }
        return rankUp;
    }

    public int getId() {
        return id;
    }

    public int getRank() {
        return rank;
    }

    public int getRankUp() {
        return rankUp;
    }

    public int getRankDown() {
        return rankDown;
    }

    //сортує список залежностей за збільшенням часу закінчення батьківської вершини
    public void sortDependencies(){
        Hashtable<Task, Integer> buffer = new Hashtable<Task, Integer>();

        Set<Task> keys = dependencies.keySet();
        Task[] Tasks = keys.toArray(new Task[keys.size()]);
        ArrayList<Task> keysArray = new ArrayList<Task>();
        for (int i = 0; i < keys.size(); i++){
            keysArray.add(Tasks[i]);
        }


        Collections.sort(keysArray, new Comparator<Task>(){
            public int compare(Task n1, Task n2){
                if(n1.getEndTime() == n2.getEndTime())
                    return 0;
                return n1.getEndTime() < n2.getEndTime() ? -1 : 1;
            }
        });

        while (!keysArray.isEmpty()){
            buffer.put(keysArray.get(0), dependencies.get(keysArray.get(0)));
            keysArray.remove(0);
        }

        dependencies = buffer;
    }

    public Hashtable<Task, Integer> getDependencies() {
        return dependencies;
    }

    public Hashtable<Task, Integer> getAdjectives() {
        return adjectives;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public Processor getProcessor() {
        return processor;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }
}
