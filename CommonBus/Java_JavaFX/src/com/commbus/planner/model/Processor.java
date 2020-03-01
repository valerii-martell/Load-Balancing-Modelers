package com.commbus.planner.model;

import javafx.fxml.FXML;

import java.util.Iterator;
import java.util.Set;

public class Processor extends SystemElement{

    private int id;
    private String[] ticks;
    private Bus bus;
    private static int workTime = 0;

    public Processor(int id, Bus bus) {
        this.id = id;
        //емулюємо підключення до мосту
        this.bus = bus;
        ticks = new String[10000];
        for (int i = 0; i < 10000; i++) {
            ticks[i] = " ";
        }
    }

    @Override
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

    public void immersion(Node node, int startIndex) {
        //резервуємо шину якщо потрібно
        if (!node.getDependencies().isEmpty()){
            Set<Node> keys = node.getDependencies().keySet();
            Iterator<Node> itr = keys.iterator();
            while (itr.hasNext()) {
                Node dependency = itr.next();
                if (this != dependency.getProcessor()) {
                    int transferStart = dependency.getEndTime() + 1;
                    int transferLength = node.getDependencies().get(dependency);
                    int firstAvailableTickInBus = bus.getFirstAvailableTick(transferStart, transferLength);

                    //погружаємо пересилку на шину
                    for (int i = firstAvailableTickInBus; i < firstAvailableTickInBus + transferLength; i++) {
                        bus.getTicks()[i] = Integer.toString(dependency.getId()) + "->" + Integer.toString(node.getId());
                    }
                    startIndex = Math.max(startIndex, firstAvailableTickInBus + transferLength);
                } else {
                    // оскільки процесор може одночасно рахувати і передавати дані, то
                    // можливий варіант що хоч батьківська задача погружена на цей самий процесор,
                    // але її час закінчення навіть більший за час закічення всіх необхідних пересилок
                    // від батьківських задач з інших процесорів
                    startIndex = Math.max(startIndex, dependency.getEndTime() + 1);
                }
            }
        }

        //погружаємо задачу
        for (int i = startIndex; i < startIndex + node.getRank(); i++)
            ticks[i] = Integer.toString(node.getId());

        //позначимо для погруженої задачі час закінчення та процесор
        node.setEndTime(startIndex + node.getRank() - 1);
        node.setProcessor(this);
        if (node.getEndTime() > workTime) workTime = node.getEndTime();
    }

    @Override
    public boolean isUsed() {
        for (int i = 0; i < ticks.length; i++)
            if (ticks[i] != " ")
                return true;
        return false;
    }

    public int getId() {
        return id;
    }

    public static int getWorkTime(){return workTime;}

    @Override
    public String[] getTicks() {
        return ticks;
    }
}
