package com.commbus.planner.model;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by KirinTor on 20.12.2017.
 *
 * Клас - примітив вершини графу
 */
public class Node {

    private int id;
    private int rank;
    private Hashtable<Node,Integer> dependencies;
    private Hashtable<Node,Integer> adjectives;
    private int rankUp;
    private int rankDown;
    private int endTime;
    private Processor processor;

    public Node(int id, int rank) {
        this.id = id;
        this.rank = rank;
        rankUp = 0;
        rankDown = 0;
        dependencies = new Hashtable<Node, Integer>();
        adjectives = new Hashtable<Node, Integer>();
    }

    //рекурсивно вираховуємо вагу вершини при "проході вверх"
    public int computeRankUp() {
        if (!adjectives.isEmpty()) {
            int max = 0;
            Set<Node> keys = adjectives.keySet();
            Iterator<Node> itr = keys.iterator();
            while (itr.hasNext()) {
                Node adjective = itr.next();
                max = Math.max(max, adjective.computeRankUp() + adjectives.get(adjective));
            }
            if (rank + max > rankUp)    {   rankUp = rank + max;    }
        }
        else {
            if (rank > rankUp)  {   rankUp = rank;  }
        }
        return rankUp;
    }

    //рекурсивно вираховуємо вагу вершини при "проході вниз"
    public int computeRankDown() {
        if (!dependencies.isEmpty()) {
            Set<Node> keys = dependencies.keySet();
            Iterator<Node> itr = keys.iterator();
            while (itr.hasNext()) {
                Node dependency = itr.next();

                rankDown = Math.max(rankDown, dependency.computeRankUp() + dependencies.get(dependency));
            }
        }
        return rankDown;
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
        Hashtable<Node, Integer> buffer = new Hashtable<Node, Integer>();

        Set<Node> keys = dependencies.keySet();
        Node[] nodes = keys.toArray(new Node[keys.size()]);
        ArrayList<Node> keysArray = new ArrayList<Node>();
        for (int i = 0; i < keys.size(); i++){
            keysArray.add(nodes[i]);
        }


        Collections.sort(keysArray, new Comparator<Node>(){
            public int compare(Node n1, Node n2){
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

    public Hashtable<Node, Integer> getDependencies() {
        return dependencies;
    }

    public Hashtable<Node, Integer> getAdjectives() {
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

    public void Print(){
        System.out.println("---------------");
        System.out.println("Node id" + id);
        System.out.println("Node rank" + rank);
        System.out.println("Node rank up" + rankUp);
        System.out.println("Node rank down" + rankDown);
        Set<Node> keys = dependencies.keySet();
        Iterator<Node> itr = keys.iterator();
        System.out.println("DEPENDENCIES: ");
        while (itr.hasNext()) {
            Node dependency = itr.next();
            System.out.print(dependency.id);
        }
        System.out.println();
        System.out.println("---------------");
    }

}
