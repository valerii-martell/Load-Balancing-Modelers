using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TokenBus
{
    class Processor
    {
        private int id;

        private string[] ticks;

        private static string[] bus;

        private static int[] token;

        static Processor()
        {
            bus = new string[1000];
            token = new int[1000];
            for (int i = 0; i < 1000; i++)
            {
                bus[i] = " ";
            }
        }

        public Processor(int id)
        {
            this.id = id;
            ticks = new string[1000];
            for (int i = 0; i < 1000; i++)
            {
                ticks[i] = " ";
            }
        }

        public int ID
        {
            get { return id; }
        }

        public string[] Ticks
        {
            get { return ticks; }
        }

        public static string[] Bus
        {
            get { return bus; }
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
                token[i] = i % processorsCount;
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

            if (task.Dependencies.Any())
            {
                foreach (KeyValuePair<Task, int> dependency in task.Dependencies)
                {
                    if (dependency.Key.Processor.ID != this.ID)
                    {
                        int transferFinish = 0;                    
                        int transferLength = task.Dependencies[dependency.Key];
                        //перевіряємо токен
                        for (int i = dependency.Key.Finish; i < token.Length; i++)
                        {
                            //знаходимо перший доступний такт на якому можна захопити токен
                            if (token[i] == dependency.Key.Processor.ID)
                            {
                                //Console.WriteLine("Token may used in tick " + i);
                                bool isFind = false;

                                for (int j = i + 1; j < token.Length; j++)
                                {
                                    //Console.WriteLine("Token in tick " + j + " is " + token[j]);
                                    if (token[j] == -1)
                                    {
                                        //Console.WriteLine("Bus used in this time");
                                        break;
                                    }
                                    else if (token[j] == id)
                                    {
                                        transferLength--;
                                        if (transferLength == 0)
                                        {
                                            transferFinish = j;
                                            isFind = true;
                                            break;
                                        }                                      
                                    }
                                }

                                if (isFind)
                                {
                                    betterStart = Math.Max(betterStart, transferFinish + 1);
                                    break;
                                }
                            }
                        }
                    }
                    else
                    {
                        betterStart = Math.Max(betterStart, dependency.Key.Finish);
                    }   
                }
            }

            //Console.WriteLine("Task m");

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
            if (task.Dependencies.Any())
            {
                task.SortDependenciesByFinishTime();
                foreach (KeyValuePair<Task, int> dependency in task.Dependencies)
                {
                    if (dependency.Key.Processor.ID != this.ID)
                    {
                        //Console.WriteLine("immers transfer from " + dependency.ID + " in proc " + dependency.Processor.id +
                        //    " to task " + task.ID + " in proc " + id);
                        int start = dependency.Key.Finish;
                        int transferDelay = 0;
                        int transferLength = task.Dependencies[dependency.Key];
                        //перевіряємо токен
                        for (int i = dependency.Key.Finish; i < token.Length; i++)
                        {
                            //знаходимо перший доступний такт на якому можна захопити токен
                            if (token[i] == dependency.Key.Processor.ID)
                            {
                                //Console.WriteLine("Token may used in tick " + i);
                                transferDelay = 0;
                                bool isFind = false;
                                for (int j = i + 1; j < token.Length; j++)
                                {
                                    //Console.WriteLine("Token in tick " + j + " is " + token[j]);
                                    if (token[j] == -1)
                                    {
                                        //Console.WriteLine("Bus used in this time");
                                        break;
                                    }
                                    else if (token[j] == id)
                                    {
                                        //Console.WriteLine("Find token in tick " + j);
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
                                    for (int j = i; j < transferDelay + 1; j++)
                                    {
                                        if (j == i) bus[j] = "П" + dependency.Key.Processor.ID + " захоплює маркер для передачi даних до П" + id + " (вiд Т" + dependency.Key.ID + " до Т"+ task.ID+")";
                                        else if (j == transferDelay) bus[j] = "П" + id + " отримує данi вiд П" + dependency.Key.Processor.ID + " (вiд Т" + dependency.Key.ID + " до Т" + task.ID + ") та звiльнює маркер";
                                        else if (token[j]==this.id) bus[j] = "П" + id + " отримує данi вiд П" + dependency.Key.Processor.ID + " (вiд Т" + dependency.Key.ID + " до Т" + task.ID + ")";
                                        //else bus[j] = "Маркер призначений не для цього процесору";
                                        token[j] = -1;
                                    }
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

