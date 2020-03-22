import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by kirintor830 on 11.01.2018.
 */
public class Process {

    public int getRank() {
        return rank;
    }

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Hashtable<Process,Integer> getParents() {
        return parents;
    }

    public Hashtable<Process,Integer> getChildren() {
        return children;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getFinish() {
        return finish;
    }

    public void setFinish(int finish) {
        this.finish = finish;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    private Hashtable<Process,Integer> parents;
    private Hashtable<Process,Integer> children;
    private int length;
    private int rank;
    private int finish;
    private Element element;

    public Process(int id, int length) {
        this.id = id;
        this.length = length;
        parents = new Hashtable<Process,Integer>();
        children = new Hashtable<Process,Integer>();
    }

    public int rankUp() {
        if (!children.isEmpty()) {
            int max = 0;
            Set<Process> keys = children.keySet();
            Iterator<Process> itr = keys.iterator();
            while (itr.hasNext()) {
                Process child = itr.next();
                max = Math.max(max, child.rankUp() + children.get(child));
            }
            if (length + max > rank)    {   rank = length + max;    }
        }
        else {
            if (length > rank)  {   rank = length;  }
        }
        return rank;
    }

    public void sortParents() {
        Hashtable<Process, Integer> sortedParents = new Hashtable<>();
        Set<Process> keys = parents.keySet();
        ArrayList<Process> keysarray = new ArrayList<>(Collections.list(parents.keys()));
        for (int i = 0; i < keys.size() - 1; i++) {
            for (int j = i + 1; j < keys.size(); j++) {
                if (keysarray.get(i).getFinish() > keysarray.get(j).getFinish()) {
                    Process buffer = keysarray.get(j);
                    keysarray.set(j, keysarray.get(i));
                    keysarray.set(i, buffer);
                }
            }
        }
        for (int i = 0; i < keysarray.size(); i++){
            sortedParents.put(keysarray.get(i), parents.get(keysarray.get(i)));
        }
        parents = sortedParents;
    }

    public void print(){
        System.out.println("---------------");
        System.out.println("Node id " + id);
        System.out.println("Node length " + length);
        System.out.println("Node rank up " + rank);
        Set<Process> keys = parents.keySet();
        Iterator<Process> itr = keys.iterator();
        System.out.println("DEPENDENCIES: ");
        while (itr.hasNext()) {
            Process dependency = itr.next();
            System.out.print(dependency.id);
        }
        System.out.println();
        System.out.println("---------------");
    }

}
