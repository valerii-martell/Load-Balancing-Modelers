using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace StarWithActiveCenter
{
    static class Program
    {
        public static int transferLabelLength = 0;
        public static int worktime = 0;
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

        public static List<Node> plan = new List<Node>();
        public static List<Element> topology = new List<Element>();
        private static int lastIndex = 0;

        //кількість процесорів
        //перший процесор виконуватиме роль активного цетру
        private static int topology_count = 5;

        //розмірність масиву - кількість вершин, кожен елемент - вага відповідної вершини, > 0
        //                                                   0  1  2  3  4  5  6  7  8  9 10 11 12 13 14
        private static int[] graphNodesWeights = new int[] { 2, 7, 6, 8, 5, 4, 4, 4, 5, 6, 4, 5, 7, 7, 4 };

        //матриця зв'язності
        private static int[,] graphCommunications = new int[,] {
            /*      0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  */
            /*0*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*1*/{  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*2*/{  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*3*/{  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*4*/{  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                                 
            /*5*/{  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },                                                     
            /*6*/{  0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*7*/{  0,  2,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*8*/{  0,  0,  3,  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /*9*/{  0,  0,  0,  5,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*10*/{  0,  0,  0,  0,  2,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*11*/{  0,  0,  0,  0,  0,  3,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
           /*12*/{  0,  0,  0,  0,  0,  0,  1,  2,  5,  0,  0,  0,  0,  0,  0 },
           /*13*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  5,  1,  3,  0,  0,  0 },
           /*14*/{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  0 }};

        static void Main(string[] args)
        {
            //Створимо відповідну кількість процесорів
            for (int i = 0; i < topology_count; i++)
                topology.Add(new Element(i));

            //Створимо задачі з відповідними вагами
            for (int i = 0; i < graphNodesWeights.Length; i++)
                plan.Add(new Node(i, graphNodesWeights[i]));

            //Задамо зв'язки з матриці інцидентності
            for (int i = 0; i < graphCommunications.GetLength(0); i++)
            {
                for (int j = 0; j < graphCommunications.GetLength(1); j++)
                {
                    if (graphCommunications[i, j] > 0)
                    {
                        plan[i].Dependencies.Add(plan[j], graphCommunications[i, j]);
                        plan[j].Adjectives.Add(plan[i], graphCommunications[i, j]);
                    }
                }
            }

            //вирахуємо ранги задач
            for (int i = 0; i < plan.Count; i++)
                plan[i].GetWeightDependencies();

            //формуємо план по спаданню критичного шляху вверх
            for (int i = 0; i < plan.Count - 1; i++)
            {
                for (int j = i + 1; j < plan.Count; j++)
                {
                    if (plan[i].WeightDependencies < plan[j].WeightDependencies)
                    {
                        Node buffer = plan[j];
                        plan[j] = plan[i];
                        plan[i] = buffer;
                    }
                }
            }

            //Поки пул задач не порожній
            while (plan.Any())
            {
                
                //індекс процесору, на якому буде мінімальний час закінчення задачі
                int index = 0;

                //оскільки система однорідна, то немає сенсу оцінювати час кінця, 
                //простіше оцінювати час початку
                int earliestStartTick = 0;

                //перевіряємо всі процесори в системі
                for (int i = 0; i < topology.Count; i++)
                {
                    //Console.WriteLine("__________________________");
                    //Console.WriteLine("WE TRY TO IMMERSE " + plan[0].ID + " IN PROC " + i);
                    //перевіримо, чи при погрузці на цей процесор в задачі будуть наявні батьківські задачі, 
                    //які були погружені на інші процесори
                    int currentStart = 0;
                    if (plan[0].Dependencies.Any())
                    {
                        foreach (KeyValuePair<Node, int> dependency in plan[0].Dependencies)
                        {
                            //якщо батьківська задача на іншому елементі то врахуємо пересилку
                            if (topology[i] != dependency.Key.Element)
                            {
                                //Console.WriteLine("Task has dependencies in another processors");
                                //можливі чотири ситуації:
                                //якщо поточний елемент не є центром
                                if (topology[i].ID != 0)
                                {
                                    //і елемент від якого йде відправка даних теж не є центром
                                    //то пересилка відбувається в два етапи, тобто подвоюється
                                    if (dependency.Key.Element.ID != 0)
                                    {
                                        currentStart = Math.Max(currentStart, (dependency.Key.Finish + 2*plan[0].Dependencies[dependency.Key]));
                                        //Console.WriteLine("Вiдправка йде не вiд центру, приймач не на центрi, довжина пересилки " + currentStart);
                                    }
                                    //але батьківська задача погружена на цетральний елемент
                                    //то пересилка йде в один етап
                                    else
                                    {
                                        currentStart = Math.Max(currentStart, dependency.Key.Finish + plan[0].Dependencies[dependency.Key]);
                                        //Console.WriteLine("Вiдправка йде вiд центру, приймач не на центрi, довжина пересилки " + currentStart);
                                    }
                                }
                                //якщо поточний елемент є цетром
                                else
                                {
                                    //а процесор від якого йде відправка даних не є центром
                                    //то пересилка відбувається в один етап
                                    if (dependency.Key.Element.ID != 0)
                                    {
                                        currentStart = Math.Max(currentStart, dependency.Key.Finish + plan[0].Dependencies[dependency.Key]);
                                    }
                                    //і батьківська задача теж погружена на цетральний елемент
                                    //то пересилка не потрібна, час старту визначається закінченням батьківської задачі
                                    else
                                    {
                                        currentStart = Math.Max(currentStart, dependency.Key.Finish);
                                    }
                                }
                            }
                            else
                            {
                                currentStart = Math.Max(currentStart, dependency.Key.Finish);
                            }
                        }
                    }
                    if (currentStart!=0) currentStart++;

                    //Console.WriteLine("FOR NODE " + plan[0].ID + " IN PROC " + i + " TRANSFER FINISHED " + currentStart);

                    //мінімальний такт, з якого може початись виконання задачі на даному процесорі
                    int startTick = topology[i].GetStartIndex(currentStart, plan[0].Weight);

                    //перед перевіркою наступних процесорів, припустимо, 
                    //що погруження на найшвидший процесор буде найефективнішим
                    if (i == 0) earliestStartTick = startTick;

                    //якщо час закінчення на даному процесорі кращий, ніж на попередніх перевірених
                    if (startTick < earliestStartTick)
                    {
                        index = i;
                        earliestStartTick = startTick;
                    }
                }

                //Погружаємо задачу на кращий процесор
                topology[index].Immersion(plan[0], earliestStartTick);
                //Console.WriteLine("NODE " + plan[0].ID + " IMMERSE IN PROC " + index + " FROM " +earliestStartTick);
                //Перед видаленням перевіримо чи ця задача мала найпізніший час закінення
                lastIndex = Math.Max(lastIndex, plan[0].Finish);
                //Console.WriteLine("=======================");

                //Видаляємо задачу з пулу
                plan.RemoveAt(0);
            }

            //нумеруємо колонки
            Console.BackgroundColor = ConsoleColor.White;
            Console.ForegroundColor = ConsoleColor.Black;
            Console.Write("    ");
            for (int i = 0; i < topology.Count; i++)
                Console.Write(i.StringBuild());
            Console.Write("      NETWORK STATUS ".StringBuild());

            //заповнюємо рядки
            Console.WriteLine();
            for (int i = 0; i < lastIndex + 1; i++)
            {
                Console.Write(i.StringBuild());
                for (int j = 0; j < topology.Count; j++)
                {
                    if (topology[j].Work[i] != " ")
                        Console.BackgroundColor = ConsoleColor.Yellow;
                    else
                        Console.BackgroundColor = ConsoleColor.White;
                    Console.Write(topology[j].Work[i].StringBuild());
                }
                if (topology[0].Transfer[i] != " ")
                    Console.BackgroundColor = ConsoleColor.Cyan;
                else
                    Console.BackgroundColor = ConsoleColor.White;
                Console.Write(topology[0].Transfer[i].StringBuild());
                Console.BackgroundColor = ConsoleColor.White;
                Console.WriteLine();
            }


            //Очищаємо дані
            topology.Clear();
            plan.Clear();

            //Затримка
            Console.ReadKey();
        }
    }
}
