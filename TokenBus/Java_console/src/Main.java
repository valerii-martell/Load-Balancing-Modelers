import java.util.ArrayList;

public class Main {

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String VERTICAL_SEPARATOR = "\u001B[40m" + "|" + "\u001B[0m";

    private static ArrayList<Process> processes = new ArrayList<Process>();
    private static ArrayList<Element> elements = new ArrayList<Element>();
    private static int lastIndex = 0;

    // розмірність масиву - кількість елементів системи, кожне значення - продуктивність
    // відповідного елементу, раціональне число не менше за 1.0
    private static double[] elementsPerformances = new double[] { 1.0, 1.7, 1.9};

    //розмірність масиву - кількість вершин, кожен елемент - вага відповідної вершини, > 0
    private static int[] processesLengths = new int[] { 2, 3, 4,  2, 5, 3,  2, 1, 2,  6, 5, 6,  3, 6,  2, 7, 1 };

    //матриця зв'язності
    private static int[][] processesDependencies = new int[][] {
            /*      0   1   2   3   4   5   6   7   8   9  10  11  12  13  14 15 16 */
            /*0*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*1*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*2*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*3*/{  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*4*/{  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*5*/{  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*6*/{  0,  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*7*/{  0,  2,  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*8*/{  0,  0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*9*/{  0,  0,  0,  2,  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*10*/{  0,  0,  0,  0,  1,  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*11*/{  0,  0,  0,  0,  0,  0,  2,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*12*/{  0,  0,  0,  0,  0,  0,  0,  4,  3,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*13*/{  0,  0,  0,  0,  0,  0,  0,  0,  2,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*14*/{  0,  0,  0,  0,  0,  0,  3,  0,  0,  1,  2,  0,  0,  0,  0,  0,  0 },
           /*15*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  3,  2,  0,  0,  0 },
           /*16*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  3,  0,  0,  1,  2,  0 }};


    public static void main(String[] args) {
        //Створимо відповідну кількість елементів
        for (int i = 0; i < elementsPerformances.length; i++)
            elements.add(new Element(i, elementsPerformances[i]));

        //Роздамо маркери
        Element.setTokens(elementsPerformances.length);

        //Створимо задачі з відповідними вагами
        for (int i = 0; i < processesLengths.length; i++)
            processes.add(new Process(i, processesLengths[i]));

        //Задамо зв'язки з матриці інцидентності
        for (int i = 0; i < processesDependencies.length; i++) {
            for (int j = 0; j < processesDependencies[i].length; j++) {
                if(processesDependencies[i][j] != 0 ) {
                    processes.get(i).getParents().put(processes.get(j), processesDependencies[i][j]);
                    processes.get(j).getChildren().put(processes.get(i), processesDependencies[i][j]);
                }
            }
        }

        //вирахуємо ранги задач
        for (int i = 0; i < processes.size(); i++) {
            processes.get(i).rankUp();
        }

        //формуємо пул задач, по спаданню критичного шляху вверх
        for (int i = 0; i < processes.size() - 1; i++) {
            for (int j = i + 1; j < processes.size(); j++) {
                if (processes.get(i).getRank() < processes.get(j).getRank()) {
                    Process buffer = processes.get(j);
                    processes.set(j, processes.get(i));
                    processes.set(i, buffer);
                }
            }
        }

        //Відсортуємо процесори за їх продуктивністю
        for (int i = 0; i < elements.size() - 1; i++){
            for (int j = i + 1; j < elements.size(); j++) {
                if (elements.get(i).getPerformance() > elements.get(j).getPerformance()) {
                    Element buffer = elements.get(j);
                    elements.set(j ,elements.get(i));
                    elements.set(i, buffer);
                }
            }
        }

        //for (int i = 0; i < processes.size(); i++){
        //    processes.get(i).print();
        //}

        //Поки пул задач не порожній
        while (!processes.isEmpty())
        {
            // індекс процесору, на якому буде мінімальний час закінчення задачі
            int indexCPU = 0;

            // час початку
            int betterStartTick = 0;

            //час кінця
            int betterFinishTick = 0;

            //processes.get(0).sortParents();

            //перевіряємо всі процесори в системі
            for (int i = 0; i < elements.size(); i++)
            {
                int currentStart = elements.get(i).getFirstAvailableTick(processes.get(0));
                int currentFinish = currentStart + (int)Math.ceil(processes.get(0).getLength()*elements.get(i).getPerformance());
                //перед перевіркою наступних процесорів, припустимо,
                //що погруження на найшвидший процесор буде найефективнішим
                if (i == 0) {
                    betterStartTick = currentStart;
                    betterFinishTick = currentFinish;
                }

                //якщо час закінчення на даному процесорі кращий, ніж на попередніх перевірених
                if (currentFinish < betterFinishTick) {
                    indexCPU = i;
                    betterStartTick = currentStart;
                    betterFinishTick = currentFinish;
                }
            }

            //Погружаємо задачу на кращий процесор
            elements.get(indexCPU).immersion(processes.get(0), betterStartTick);

            //Перед видаленням перевіримо чи ця задача мала найпізніший час закінення
            lastIndex = Math.max(lastIndex, processes.get(0).getFinish());

            //Видаляємо задачу з пулу
            processes.remove(0);
        }

        Element.setTokens(elementsPerformances.length);

        //нумеруємо колонки
        System.out.print("    "+VERTICAL_SEPARATOR);
        for (int i = 0; i < elements.size(); i++){
            String str;
            if (elements.get(i).getId() < 10) str = " E"+elements.get(i).getId()+" ";
            else str = "E"+elements.get(i).getId()+"";
            System.out.print(ANSI_YELLOW_BACKGROUND + str + VERTICAL_SEPARATOR + ANSI_RESET);
        }
        System.out.println("\tBUS\t");

        //String separator = "-----";
        //for (int i = 0; i < elements.size(); i++)
        //   separator+="-----";

        //заповнюємо рядки
        //System.out.println(separator);
        for(int i = 0; i < lastIndex; i++) {
            String str;
            if (i < 10) str = "  "+i+" ";
            else if (i<100) str = " "+i+" ";
            else str = i+" ";
            System.out.print(str + VERTICAL_SEPARATOR);

            for (int j = 0; j < elements.size(); j++) {
                str = elements.get(j).getTicks()[i];
                if (Element.getToken()[i] == elements.get(j).getId()) str+="M";
                if (str.length() == 1) str = " "+str+"  ";
                else if (str.length() == 2) str = " "+str+" ";
                else if (str.length() == 3) str = " "+str+"";

                if ((str.contains("M"))&&(str.length()!=1)){
                    System.out.print(ANSI_CYAN_BACKGROUND + str + ANSI_RESET + VERTICAL_SEPARATOR);
                }
                else if (str.length()!=0){
                    System.out.print(ANSI_GREEN_BACKGROUND + str + ANSI_RESET + VERTICAL_SEPARATOR);
                }
                else if (str.length()==0){
                    System.out.print(str + "    "+VERTICAL_SEPARATOR);
                }
            }

            if (Element.getBus()[i] != " "){
                System.out.print("\t" + Element.getBus()[i]);
            }

            System.out.println();
        }

        //Очищаємо дані
        elements.clear();
        processes.clear();
    }
}
