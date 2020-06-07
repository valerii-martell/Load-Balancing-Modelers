import java.util.Iterator;
import java.util.Set;

/**
 * Created by kirintor830 on 25.12.2017.
 */
public class Processor {

    private int id;
    private double performance;
    private String[] ticks;

    public int getId() {
        return id;
    }

    public double getPerformance() {
        return performance;
    }

    public String[] getTicks() {
        return ticks;
    }

    public int getWindow(int start, int size) {
        //шукаємо вільне вікно
        for (int i = start; i < ticks.length; i++) {
            Boolean isFree = false;
            if (ticks[i] == " ") {
                //вільний такт знайдено
                isFree = true;
                //перевіряємо наступні size-1 тактів
                for (int j = i + 1; j < i + size; j++) {
                    if (ticks[j] != " ") {
                        isFree = false;
                        break;
                    }
                }
            }
            //вікно знайдено
            if (isFree) return i;
        }
        return 0;

    }

    public void setProgram(Program program, int start) {
        //вираховуваємо довжину програми з врахуванням продуктивності процесору
        int workTime = (int)(program.getWorkTime()*performance) + 1;

        //погружаємо програми
        for (int i = start; i < start + workTime; i++)
            ticks[i] = Integer.toString(program.getId());

        //позначимо для погруженої програми час закінчення та процесор
        program.setEnd(start + workTime - 1);
        program.setProcessor(this);

        //перевіримо чи не ця програма була останньою (тобто визначала час роботи)
        Main.fullTime = Math.max(Main.fullTime, program.getEnd());
    }

    //коефіцієнт ефективності (кількість використовуваних тактів / кількість всіх тактів
    public double getEfficiencyCoefficient(int totalTick) {
        double usedTicks = 0;
        for (int i = 0; i < totalTick; i++) {
            if (ticks[i] != " ") {
                usedTicks++;
            }
        }
        return usedTicks / totalTick;
    }

    public Processor(int id, double performance) {
        this.id = id;
        this.performance = performance;
        ticks = new String[10000];
        for (int i = 0; i < 10000; i++) {
            ticks[i] = " ";
        }
    }

}
