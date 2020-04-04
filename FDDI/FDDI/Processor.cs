using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FDDI
{
    class Processor
    {
        private int id;
        private string[] ticks;
        private static string[] primaryRing;
        private static string[] secondaryRing;
        private static int[] token;

        static Processor()
        {
            primaryRing = new string[1000];
            secondaryRing = new string[1000];
            token = new int[1000];
            for (int i = 0; i < 1000; i++)
            {
                primaryRing[i] = " ";
                secondaryRing[i] = "Not used in normal work mode";
            }
        }

        public Processor(int id)
        {
            this.id = id;
            ticks = new string[1000];
            for (int i = 0; i < 1000; i++)
                ticks[i] = " ";
        }

        public int ID
        {
            get { return id; }
        }

        public string[] Ticks
        {
            get { return ticks; }
        }

        public static string[] PrimaryRing
        {
            get { return primaryRing; }
        }

        public static string[] SecondaryRing
        {
            get { return secondaryRing; }
        }

        public static int[] Token
        {
            get { return token; }
        }

        public double GetEfficiencyСoefficient(int lastTick)
        {
            double use = 0;
            for (int i = 0; i < lastTick; i++)
            {
                if (ticks[i] != " ")
                {
                    use++;
                }
            }
            return use / lastTick;
        }

        public static void SetTokens(int processorsCount)
        {
            for (int i = 0; i < token.Length; i++)
                token[i] = processorsCount - (i % processorsCount) - 1;
        }

        public bool IsUsed()
        {
            for (int i = 0; i < ticks.Length; i++)
                if (ticks[i] != " ") return true;
            return false;
        }

        //знаходимо перший доступний тік з якого шина взагалі звільниться
        public int GetFirstAvailableTick(Task task)
        {
            int betterStart = 0;

            //якщо задача має батьківські задачі
            if (task.Senders.Any())
            {
                foreach (KeyValuePair<Task, int> sender in task.Senders)
                {
                    //якщо батьківська задача погружена не на цей процесор, то необхідно врахувати
                    //затримку внаслідок пересилки даних
                    if (sender.Key.Processor.ID != this.ID)
                    {
                        int transferFinish = 0;
                        int transferLength = task.Senders[sender.Key];
                        //перевіряємо стан токену
                        for (int i = sender.Key.Finish; i < token.Length; i++)
                        {
                            //знаходимо перший доступний такт на якому 
                            //даний процесор може захопити токен
                            if (token[i] == sender.Key.Processor.ID)
                            {
                                //дивимося, скільки разів ще треба буде захопити токен, 
                                //аби закінчити передачу
                                bool isComplete = false;
                                for (int j = i + 1; j < token.Length; j++)
                                {
                                    if (token[j] == -1)
                                    {
                                        break;
                                    }
                                    else if (token[j] == id)
                                    {
                                        //з кожним знаходженням токену зменшуємо довжину повідомлення, 
                                        //яке треба переслати
                                        transferLength--;
                                        if (transferLength == 0)
                                        {
                                            //запам'ятовуємо, на якому такті повідомлення було повністю переслане
                                            transferFinish = j;
                                            isComplete = true;
                                            break;
                                        }
                                    }
                                }

                                //коли пересилку закінчено перевіряємо, чи не був це критичний нащадок
                                if (isComplete)
                                {
                                    betterStart = Math.Max(betterStart, transferFinish + 1);
                                    break;
                                }
                            }
                        }
                    }
                    //інакше, якщо батьківська задача на цьому ж процесорі,
                    //по її закінченню одразу ж може початись поточна задача
                    else
                    {
                        betterStart = Math.Max(betterStart, sender.Key.Finish);
                    }
                }
            }

            //шукаємо достатньо велике вільне вікно в роботі процесора,
            //починаючи з тіку, на якому закінчились всі необхідні пересилки
            for (int i = betterStart; i < ticks.Length; i++)
            {
                bool isFree = false;
                if (ticks[i] == " ")
                {
                    //вільний тік знайдено
                    isFree = true;
                    //перевіряємо наступні тіки
                    for (int j = i + 1; j < i + task.Length; j++)
                    {
                        //вікно замале
                        if (ticks[j] != " ")
                        {
                            isFree = false;
                            break;
                        }
                    }
                }
                //вікно знайдено
                if (isFree)
                {
                    return i;
                }
            }
            return 0;
        }

        public void Immersion(Task task, int betterStart)
        {
            //якщо задача має батьківські задача
            if (task.Senders.Any())
            {
                //сортуємо батьківські задачі в порядку зростання їх часу закінчення
                task.SortSendersByFinishTime();

                foreach (KeyValuePair<Task, int> sender in task.Senders)
                {
                    //якщо батьківська задача погружена не на цей процесор
                    if (sender.Key.Processor.ID != this.ID)
                    {
                        int start = sender.Key.Finish;
                        int transferDelay = 0;
                        int transferLength = task.Senders[sender.Key];
                        //перевіряємо токен
                        for (int i = sender.Key.Finish; i < token.Length; i++)
                        {
                            //знаходимо перший доступний такт на якому можна захопити токен
                            if (token[i] == sender.Key.Processor.ID)
                            {
                                transferDelay = 0;
                                bool isFind = false;
                                for (int j = i + 1; j < token.Length; j++)
                                {
                                    if (token[j] == -1)
                                    {
                                        break;
                                    }
                                    else if (token[j] == id)
                                    {
                                        transferLength--;
                                        if (transferLength == 0)
                                        {
                                            transferDelay = j;
                                            isFind = true;
                                            break;
                                        }
                                    }
                                }

                                if (isFind)
                                {
                                    //імітуємо роботу головного кільця
                                    for (int j = i; j < transferDelay + 1; j++)
                                    {
                                        if (j == i) primaryRing[j] = "The token is captured. From task " + sender.Key.ID + " to task " + task.ID;
                                        else if (j == transferDelay) primaryRing[j] = "All data transfer from task " + sender.Key.ID + " to task " + task.ID + " comleted. The token is released.";
                                        else if (token[j] == this.id) primaryRing[j] = "Part of the data from task " + sender.Key.ID + " to task " + task.ID + " was transferred successfully.";
                                        else primaryRing[j] = "The token is passed to the right processor.";
                                        token[j] = -1;
                                    }
                                    //задача зможе початись з наступного такту
                                    betterStart = transferDelay + 1;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            //погружаємо власне задачу
            for (int i = betterStart; i < betterStart + task.Length; i++)
            {
                ticks[i] = task.ID.ToString();
            }

            //позначимо для погруженою задачі час закінчення та процесор
            task.Finish = betterStart + task.Length;
            task.Processor = this;

        }
    }
}
