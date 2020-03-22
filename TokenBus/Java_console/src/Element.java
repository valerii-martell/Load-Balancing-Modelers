import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by kirintor830 on 11.01.2018.
 */
public class Element {

    private int id;
    private double performance;
    private String[] ticks;
    private static String[] bus;
    private static int[] token;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    public String[] getTicks() {
        return ticks;
    }

    public static String[] getBus() {
        return bus;
    }

    public static int[] getToken() {
        return token;
    }

    public Element(int id, double performance){
        this.id = id;
        this.performance = performance;
        ticks = new String[1000];
        bus = new String[1000];
        token = new int[1000];
        for (int i = 0; i < 1000; i++){
            bus[i] = " ";
            ticks[i] = "";
        }
    }

    public double getEfficiencyСoefficient(int lastTick){
        double use = 0;
        for (int i = 0; i < lastTick; i++) {
            if (ticks[i] != "") {
                use++;
            }
        }
        return use / lastTick;
    }

    public static void setTokens(int processorsCount) {
        for (int i = 0; i < token.length; i++)
            token[i] = i % processorsCount;
    }

    public Boolean isUsed() {
        for (int i = 0; i < ticks.length; i++)
            if (ticks[i] != " ") return true;
        return false;
    }

    //знаходимо перший доступний тік з якого шина взагалі звільниться
    public int getFirstAvailableTick(Process process) {
        int start = 0;

        if (!process.getParents().isEmpty()) {
            Set<Process> keys = process.getParents().keySet();
            Iterator<Process> itr = keys.iterator();
            while (itr.hasNext()) {
                Process parent = itr.next();
                if (parent.getElement().getId() != this.id) {
                    int transferFinish = parent.getFinish() + 1;
                    int transferLength = process.getParents().get(parent);
                        //перевіряємо токен
                        for (int i = parent.getFinish(); i < token.length; i++) {
                        //знаходимо перший доступний такт на якому можна захопити токен
                            if (token[i] == parent.getElement().getId()) {
                                Boolean isFind = false;
                                for (int j = i + 1; j < token.length; j++) {
                                    if (token[j] == -1) {
                                       break;
                                        } else if (token[j] == id) {
                                            transferLength--;
                                        }
                                        if (transferLength==0) {
                                            transferFinish = j;
                                            isFind = true;
                                            break;
                                        }
                                    }
                                if (isFind) {
                                    start = Math.max(start, transferFinish + 1);
                                    //transferLength--;
                                    break;
                                }
                            }
                        }
                } else {
                    start = Math.max(start, parent.getFinish());
                }
            }
        }

        //вираховуваємо довжину процесу з врахуванням продуктивності процесору
        int realProcessLength = (int)Math.ceil(process.getLength()*performance);

        for (int i = start; i < ticks.length; i++) {
            Boolean isFree = false;
            if (ticks[i] == "") {
                //вільний тік знайдено
                isFree = true;
                //перевіряємо наступні тіки
                for (int j = i + 1; j < i + realProcessLength; j++) {
                    if (ticks[j] != "") {
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

    public void immersion(Process process, int start) {





        if (!process.getParents().isEmpty()) {
            Set<Process> keys = process.getParents().keySet();
            Iterator<Process> itr = keys.iterator();
            while (itr.hasNext()) {
                Process parent = itr.next();
                if (parent.getElement().getId() != this.id) {
                    int transferDelay = 0;
                    int transferLength = process.getParents().get(parent);
                    //перевіряємо токен
                    for (int i = parent.getFinish(); i < token.length; i++) {
                        //знаходимо перший доступний такт на якому можна захопити токен
                        if (token[i] == parent.getElement().getId()) {
                            Boolean isFind = false;
                            for (int j = i + 1; j < token.length; j++) {
                                if (token[j] == -1) {
                                    break;
                                } else if (token[j] == id) {
                                    transferLength--;
                                }
                                if (transferLength==0) {
                                    transferDelay = j;
                                    isFind = true;
                                    break;
                                }
                            }
                                    if (isFind) {
                                        for (int j = i; j < transferDelay + 1; j++) {
                                            if (j == i)
                                                bus[j] = "E"+parent.getElement().getId() + " occupy marker, " + parent.getId() + " -> " + process.getId();
                                            else if (j == transferDelay)
                                                bus[j] = process.getId() + " <- " + parent.getId() + ", E"+this.id +" release marker";
                                            //else bus[j] = The marker moves along the bus@;
                                            token[j] = -1;
                                        }
                                        start = transferDelay+1;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }



        //вираховуваємо довжину процесу з врахуванням продуктивності процесору
        int realProcessLength = (int)Math.ceil(process.getLength()*performance);

        //погружаємо власне задачу
        for (int i = start; i < start + realProcessLength; i++) {
            ticks[i] = Integer.toString(process.getId());
        }

        //позначимо для погруженою задачі час закінчення та процесор
        process.setFinish(start + realProcessLength);
        process.setElement(this);
    }
}