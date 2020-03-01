package com.commbus.planner.model;

/**
 * Created by Mr. Santa Claus on 31.12.2017.
 *
 * Created by KirinTor on 20.12.2017.
 */
public class Bus extends SystemElement {

    private String[] ticks;

    public Bus() {
        ticks = new String[10000];
        for (int i = 0; i < 10000; i++) {
            ticks[i] = " ";
        }
    }

    @Override
    public boolean isUsed() {
        for (int i = 0; i < ticks.length; i++)
            if (ticks[i] != " ")
                return true;
        return false;
    }

    @Override
    public int getFirstAvailableTick(int start, int length){
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

    @Override
    public String[] getTicks() {
        return ticks;
    }

}
