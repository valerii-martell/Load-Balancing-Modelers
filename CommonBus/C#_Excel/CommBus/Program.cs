using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Excel = Microsoft.Office.Interop.Excel;

namespace CommBus
{
    static class Program
    {
        private static List<Task> tasks = new List<Task>();
        private static List<Processor> processors = new List<Processor>();
        //запустимо новий екземпляр процесу Excel
        static Excel.Application excelapp = new Excel.Application();
        static Excel.Workbooks excelappworkbooks = excelapp.Workbooks;
        //Відкриємо книгу HEFT.xlsx та отримаємо посилання на таблиці в ній
        static Excel.Workbook excelappworkbook = excelapp.Workbooks.Open(AppDomain.CurrentDomain.BaseDirectory + @"\HEFT.xlsx");
        static Excel.Sheets excelsheets = excelappworkbook.Worksheets;
        //Отримаємо посилання на першу таблицю в книзі
        static Excel.Worksheet excelworksheet = (Excel.Worksheet)excelsheets.get_Item(1);
        //Шина
        public static Processor bus;

        private static void Compute()
        {
            bus = new Processor(-1);

            //Зчитуємо параметри системи, комбінаціями id процесора + продуктивність процесора
            int index = 3;
            do
            {
                Excel.Range excelcellsID = excelworksheet.get_Range("A" + index);
                Excel.Range excelcellsPerformance = excelworksheet.get_Range("B" + index++);
                try
                {
                    processors.Add(new Processor(Int32.Parse(Convert.ToString(excelcellsID.Value2))));
                }
                catch
                {
                    break;
                }
            }
            while (true);

            //якщо зчитування не відбулось спробуємо зчитати комбіновані параметри системи
            if (!processors.Any())
            {
                //
                Excel.Range processorsCountCell = excelworksheet.get_Range("E2");
                Excel.Range processorsPerformanceCell = excelworksheet.get_Range("E3");
                try
                {
                    double processorPerformance = Double.Parse(Convert.ToString(processorsPerformanceCell.Value2));
                    for (int i = 0; i < Int32.Parse(Convert.ToString(processorsCountCell.Value2)); i++)
                    {
                        processors.Add(new Processor(i));
                    }
                }
                catch { }
            }

            //виведемо відсортований список процесорів в системі
            //for (int i = 0; i < processors.Count; i++)
            //{
            //    Console.WriteLine(processors[i].ID + " " + processors[i].Performance);
            //}

            //Зчитуємо граф програми
            //Формуємо список задач
            index = 3;
            do
            {
                Excel.Range excelcellsTaskID = excelworksheet.get_Range("G" + index);
                Excel.Range excelcellsTaskLength = excelworksheet.get_Range("H" + index++);
                try
                {
                    tasks.Add(new Task(
                        Int32.Parse(Convert.ToString(excelcellsTaskID.Value2)),
                        Int32.Parse(Convert.ToString(excelcellsTaskLength.Value2))));
                }
                catch
                {
                    break;
                }
            }
            while (true);


            //Зчитуємо граф програми
            //Задаємо зв'язки між задачами
            index = 3;
            char delimiter = ' ';
            for (int i = 0; i < tasks.Count; i++)
            {
                Excel.Range excelcellsTaskSenders = excelworksheet.get_Range("I" + index);
                Excel.Range excelcellsTaskCommLength = excelworksheet.get_Range("J" + index++);
                string sendersStr = Convert.ToString(excelcellsTaskSenders.Value2);
                string commLengthsStr = Convert.ToString(excelcellsTaskCommLength.Value2);
                try
                {
                    string[] senders = sendersStr.Split(delimiter);
                    string[] commLengths = commLengthsStr.Split(delimiter);
                    for (int j = 0; j < senders.Length; j++)
                    {
                        int neighbor = Int32.Parse(senders[j]);
                        int comm = Int32.Parse(commLengths[j]);
                        tasks[i].Senders.Add(tasks[neighbor], comm);
                        tasks[neighbor].Receivers.Add(tasks[i], comm);
                    }
                }
                catch { }
            }


            for (int i = 0; i < tasks.Count; i++)
            {
                tasks[i].GetRankUp();
                tasks[i].GetRankDown();
            }

            //формуємо пул задач, по спаданню критичного шляху вверх
            for (int i = 0; i < tasks.Count - 1; i++)
            {
                for (int j = i + 1; j < tasks.Count; j++)
                {
                    if (tasks[i].RankUp < tasks[j].RankUp)
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
                //мінімальний час закінчення задачі
                int earliestFinishTime = 0;

                //індекс процесору, на якому буде мінімальний час закінчення задачі
                int earliestFinishProcessor = 0;

                //час, з якого повинна буде початись остаточна погрузка
                int betterStartTick = 0;

                //перевіряємо всі процесори в системі
                for (int i = 0; i < processors.Count; i++)
                {
                    //перевіримо, чи при погрузці на цей процесор в задачі будуть наявні батьківські задачі, 
                    //які були погружені на інші процесори
                    int transferLength = 0;
                    if (tasks[0].Senders.Any())
                    {
                        foreach (KeyValuePair<Task, int> sender in tasks[0].Senders)
                        {
                            if (processors[i] != sender.Key.Processor)
                            {
                                //довжина пересилок 
                                transferLength += tasks[0].Senders[sender.Key];
                            }
                        }
                    }

                    //кількість тактів, яку займе виконання даної задачі на даному процесорі, 
                    //з врахуванням необхідних пересилок 
                    int fullLength = tasks[0].Length + transferLength;

                    //мінімальний такт, з якого може початись виконання задачі на даному процесорі
                    int startTick = processors[i].GetFirstAvailableTick(tasks[0].GetFirstAvailableTick(), fullLength);

                    //час закінчення задачі на даному процесорі
                    int thisFinishTime = startTick + fullLength;

                    //перед перевіркою наступних процесорів, припустимо, 
                    //що погруження на найшвидший процесор буде найефективнішим
                    if (i == 0)
                    {
                        earliestFinishTime = thisFinishTime;
                        betterStartTick = startTick;
                    }

                    //якщо час закінчення на даному процесорі кращий, ніж на попередніх перевірених
                    if (thisFinishTime < earliestFinishTime)
                    {
                        earliestFinishTime = thisFinishTime;
                        earliestFinishProcessor = i;
                        betterStartTick = startTick;
                    }
                }

                //Погружаємо задачу на кращий процесор
                processors[earliestFinishProcessor].Immersion(tasks[0], betterStartTick);

                //Видаляємо задачу з пулу
                tasks.RemoveAt(0);
            }

            //визначаємо область відображення (найпізніший тік всіх процесорах) 
            int lastTick = 0;
            foreach (Processor processor in processors)
            {
                lastTick = Math.Max(lastTick, processor.CurrentLastTick);
            }

            //затираємо результати попереднього моделювання
            excelworksheet.get_Range("N2", "AZ500").Cells.Interior.Color = Excel.XlRgbColor.rgbWhite;
            excelworksheet.get_Range("N2", "AZ500").Cells.Value = "";
            excelworksheet.get_Range("N2", "AZ500").Cells.NumberFormat = "@";

            //пронумеруємо такти (рядки)
            for (int i = 3; i < lastTick + 3; i++)
            {
                excelworksheet.Cells[i, 14].Value = i - 3;
            }

            //пронумеруємо процесори (колонки)
            for (int i = 15; i < 15 + processors.Count; i++)
            {
                excelworksheet.Cells[2, i].Value = processors[i - 15].ID;
            }
            excelworksheet.Cells[2, 15+processors.Count].Value = "BUS";
 
            processors.Add(bus);
            //записуємо результати погруження
            for (int i = 0; i < processors.Count; i++)
            {
                for (int j = 0; j < lastTick; j++)
                {
                    try
                    {   //задача
                        int tick = Int32.Parse(processors[i].Ticks[j]);
                        if (tick >= 0)
                        {
                            excelworksheet.Cells[j + 3, i + 15].Value = tick;
                            excelworksheet.Cells[j + 3, i + 15].Interior.Color = Excel.XlRgbColor.rgbLime;
                        }
                    }
                    catch
                    {
                        //пустий тік
                        if (processors[i].Ticks[j] == " ")
                        {
                            excelworksheet.Cells[j + 3, i + 15].Interior.Color = Excel.XlRgbColor.rgbLavender;
                        }
                        //пересилка
                        else
                        {
                            if (i == processors.Count - 1)
                            {
                                excelworksheet.Cells[j + 3, i + 15].Interior.Color = Excel.XlRgbColor.rgbGold;
                            }
                            else
                            {
                                excelworksheet.Cells[j + 3, i + 15].Interior.Color = Excel.XlRgbColor.rgbDeepSkyBlue;
                            }
                            excelworksheet.Cells[j + 3, i + 15].Value = processors[i].Ticks[j];
                            
                        }
                    }
                }
            }

            //заголовок
            excelworksheet.get_Range("N1").Font.Size = 16;
            excelworksheet.get_Range("N1").Cells.Value = "ПЛАН ПОГРУЖЕННЯ";

            //коефіцієнти загруженості кожного процесора 
            excelworksheet.Cells[lastTick + 3, 14].Value = "КЗ";
            excelworksheet.get_Range("N" + (lastTick + 3)).EntireRow.AutoFit();
            excelworksheet.get_Range("N" + (lastTick + 3)).EntireColumn.AutoFit();
            for (int i = 15; i < 15 + processors.Count; i++)
            {
                excelworksheet.Cells[lastTick + 3, i].Value = (processors[i - 15].GetEfficiencyСoefficient(lastTick) * 100).ToString("#.#") + "%";
                excelworksheet.Cells[lastTick + 3, i].Interior.Color = Excel.XlRgbColor.rgbViolet;
            }

            /*for (int i = 0; i < processors.Count; i++)
            {
                processors[i].Print();
            }*/

            processors.Clear();
            tasks.Clear();
        }

        static void Main(string[] args)
        {
            try
            {
                string check = Convert.ToString(excelworksheet.get_Range("L2").Value2);
                while (true)
                {
                    string change = Convert.ToString(excelworksheet.get_Range("L2").Value2);
                    if (change == "0")
                    {
                        excelappworkbooks.Close();
                        excelapp.Quit();
                    }
                    else if (check != change)
                    {
                        Compute();
                        check = change;
                    }
                }
            }
            catch { }
            //Console.ReadKey();
        }
    }
}