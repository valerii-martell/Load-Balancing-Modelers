
public class Bridge {

    private String[] ticks;

    public Bridge() {
        ticks = new String[10000];
        for (int i = 0; i < 10000; i++) {
            ticks[i] = " ";
        }
    }

    public boolean isUsed() {
        for (int i = 0; i < ticks.length; i++)
            if (ticks[i] != " ")
                return true;
        return false;
    }

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

    public String[] getTicks() {
        return ticks;
    }
}
