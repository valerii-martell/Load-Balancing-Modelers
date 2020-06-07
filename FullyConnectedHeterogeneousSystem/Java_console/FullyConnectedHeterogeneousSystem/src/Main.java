import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by kirintor830 on 25.12.2017.
 */
public class Main {

    public static int fullTime = 0;

    public static double[] processorsPerformances = new double[]{ 1, 1.3, 1.7, 2.0, 1.5, 1.9 };

    public static int[] programsWorkTimes = new int[]{ 2, 5, 3, 4, 5, 5, 2, 3, 7, 2, 6, 1, 10, 3, 3 };

    public static int[][] incidence = new int[][]{
     /*      0   1   2   3   4   5   6   7   8   9  10  11  12  13  14 */
     /*0*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
     /*1*/{  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
     /*2*/{  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
     /*3*/{  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
     /*4*/{  4,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
     /*5*/{  0,  4,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
     /*6*/{  0,  2,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
     /*7*/{  0,  0,  3,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
     /*8*/{  0,  0,  0,  6,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
     /*9*/{  0,  0,  0,  0,  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
    /*10*/{  0,  0,  0,  0,  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
    /*11*/{  0,  0,  0,  0,  0,  3,  5,  2,  0,  0,  0,  0,  0,  0,  0 },
    /*12*/{  0,  0,  0,  0,  0,  0,  0,  0,  1,  5,  3,  0,  0,  0,  0 },
    /*13*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7,  0,  8,  0,  0 },
    /*14*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8,  0,  7,  0,  0 }};

    public static ArrayList<Processor> processors = new ArrayList<>();
    public static ArrayList<Program> programs = new ArrayList<>();

    public static void main(String[] args) {

        //Створимо програми з відповідними вагами
        for (int i = 0; i < programsWorkTimes.length; i++)
            programs.add(new Program(i, programsWorkTimes[i]));

        //Задамо зв'язки з матриці інцидентності
        for (int i = 0; i < incidence.length; i++) {
            for (int j = 0; j < incidence[i].length; j++) {
                if(incidence[i][j] > 0 ) {
                    programs.get(i).getSenders().put(programs.get(j), incidence[i][j]);
                    programs.get(j).getReceivers().put(programs.get(i), incidence[i][j]);
                }
            }
        }

        //вирахуємо ранги програм
        for (int i = 0; i < programs.size(); i++)
            programs.get(i).uRankCalculate();

        //Відсортуємо список програм, по спаданню критичного шляху вверх
        for (int i = 0; i < programs.size() - 1; i++) {
            for (int j = i + 1; j < programs.size(); j++) {
                if (programs.get(i).getURank() < programs.get(j).getURank()) {
                    Program buffer = programs.get(j);
                    programs.set(j, programs.get(i));
                    programs.set(i, buffer);
                }
            }
        }

        //Зчитуємо параметри процесорів
        for (int i = 0; i < processorsPerformances.length; i++)
            processors.add(new Processor(i, processorsPerformances[i]));

        //Відсортуємо процесори за їх продуктивністю
        for (int i = 0; i < processors.size() - 1; i++){
            for (int j = i + 1; j < processors.size(); j++) {
                if (processors.get(i).getPerformance() > processors.get(j).getPerformance()) {
                    Processor buffer = processors.get(j);
                    processors.set(j ,processors.get(i));
                    processors.set(i, buffer);
                }
            }
        }

        //Поки список програм не порожній
        while (!programs.isEmpty()) {
            //індекс процесору, на якому буде мінімальний час закінчення задачі
            int index = 0;

            //кращий час початку та закінчення
            int betterStart = 0;
            int betterFinish = 0;

            //перевіряємо всі процесори в системі
            for (int i = 0; i < processors.size(); i++) {
                //перевіримо, чи при погрузці на цей процесор в програми будуть наявні батьківські програми,
                //які були погружені на інші процесори
                int start = 0;
                int finish = 0;
                if (!programs.get(0).getSenders().isEmpty()) {
                    Set<Program> keys = programs.get(0).getSenders().keySet();
                    Iterator<Program> itr = keys.iterator();
                    while (itr.hasNext()) {
                        Program sender = itr.next();
                        //якщо батьківська програма була погружена на інший процесор то врахуємо пересилку
                        if (processors.get(i) != sender.getProcessor())
                            start = Math.max(start, sender.getEnd() + programs.get(0).getSenders().get(sender));
                        else //інакше час початку поточної програми може бути часом закінчення батьківської
                            start = Math.max(start, sender.getEnd());
                    }
                }

                //мінімальний такт, з якого може початись виконання задачі на даному процесорі
                start = processors.get(i).getWindow(start,
                        (int)(programs.get(0).getWorkTime()*processors.get(i).getPerformance()) + 1);

                //час закінчення на даному процесорі
                finish = start + (int)(programs.get(0).getWorkTime()*processors.get(i).getPerformance()) + 1;

                //перед перевіркою наступних процесорів, припустимо,
                //що погруження на найшвидший процесор буде найефективнішим
                if (i == 0) {
                    betterFinish = finish;
                    betterStart = start;
                }

                //якщо час закінчення на даному процесорі кращий, ніж на попередніх перевірених
                if (finish < betterFinish) {
                    index = i;
                    betterFinish = finish;
                    betterStart = start;
                }
            }

            //Погружаємо програму на кращий процесор
            processors.get(index).setProgram(programs.get(0), betterStart);

            //Видаляємо програму з пулу
            programs.remove(0);
        }

        //нумеруємо колонки
        System.out.print("    |");
        for (int i = 0; i < processors.size(); i++){
            String str;
            if (processors.get(i).getId() < 10) str = "  "+processors.get(i).getId()+" ";
            else str = ""+processors.get(i).getId()+" ";
            System.out.print(str + "|");
        }
        System.out.println();

        String separator = "-----";
        for (int i = 0; i < processors.size(); i++)
            separator+="-----";

        //заповнюємо рядки
        System.out.println(separator);
        for(int i = 0; i < fullTime + 1; i++) {
            String str;
            if (i < 10) str = "  "+i+" ";
            else if (i<100) str = " "+i+" ";
            else str = i+" ";
            System.out.print(str + "|");

            for (int j = 0; j < processors.size(); j++) {
                str = processors.get(j).getTicks()[i];
                if (str.length() == 1) str = "  "+str+" ";
                else str = " "+str+" ";
                System.out.print(str + "|");
            }
            System.out.println();
        }

        //Очищаємо дані
        processors.clear();;
        programs.clear();
    }
}
