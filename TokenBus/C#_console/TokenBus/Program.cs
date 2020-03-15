using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TokenBus
{
    static class Program
    {
        public static string StringBuild(this string str)
        {
            if (str.Length == 0)
                return "    ";
            else if (str.Length == 1)
                return "  " + str + " ";
            else if (str.Length == 2)
                return " " + str + " ";
            else
                return str + " ";
        }

        public static string StringBuild(this int i)
        {
            if (i < 10)
                return "  " + i + " ";
            else if (i < 100)
                return " " + i + " ";
            else
                return "" + i + " ";
        }

        private static List<Task> tasks = new List<Task>();
        private static List<Processor> processors = new List<Processor>();
        private static int lastIndex = 0;

        //кількість процесорів
        private static int processorsCount = 3;

        //розмірність масиву - кількість вершин, кожен елемент - вага відповідної вершини, > 0
        private static int[] graphNodesWeights = new int[] { 2, 1, 4, 5,  3, 4, 6, 4, 3,  3, 8, 2, 7,  6, 3, 2 };

        //матриця зв'язності
        private static int[,] graphCommunications = new int[,] {
            /*      0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15 */
            /*0*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*1*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*2*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*3*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*4*/{  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*5*/{  3,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                     
            /*6*/{  0,  1,  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*7*/{  0,  0,  2,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*8*/{  0,  0,  0,  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*9*/{  0,  0,  0,  0,  2,  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*10*/{  0,  0,  0,  0,  0,  1,  3,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*11*/{  0,  0,  0,  0,  0,  0,  2,  1,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*12*/{  0,  0,  0,  0,  0,  0,  0,  2,  1,  0,  0,  0,  0,  0,  0,  0 },
           /*13*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  1,  0,  0,  0,  0,  0 },
           /*14*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  2,  0,  0,  0 },
           /*15*/{  0,  0,  0,  0,  0,  0,  1,  0,  0,  0,  0,  0,  0,  2,  3,  0 }};
        static void Main(string[] args)
        {
            //Створимо відповідну кількість процесорів
            for (int i = 0; i < processorsCount; i++)
                processors.Add(new Processor(i));

            Processor.SetTokens(processorsCount);

            //Створимо задачі з відповідними вагами
            for (int i = 0; i < graphNodesWeights.Length; i++)
                tasks.Add(new Task(i, graphNodesWeights[i]));

            //Задамо зв'язки з матриці інцидентності
            for (int i = 0; i < graphCommunications.GetLength(0); i++)
            {
                for (int j = 0; j < graphCommunications.GetLength(1); j++)
                {
                    if (graphCommunications[i, j] > 0)
                    {
                        tasks[i].Dependencies.Add(tasks[j], graphCommunications[i,j]);
                        tasks[j].Adjectives.Add(tasks[i], graphCommunications[i, j]);
                    }
                }
            }

            //вирахуємо ранги задач
            for (int i = 0; i < tasks.Count; i++)
            {
                tasks[i].GetRank();
            }

            //формуємо пул задач, по спаданню критичного шляху вверх
            for (int i = 0; i < tasks.Count - 1; i++)
            {
                for (int j = i + 1; j < tasks.Count; j++)
                {
                    if (tasks[i].Rank < tasks[j].Rank)
                    {
                        Task buffer = tasks[j];
                        tasks[j] = tasks[i];
                        tasks[i] = buffer;
                    }
                }
            }

            //Виведемо відсортований пул задач 
            //foreach(Task task in tasks)
            //{
            //    task.Print();
            //}

            //Поки пул задач не порожній
            while (tasks.Any())
            {
                //Console.WriteLine();
                //Console.WriteLine("--------------------------");
                //Console.WriteLine("We try to immerse task " + tasks[0].ID);
                //індекс процесору, на якому буде мінімальний час закінчення задачі
                int indexCPU = 0;

                //оскільки система однорідна, то немає сенсу оцінювати час кінця, 
                //простіше оцінювати час початку
                int betterStartTick = 0;

                //перевіряємо всі процесори в системі
                for (int i = 0; i < processors.Count; i++)
                {
                    //Console.WriteLine("=========");
                    //Console.WriteLine("We check proc " + processors[i].ID);
                    int currentStart = processors[i].GetFirstAvailableTick(tasks[0]);
                    //Console.WriteLine("In this proc task may start from " + currentStart);
                    //перед перевіркою наступних процесорів, припустимо, 
                    //що погруження на найшвидший процесор буде найефективнішим
                    if (i == 0) betterStartTick = currentStart;

                    //якщо час закінчення на даному процесорі кращий, ніж на попередніх перевірених
                    if (currentStart < betterStartTick)
                    {
                        indexCPU = i;
                        betterStartTick = currentStart;
                    }

                    //Console.WriteLine("=========");
                }

                //Console.WriteLine("We immerse task in proc "+indexCPU+" from tick "+betterStartTick);
                //Погружаємо задачу на кращий процесор
                processors[indexCPU].Immersion(tasks[0], betterStartTick);

                //Перед видаленням перевіримо чи ця задача мала найпізніший час закінення
                lastIndex = Math.Max(lastIndex, tasks[0].Finish);

                //Видаляємо задачу з пулу
                tasks.RemoveAt(0);
                //Console.WriteLine("--------------------------");
                //Console.WriteLine();
            }
            //Console.WriteLine();
            Processor.SetTokens(processorsCount);
            //нумеруємо колонки
            Console.BackgroundColor = ConsoleColor.White;
            Console.ForegroundColor = ConsoleColor.Black;
            Console.Write("    ");
            for (int i = 0; i < processors.Count; i++)
                Console.Write(i.StringBuild());
            Console.Write("     Маркер \tШина ");
            

            //заповнюємо рядки
            Console.WriteLine();
            for (int i = 0; i < lastIndex; i++)
            {
                Console.Write(i.StringBuild());
                for (int j = 0; j < processors.Count; j++)
                {
                    if (processors[j].Ticks[i] != " ")
                        Console.BackgroundColor = ConsoleColor.Yellow;
                    else
                        Console.BackgroundColor = ConsoleColor.White;
                    Console.Write(processors[j].Ticks[i].StringBuild());
                }
                Console.BackgroundColor = ConsoleColor.White;
                Console.Write("\t" + Processor.Token[i]);
                Console.Write("\t" + Processor.Bus[i]);
                
                Console.BackgroundColor = ConsoleColor.White;
                Console.WriteLine();
            }


            //Очищаємо дані
            processors.Clear();
            tasks.Clear();

            //Затримка
            Console.ReadKey();
        }
    }
}
