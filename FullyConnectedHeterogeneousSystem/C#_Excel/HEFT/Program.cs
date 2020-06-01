using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Excel = Microsoft.Office.Interop.Excel;

namespace HEFT
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

        private static void Compute()
        {
            //Зчитуємо параметри системи, комбінаціями id процесора + продуктивність процесора
            int index = 3;
            do
            {
                Excel.Range excelcellsID = excelworksheet.get_Range("A" + index);
                Excel.Range excelcellsPerformance = excelworksheet.get_Range("B" + index++);
                try
                {
                    processors.Add(new Processor(
                        Int32.Parse(Convert.ToString(excelcellsID.Value2)),
                        Double.Parse(Convert.ToString(excelcellsPerformance.Value2))));
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
                        processors.Add(new Processor(i, processorPerformance));
                    }
                }
                catch
                {

                }
            }

            //відсортуємо процесори за їх продуктивністю
            for (int i = 0; i < processors.Count - 1; i++)
            {
                for (int j = i + 1; j < processors.Count; j++)
                {
                    if (processors[i].Performance > processors[j].Performance)
                    {
                        Processor buffer = processors[j];
                        processors[j] = processors[i];
                        processors[i] = buffer;
                    }
                }
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
            
            //вирахуємо ранги задач
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
            foreach(Task task in tasks)
            {
                task.Print();
            }

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
                    int transferFinish = 0;
                    if (tasks[0].Senders.Any())
                    {
                        foreach (KeyValuePair<Task, int> sender in tasks[0].Senders)
                        {
                            if (processors[i] != sender.Key.Processor)
                            {
                                //якщо батьківська задача на іншому процесорі то врахуємо пересилку
                                if (processors[i] != sender.Key.Processor)
                                    transferFinish = Math.Max(transferFinish, sender.Key.FinishTick + tasks[0].Senders[sender.Key]);
                                else
                                    transferFinish = Math.Max(transferFinish, sender.Key.FinishTick);
                                transferFinish++;
                            }
                        }
                    }
                    

                    //кількість тактів, яку займе виконання даної задачі на даному процесорі, 
                    //з врахуванням продуктивності процесору
                    int realTaskLength = Convert.ToInt32(Math.Ceiling(tasks[0].Length * processors[i].Performance));

                    //мінімальний такт, з якого може початись виконання задачі на даному процесорі
                    int startTick = processors[i].GetFirstAvailableTick(transferFinish, realTaskLength);
                    
                    //час закінчення задачі на даному процесорі
                    int thisFinishTime = startTick + realTaskLength;

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
            excelworksheet.get_Range("N2", "AZ200").Cells.Interior.Color = Excel.XlRgbColor.rgbWhite;
            excelworksheet.get_Range("N2", "AZ200").Cells.Value = "";
            excelworksheet.get_Range("N2", "AZ200").Cells.NumberFormat = "@";
            //excelworksheet.Columns.AutoFit();

            //пронумеруємо такти (рядки)
            for (int i = 3; i < lastTick + 3; i++)
            {
                excelworksheet.Cells[i, 14].Value = i - 3;
            }

            //пронумеруємо процесори і їх мережеві карти(колонки)
            int p = 15;
            foreach (Processor processor in processors)
            {
                excelworksheet.Cells[2, p].Value = processor.ID;
                p++;
                excelworksheet.Cells[2, p].Value = "NC(" + processor.ID.ToString() + ")";
                p++;
            }
            
            //записуємо результати погруження
            p = 15;
            foreach(Processor processor in processors)
            {
                //процесор
                for (int j = 0; j < lastTick; j++)
                {
                    //пустий тік
                    if (processor.Ticks[j] == " ")
                    {
                        excelworksheet.Cells[j + 3, p].Interior.Color = Excel.XlRgbColor.rgbLavender;
                    }
                    else
                    //задача
                    {
                        excelworksheet.Cells[j + 3, p].Value = "       " + processor.Ticks[j] + "       ";
                        excelworksheet.Cells[j + 3, p].Interior.Color = Excel.XlRgbColor.rgbLime;
                    }
                }
                //мережева карта
                p++;
                for (int j = 0; j < lastTick; j++)
                {
                    //пустий тік
                    if (processor.NetworkCardTicks[j] == " ")
                    {
                        excelworksheet.Cells[j + 3, p].Interior.Color = Excel.XlRgbColor.rgbLavender;
                    }
                    else
                    //пересилка
                    {
                        excelworksheet.Cells[j + 3, p].Value = processor.NetworkCardTicks[j];
                        excelworksheet.Cells[j + 3, p].Interior.Color = Excel.XlRgbColor.rgbDeepSkyBlue;
                    }
                }
                p++;
            }
           
            //заголовок
            excelworksheet.get_Range("N1").Font.Size = 16;
            excelworksheet.get_Range("N1").Cells.Value = "ПЛАН ПОГРУЖЕННЯ";

            //коефіцієнти загруженості кожного процесора та його мережевої карти  
            excelworksheet.Cells[lastTick + 3, 14].Value = "КЗ";
            excelworksheet.get_Range("N" + (lastTick + 3)).EntireRow.AutoFit();
            excelworksheet.get_Range("N" + (lastTick + 3)).EntireColumn.AutoFit();
            p = 15;
            foreach(Processor processor in processors)
            {
                excelworksheet.Cells[lastTick + 3, p].Value = (processor.GetEfficiencyСoefficient(lastTick) * 100).ToString("#.#") + "%";
                excelworksheet.Cells[lastTick + 3, p].Interior.Color = Excel.XlRgbColor.rgbViolet;
                p++;
                excelworksheet.Cells[lastTick + 3, p].Value = (processor.GetNetworkCardEfficiencyСoefficient(lastTick) * 100).ToString("#.#") + "%";
                excelworksheet.Cells[lastTick + 3, p].Interior.Color = Excel.XlRgbColor.rgbViolet;
                p++;
            }
            
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
                    if (check != change)
                    {
                        Compute();
                        check = change;
                    }
                }
            }
            catch { }
            Console.ReadKey();
        }
    }
}
