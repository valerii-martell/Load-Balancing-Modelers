import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Main {

    private static ArrayList<Task> tasks = new ArrayList<Task>();
    private static ArrayList<Processor> processors = new ArrayList<Processor>();
    private static int lastIndex = 0;

    private static double[] performances = new double[] { 1.0, 1.3, 1.5};

    private static int[] tasksRanks = new int[] { 4, 3, 2, 5,  2, 3, 6,  1, 3, 4,  8, 5, 3,  2, 7, 3,  3 };

    private static int[][] dependencies = new int[][] {
            /*      0   1   2   3   4   5   6   7   8   9  10  11  12  13  14 15 16 */
            /*0*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*1*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*2*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*3*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*4*/{  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*5*/{  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*6*/{  0,  4,  5,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*7*/{  0,  0,  0,  0,  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*8*/{  0,  0,  0,  0,  1,  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*9*/{  0,  0,  0,  0,  0,  6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*10*/{  0,  0,  0,  0,  0,  5,  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*11*/{  0,  0,  0,  0,  0,  0,  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*12*/{  0,  0,  0,  0,  0,  0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*13*/{  0,  0,  0,  0,  0,  0,  0,  3,  2,  4,  0,  0,  0,  0,  0,  0,  0 },
           /*14*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  5,  3,  0,  0,  0,  0,  0 },
           /*15*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  6,  4,  0,  0,  0,  0 },
           /*16*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  3,  2,  3,  0 }};

    public static void main(String[] args) {

        Bridge bridge = new Bridge();
        
        for (int i = 0; i < performances.length; i++)
            processors.add(new Processor(i, performances[i], bridge));

        for (int i = 0; i < tasksRanks.length; i++)
            tasks.add(new Task(i, tasksRanks[i]));

        for (int i = 0; i < dependencies.length; i++) {
            for (int j = 0; j < dependencies[i].length; j++) {
                if(dependencies[i][j] != 0 ) {
                    tasks.get(i).getDependencies().put(tasks.get(j), dependencies[i][j]);
                    tasks.get(j).getAdjectives().put(tasks.get(i), dependencies[i][j]);
                }
            }
        }

        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).computeRankUp();
        }

        for (int i = 0; i < tasks.size() - 1; i++) {
            for (int j = i + 1; j < tasks.size(); j++) {
                if (tasks.get(i).getRankUp() < tasks.get(j).getRankUp()) {
                    Task buffer = tasks.get(j);
                    tasks.set(j, tasks.get(i));
                    tasks.set(i, buffer);
                }
            }
        }

        for (int i = 0; i < processors.size() - 1; i++){
            for (int j = i + 1; j < processors.size(); j++) {
                if (processors.get(i).getPerformance() > processors.get(j).getPerformance()) {
                    Processor buffer = processors.get(j);
                    processors.set(j ,processors.get(i));
                    processors.set(i, buffer);
                }
            }
        }
        

        /*
        for (int i = 0; i < tasks.size(); i++){
            tasks.get(i).Print();
        }*/

        while (!tasks.isEmpty()) {

            int minimumFinish = 0;
            int betterProcessorIndex = 0;
            int betterStart = 0;
            tasks.get(0).sortDependencies();

            for (int i = 0; i < processors.size(); i++) {
                int transferLength = 0;
                int start = 0;
                if (!tasks.get(0).getDependencies().isEmpty()) {
                    Set<Task> keys = tasks.get(0).getDependencies().keySet();
                    Iterator<Task> itr = keys.iterator();
                    while (itr.hasNext()) {
                        Task dependency = itr.next();
                        if (processors.get(i) != dependency.getProcessor()) {
                            int transferStart = dependency.getEndTime() + 1;
                            transferLength = tasks.get(0).getDependencies().get(dependency);
                            start = Math.max(start, bridge.getFirstAvailableTick(transferStart, transferLength) + transferLength);
                        } else {
                            start = Math.max(start, dependency.getEndTime() + 1);
                        }
                    }
                }

                int taskLength = (int)Math.ceil(tasks.get(0).getRank()*processors.get(i).getPerformance());

                start = processors.get(i).getFirstAvailableTick(start, taskLength);

                int thisFinish = start + taskLength;

                if (i == 0)
                {
                    minimumFinish = thisFinish;
                    betterStart = start;
                }

                if (thisFinish < minimumFinish)
                {
                    minimumFinish = thisFinish;
                    betterProcessorIndex = i;
                    betterStart = start;
                }
            }
            processors.get(betterProcessorIndex).immersion(tasks.get(0), betterStart);
            lastIndex = Math.max(lastIndex, tasks.get(0).getEndTime());
            tasks.remove(0);
        }

        System.out.print("    |");
        for (int i = 0; i < processors.size(); i++){
            String str;
            if (processors.get(i).getId() < 10) str = " П"+processors.get(i).getId()+" ";
            else str = "П"+processors.get(i).getId()+"";
            System.out.print(str + "|");
        }
        System.out.println("\t МІСТ\t");

        String separator = "-----";
        for (int i = 0; i < processors.size() + 14; i++)
           separator+="-----";
        //System.out.println(lastIndex);

        System.out.println(separator);
        for(int i = 0; i < lastIndex + 1; i++) {
            String str;
            if (i < 10) str = "  "+i+" ";
            else if (i<100) str = " "+i+" ";
            else str = i+" ";
            System.out.print(str + "|");

            for (int j = 0; j < processors.size(); j++) {
                str = processors.get(j).getTicks()[i];
                if (str.length() == 1) str = " "+str+"  ";
                else if (str.length() == 2) str = " "+str+" ";
                else if (str.length() == 3) str = " "+str+"";

                if ((str.contains("M"))&&(str.length()!=1)){
                    System.out.print(str + "|");
                }
                else if (str.length()!=0){
                    System.out.print(str + "|");
                }
                else if (str.length()==0){
                    System.out.print(str + "    |");
                }
            }

            if (bridge.getTicks()[i] != " "){
                System.out.print("\t" + bridge.getTicks()[i]);
            }
            System.out.println();
        }
    }
}
