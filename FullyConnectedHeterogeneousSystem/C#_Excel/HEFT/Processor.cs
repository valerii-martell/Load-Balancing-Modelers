using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HEFT
{
    class Processor
    {
        private double performance;

        private string[] ticks;

        private string[] networkCardTicks;

        private int currettLastTick;

        private int id;

        public Processor(int id, double performance)
        {
            this.id = id;
            this.performance = performance;
            ticks = new string[1000];
            networkCardTicks = new string[1000];
            for (int i = 0; i < 1000; i++)
            {
                ticks[i] = " ";
                networkCardTicks[i] = " ";
            }
            this.currettLastTick = 0;
        }

        public double Performance
        {
            get { return performance; }
        }

        public int CurrentLastTick
        {
            get { return currettLastTick; }
            set { this.currettLastTick = value; }
        }

        public int ID
        {
            get { return id; }
        }

        public string[] Ticks
        {
            get { return ticks; }
        }

        public string[] NetworkCardTicks
        {
            get { return networkCardTicks; }
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

        public double GetNetworkCardEfficiencyСoefficient(int lastTick)
        {
            double use = 0;
            for (int i = 0; i < lastTick; i++)
            {
                if (networkCardTicks[i] != " ")
                {
                    use++;
                }
            }
            return use / lastTick;
        }

        public int GetFirstAvailableTick(int startTick, int length)
        {
            //довжина задачі з врахуванням продуктивності процесору
            int realLength = Convert.ToInt32(Math.Ceiling(length * this.performance));

            //шукаємо вільне вікно
            for (int i = startTick; i < ticks.Length; i++)
            {
                bool isFree = false;
                if (ticks[i] == " ")
                {
                    //вільний тік знайдено
                    isFree = true;
                    //перевіряємо наступні тіки
                    for (int j = i + 1; j < i + realLength; j++)
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
            int realTaskLength = Convert.ToInt32(Math.Ceiling(task.Length * performance));

            //спершу погружаємо пересилки даних на відповідні мережеві карти
            foreach (KeyValuePair<Task, int> sender in task.Senders)
            {
                if (sender.Key.Processor != this)
                {
                    //довжина пересилки
                    int transferLength = sender.Value;

                    for (int i = sender.Key.FinishTick + 1; i < sender.Key.FinishTick + 1 + sender.Value; i++)
                    {
                        //відображаємо на мережевій карті поточного процесору
                        if (networkCardTicks[i] == " ")
                        {
                            networkCardTicks[i] = task.ID + " FROM " + sender.Key.ID;
                        }
                        else
                        {
                            networkCardTicks[i] += ", " + task.ID + " FROM " + sender.Key.ID;
                        }

                        //відображаємо на мережевій карті відправника
                        if (sender.Key.Processor.NetworkCardTicks[i] == " ")
                        {
                            sender.Key.Processor.NetworkCardTicks[i] = sender.Key.ID + " TO " + task.ID;
                        }
                        else
                        {
                            sender.Key.Processor.NetworkCardTicks[i] += ", " + sender.Key.ID + " TO " + task.ID;
                        }
                    }
                }
            }

            //погружаємо власне задачу
            for (int i = betterStart; i < betterStart + realTaskLength; i++)
            {
                ticks[i] = task.ID.ToString();
            }

            //якщо погруження відбулось не в вікно, то зсуваємо вказівник останнього тіку
            if (betterStart + realTaskLength > currettLastTick)
            {
                currettLastTick = betterStart + realTaskLength;
            }

            //позначимо для погруженою задачі час закінчення та процесор
            task.FinishTick = betterStart + realTaskLength - 1;
            task.Processor = this;
        }
    }

                /*public int GetFreeWindowIndex(int start, int taskLength)
                {
                    int startIndex = currettLastTick;
                    //Console.WriteLine("Maybe its "+ currettLastTick);
                    for (int i = start; i < 1000; i++)
                    {
                        bool isFree = false;
                        //Console.WriteLine("Check tick "+i);
                        if (ticks[i] == -1)
                        {
                            isFree = true;
                            //Console.WriteLine("tick "+i+" free");
                            //Console.WriteLine("Check ticks from "+ (i+1) +" to " + (i + Convert.ToInt32(Math.Ceiling(taskLength * performance))));
                            for (int j = i+1; j < i + Convert.ToInt32(Math.Ceiling(taskLength * performance)); j++)
                            {
                                //Console.WriteLine(j);
                                if (ticks[j] != -1)
                                {
                                    //Console.WriteLine("tick "+j+"not free");
                                    isFree = false;
                                    break;
                                }
                            }
                        }

                        if (isFree)
                        {
                            //Console.WriteLine("window start from tick: "+i);
                            startIndex = i;
                            break;
                        }
                    }
                    //Console.WriteLine("=====Free window in processor " + performance + " is " + startIndex);
                    return startIndex;
                }*/

                /*public void Print()
                {
                    Console.Write("\nProcessor ID: "+ id);
                    Console.Write("\nProcessor performance: " + performance);
                    Console.Write("\nProcessor tisks : ");
                    for (int i = 0; i < currettLastTick; i++)
                    {
                        if (i>=10)
                        {
                            Console.Write(i + " ");
                        }
                        else
                        {
                            Console.Write(" " + i + " ");
                        }

                    }
                    Console.Write("\nProcessor tasks : ");
                    for (int i=0; i<currettLastTick; i++)
                    {
                        if (Int32.Parse(ticks[i]) >= 10)
                        {
                            Console.Write(ticks[i] + " ");
                        }
                        else if (Int32.Parse(ticks[i]) >= 0)
                        {
                            Console.Write(" " + ticks[i] + " ");
                        }
                        else
                        {
                            Console.Write(ticks[i] + " ");
                        }
                    }
                }*/
}
