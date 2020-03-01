package com.commbus.planner.model;

import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by KirinTor on 20.12.2017.
 *
 * Клас моделює роботу супервізору паралельної обч. системи
 */
public class Supervisor {

    private ArrayList<Node> nodes;
    private ArrayList<Processor> processors;
    private Bus bus;

    public Supervisor(ObservableList<SimpleNodeProperty> nodes, int processorsCount) {
        //спочатку формуємо список вершин без з'єднань
        this.nodes = new ArrayList<Node>();
        this.processors = new ArrayList<Processor>();

        for (int i = 0; i < nodes.size(); i++) {
            this.nodes.add(new Node(
                    Integer.valueOf(nodes.get(i).getId()).intValue(),
                    Integer.valueOf(nodes.get(i).getRank()).intValue()));
        }

        //тепер можна задати зв'язки між вершинами
        for (int i = 0; i < nodes.size(); i++) {
            try{
                String[] dependenciesStr = nodes.get(i).getDependencies().split(" ");
                String[] communicationsLengthsStr = nodes.get(i).getCommunicationsLengths().split(" ");
                for (int j = 0; j < dependenciesStr.length; j++) {
                        int dependency = Integer.valueOf(dependenciesStr[j]).intValue();
                        int communicationLength = Integer.valueOf(communicationsLengthsStr[j]).intValue();
                        this.nodes.get(i).getDependencies().put(this.nodes.get(dependency), communicationLength);
                        this.nodes.get(dependency).getAdjectives().put(this.nodes.get(i), communicationLength);
                }
            } catch (Exception e){};
        }

        //створюємо систему
        //починаючи з шини
        bus = new Bus();

        //створюємо список процесорів, кожен "підключимо" до шини
        for (int i = 0; i < processorsCount; i++) {
            processors.add(new Processor(i, bus));
        }
    }

    public ArrayList<String[]> getPlan(){
        ArrayList<String[]> plan = new ArrayList<String[]>();

        //вирахуємо ранги задач
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).computeRankUp();
            //nodes.get(i).computeRankDown();
        }

        //формуємо пул задач, по спаданню критичного шляху вверх
        for (int i = 0; i < nodes.size() - 1; i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (nodes.get(i).getRankUp() < nodes.get(j).getRankUp()) {
                    Node buffer = nodes.get(j);
                    nodes.set(j, nodes.get(i));
                    nodes.set(i, buffer);
                }
            }
        }

        /*
        for (int i = 0; i < nodes.size(); i++){
            nodes.get(i).Print();
        }*/

        //Поки пул задач не порожній
        while (!nodes.isEmpty()) {

            //мінімальний час закінчення задачі
            //int minimumFinish = 0;

            //індекс процесору, на якому буде мінімальний час закінчення задачі
            int betterProcessorIndex = 0;

            //час, з якого повинна буде початись остаточна погрузка
            int betterStart = 0;

            //відсортуємо залежності по часу їх завершення
            nodes.get(0).sortDependencies();

            //перевіряємо всі процесори в системі
            for (int i = 0; i < processors.size(); i++) {
                //перевіримо, чи при погрузці на цей процесор в задачі
                //будуть наявні батьківські задачі,
                //які були погружені на інші процесори
                int transferLength = 0;
                int start = 0;
                if (!nodes.get(0).getDependencies().isEmpty()) {
                    Set<Node> keys = nodes.get(0).getDependencies().keySet();
                    Iterator<Node> itr = keys.iterator();
                    while (itr.hasNext()) {
                        Node dependency = itr.next();
                        //якщо батьківська задача була погружена на інший процесор
                        if (processors.get(i) != dependency.getProcessor()) {
                            int transferStart = dependency.getEndTime() + 1;
                            transferLength = nodes.get(0).getDependencies().get(dependency);

                            //ми шукаємо яка пересилка стане останньою (критичною)
                            start = Math.max(start, bus.getFirstAvailableTick(transferStart, transferLength) + transferLength);
                        } else {
                            //якщо ж батьківська задача на цьому ж процесорі, то поточна може стартувати одразу після неї
                            //якщо пересилки від інших задач закінчились раніше чи їх взагалі не було
                            start = Math.max(start, dependency.getEndTime() + 1);
                        }
                    }
                }

                //Стратегія "включення" (пошуку вільних вікон)
                start = processors.get(i).getFirstAvailableTick(start, nodes.get(0).getRank());

                //Без стратегії "включення"
                //start = Math.max(start, processors.get(i).getPointer());

                //час закінчення задачі на даному процесорі
                //int thisFinish = start + nodes.get(0).getRank();

                //перед перевіркою наступних процесорів, припустимо,
                //що погруження на найшвидший процесор буде найефективнішим
                if (i == 0)
                {
                    //minimumFinish = thisFinish;
                    betterStart = start;
                }

                //якщо час початку на даному процесорі кращий, ніж на попередніх перевірених то вважатимемо його найефективнішим
                if (start < betterStart)
                {
                    //minimumFinish = thisFinish;
                    betterProcessorIndex = i;
                    betterStart = start;
                }
            }
            //Погружаємо задачу на кращий процесор
            processors.get(betterProcessorIndex).immersion(nodes.get(0), betterStart);

            //Виключаємо задачу з пулу
            nodes.remove(0);
        }

        //заповнюємо остаточний план
        for (int i = 0; i < processors.size(); i++){
            //процесори, які весь час простоювали, в плані не відображаються
            if (processors.get(i).isUsed()){
                plan.add(processors.get(i).getTicks());
            }
        }
        //доповнюємо його шиною
        plan.add(bus.getTicks());

        return plan;
    }

    //повертає остаточний час роботи програми (в тіках)
    public static int getExecuteTime() {
        return Processor.getWorkTime();
    }

}
