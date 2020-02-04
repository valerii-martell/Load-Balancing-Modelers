using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CommBus
{
    class Processor
    {
        private string[] ticks;

        private int currettLastTick;

        private int id;

        public Processor(int id)
        {
            this.id = id;
            ticks = new string[1000];
            for (int i = 0; i < 1000; i++)
            {
                ticks[i] = " ";
            }
            this.currettLastTick = 0;
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

        //коефіцієнт загруженості 
        //відношення робочих тіків до загальної кількості тіків системи
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

        public int GetFirstAvailableTick(int firstAvailableTick, int length)
        {
            int startIndex = Math.Max(currettLastTick, firstAvailableTick);
            //шукаємо вільне вікно
            for (int i = firstAvailableTick; i < currettLastTick; i++)
            {
                bool isFree = false;
                if (ticks[i] == " ")
                {
                    //вільний тік знайдено
                    isFree = true;
                    //перевіряємо наступні тіки
                    for (int j = i + 1; j < i + length; j++)
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
                    startIndex = i;
                    break;
                }
            }
            return startIndex;
        }

        public void Immersion(Task task, int betterStart)
        {
            int start = betterStart;

            //спершу погружаємо пересилки даних на процесори,
            //попутно перевіряючи шину на зайнятість та в разі 
            //необхідності відкладаючи пересилку
            foreach (KeyValuePair<Task, int> sender in task.Senders)
            {
                if (sender.Key.Processor != this)
                {
                    //довжина пересилки 
                    int transferLength = task.Senders[sender.Key];

                    //погружаємо цю пересилку на шину                    
                    int startInBus = Program.bus.GetFirstAvailableTick(start, transferLength);
                    for (int i = startInBus; i < startInBus + transferLength; i++)
                    {
                        //на шині відображаємо ID процесорів-учасників пересилки
                        //"від-до"
                        Program.bus.Ticks[i] = sender.Key.Processor.ID + " - " + this.id;
                    }
                    
                    //погрузимо пересилки на самі процесори
                    for (int i = startInBus; i < startInBus + transferLength; i++)
                    {
                        //на процесорах відображаємо задачі між якими йде обмін даними
                        //приймач "до-від"
                        ticks[i] = task.ID + "<-" + sender.Key.ID;
                        //відправник "від-до"
                        sender.Key.Processor.Ticks[i] = sender.Key.ID + "->" + task.ID;
                    }

                    //якщо це не була вставка в вільне "вікно", то
                    //зсунемо вказівники перших доступних тіків на процесорах-учасниках пересилки
                    //і шині на довжину цієї пересилки
                    if (startInBus + transferLength > Program.bus.CurrentLastTick)
                    {
                        Program.bus.CurrentLastTick = startInBus + transferLength;
                    }
                    if (start + transferLength > sender.Key.Processor.CurrentLastTick)
                    {
                        sender.Key.Processor.CurrentLastTick = start + transferLength;
                    }
                    if (start + transferLength > CurrentLastTick)
                    {
                        CurrentLastTick = start + transferLength;
                    }

                    //початок наступної пересилки буде кінцем поточної пересилки
                    start = startInBus + transferLength;
                }
            }

            //погружаємо власне задачу
            for (int i = start; i < start + task.Length; i++)
            {
                ticks[i] = task.ID.ToString();
            }

            if (start + task.Length > currettLastTick)
            {
                currettLastTick = start + task.Length;
            }

            task.Processor = this;
        }

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
}
