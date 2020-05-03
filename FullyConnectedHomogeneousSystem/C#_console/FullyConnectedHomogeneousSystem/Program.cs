using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FullyConnectedHomogeneousSystem
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

        private static List<GraphNode> pool = new List<GraphNode>();
        private static List<CPU> cpus = new List<CPU>();
        private static int lastIndex = 0;

        //кількість процесорів
        private static int CPUs_count = 4;

        //розмірність масиву - кількість вершин, кожен елемент - вага відповідної вершини, > 0
        private static int[] graphNodesWeights = new int[] { 2, 2, 2, 4, 7, 5, 3, 1, 5, 1, 3, 3, 4, 4, 7, 1 };

        //матриця зв'язності
        private static int[,] graphCommunications = new int[,] {
            /*      0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15 */
            /*0*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*1*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*2*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*3*/{  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*4*/{  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*5*/{  0,  0,  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                     
            /*6*/{  0,  0,  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*7*/{  0,  0,  0, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*8*/{  0,  4,  0,  0,  2,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*9*/{  0,  0,  0,  0,  0,  0, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*10*/{  0,  0,  0,  0,  0,  0,  0,  0,  3,  0,  0,  0,  0,  0,  0,  0 },
           /*11*/{  0,  0,  0,  0,  0,  0,  0,  0,  2,  0,  0,  0,  0,  0,  0,  0 },
           /*12*/{  0,  0,  0,  0,  0,  0,  0,  0,  5,  0,  0,  0,  0,  0,  0,  0 },
           /*13*/{  0,  0,  0,  3,  0,  0,  0,  0,  0,  0,  6,  1,  0,  0,  0,  0 },
           /*14*/{  0,  0,  0,  0,  0,  0,  4,  0,  0,  0,  0,  1,  6,  0,  0,  0 },
           /*15*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  3,  0 }};
        static void Main(string[] args)
        {
            //Створимо відповідну кількість процесорів
            for (int i = 0; i < CPUs_count; i++)
                cpus.Add(new CPU(i));

            //Створимо задачі з відповідними вагами
            for (int i = 0; i < graphNodesWeights.Length; i++)
                pool.Add(new GraphNode(i, graphNodesWeights[i]));

            //Задамо зв'язки з матриці інцидентності
            for (int i = 0; i < graphCommunications.GetLength(0); i++)
            {
                for (int j = 0; j < graphCommunications.GetLength(1); j++)
                {
                    if(graphCommunications[i,j] > 0 )
                    {
                        pool[i].Parents.Add(pool[j], graphCommunications[i, j]);
                        pool[j].Children.Add(pool[i], graphCommunications[i, j]);
                    }
                }
            }
                
            //вирахуємо ранги задач
            for (int i = 0; i < pool.Count; i++)
            {
                pool[i].GetWeightParents();
            }

            //формуємо пул задач, по спаданню критичного шляху вверх
            for (int i = 0; i < pool.Count - 1; i++)
            {
                for (int j = i + 1; j < pool.Count; j++)
                {
                    if (pool[i].WeightParents < pool[j].WeightParents)
                    {
                        GraphNode buffer = pool[j];
                        pool[j] = pool[i];
                        pool[i] = buffer;
                    }
                }
            }

            //Виведемо відсортований пул задач 
            //foreach(GraphNode graphNode in pool)
            //{
            //    graphNode.Print();
            //}

            //Поки пул задач не порожній
            while (pool.Any())
            {
                //індекс процесору, на якому буде мінімальний час закінчення задачі
                int indexCPU = 0;

                //оскільки система однорідна, то немає сенсу оцінювати час кінця, 
                //простіше оцінювати час початку
                int betterStartTick = 0;

                //перевіряємо всі процесори в системі
                for (int i = 0; i < cpus.Count; i++)
                {
                    //перевіримо, чи при погрузці на цей процесор в задачі будуть наявні батьківські задачі, 
                    //які були погружені на інші процесори
                    int currentStart = 0;
                    if (pool[0].Parents.Any())
                    {
                        foreach (KeyValuePair<GraphNode, int> parent in pool[0].Parents)
                        {
                            //якщо батьківська задача на іншому процесорі то врахуємо пересилку
                            if (cpus[i] != parent.Key.CPU)
                            {
                                currentStart = Math.Max(currentStart, parent.Key.Finish+1 + pool[0].Parents[parent.Key]);
                            }
                            else
                            {
                                currentStart = Math.Max(currentStart, parent.Key.Finish+1);
                            }
                        }
                    }

                    //мінімальний такт, з якого може початись виконання задачі на даному процесорі
                    int startTick = cpus[i].GetFreeWindowIndex(currentStart, pool[0].Weight);

                    //перед перевіркою наступних процесорів, припустимо, 
                    //що погруження на найшвидший процесор буде найефективнішим
                    if (i == 0) betterStartTick = startTick;

                    //якщо час закінчення на даному процесорі кращий, ніж на попередніх перевірених
                    if (startTick < betterStartTick)
                    {
                        indexCPU = i;
                        betterStartTick = startTick;
                    }
                }

                //Погружаємо задачу на кращий процесор
                cpus[indexCPU].Set(pool[0], betterStartTick);

                //Перед видаленням перевіримо чи ця задача мала найпізніший час закінення
                lastIndex = Math.Max(lastIndex, pool[0].Finish);

                //Видаляємо задачу з пулу
                pool.RemoveAt(0);
            }

            //нумеруємо колонки
            Console.BackgroundColor = ConsoleColor.White;
            Console.ForegroundColor = ConsoleColor.Black;
            Console.Write("    ");
            for (int i = 0; i < cpus.Count; i++)
                Console.Write(i.StringBuild());

            //заповнюємо рядки
            Console.WriteLine();
            for(int i = 0; i < lastIndex + 1; i++)
            {
                Console.Write(i.StringBuild());
                for (int j = 0; j < cpus.Count; j++)
                {
                    if (cpus[j].Work[i] != " ")
                        Console.BackgroundColor = ConsoleColor.Green;
                    else
                        Console.BackgroundColor = ConsoleColor.White;
                    Console.Write(cpus[j].Work[i].StringBuild());
                }
                Console.BackgroundColor = ConsoleColor.White;
                Console.WriteLine();
            }
                

            //Очищаємо дані
            cpus.Clear();
            pool.Clear();

            //Затримка
            Console.ReadKey();
        }
    }
}
