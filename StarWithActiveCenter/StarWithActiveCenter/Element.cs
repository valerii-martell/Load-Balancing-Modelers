using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace StarWithActiveCenter
{
    class Element
    {
        private string[] work;
        private static string[] transfer;
        private int id;

        public Element(int id)
        {
            this.id = id;
            work = new string[10000];
            transfer = new string[10000];
            for (int i = 0; i < 10000; i++)
            {
                work[i] = " ";
                transfer[i] = " ";
            }
        }

        public int ID
        {
            get { return id; }
        }

        public string[] Work
        {
            get { return work; }
        }

        public string[] Transfer
        {
            get { return transfer; }
        }

        public bool isUse(int lastTick)
        {
            for (int i = 0; i < lastTick; i++)
            {
                if (work[i] != " ")
                {
                    return true;
                }
            }
            return false;
        }

        public int GetStartIndex(int startScan, int length)
        {
            for (int i = startScan; i < work.Length; i++)
            {
                bool isFree = false;
                if (work[i] == " ")
                {
                    //вільний тік знайдено
                    isFree = true;
                    //перевіряємо наступні тіки
                    for (int j = i + 1; j < i + length; j++)
                    {
                        if (work[j] != " ")
                        {
                            isFree = false;
                            break;
                        }
                    }
                }
                //вікно знайдено
                if (isFree) return i;
            }
            return 0;
        }

        public void Immersion(Node node, int start)
        {
            //спершу погружаємо пересилки даних на відповідні мережеві карти
            foreach (KeyValuePair<Node, int> dependency in node.Dependencies)
            {
                if (dependency.Key.Element != this)
                {
                    //довжина пересилки
                    int communicationLength = dependency.Value;

                    //можливі чотири ситуації:
                    //якщо поточний елемент не є центром
                    if (this.id != 0)
                    {
                        //і елемент від якого йде відправка даних теж не є центром
                        //то пересилка відбувається в два етапи
                        if (dependency.Key.Element.ID != 0)
                        {
                            //центр отримує дані
                            for (int i = dependency.Key.Finish + 1; i < dependency.Key.Finish + 1 + communicationLength; i++)
                            {
                                //центр отримує дані
                                transfer[i] += " | Transit " + node.ID + "(E0) <- " + dependency.Key.ID+"(E"+dependency.Key.Element.ID+")";
                            }

                            //центр передає дані
                            for (int i = dependency.Key.Finish + 1 + communicationLength; i < dependency.Key.Finish + 1 + 2*communicationLength; i++)
                            {
                                //центр передає дані
                                transfer[i] += " | Transit " + node.ID + "(E"+this.id+") <- " + dependency.Key.ID + "(E0)";
                            }
                        }
                        else //але елемент від якого йде відправка є центром, то пересилка йде в один етап
                        {
                            //центр передає дані
                            for (int i = dependency.Key.Finish + 1; i < dependency.Key.Finish + 1 + communicationLength; i++)
                            {
                                //центр передає дані
                                transfer[i] += " | "+node.ID + "(E"+this.id+") <- " + dependency.Key.ID + "(E"+dependency.Key.Element.ID+")";
                            }
                        }
                    }
                    //якщо поточний процесор є цетром
                    else
                    {
                        //а процесор від якого йде відправка даних не є центром
                        //то пересилка відбувається в один етап
                        //центр отримує дані
                        if (dependency.Key.Element.ID != 0)
                        {
                            for (int i = dependency.Key.Finish + 1; i < dependency.Key.Finish + 1 + communicationLength; i++)
                            {
                                //центр отримує дані
                                transfer[i] += " | " + node.ID + "(E"+this.id+") <- " + dependency.Key.ID + "(E"+dependency.Key.Element.ID+")";
                            }
                        }
                    }   
                }
            }

            //погружаємо задачу
            for (int i = start; i < start + node.Weight; i++)
                work[i] = node.ID.ToString();
            //позначимо для погруженою задачі час закінчення та процесор
            node.Finish = start + node.Weight - 1;
            node.Element = this;
            Program.worktime = Math.Max(Program.worktime, node.Finish);
        }
    }
}
