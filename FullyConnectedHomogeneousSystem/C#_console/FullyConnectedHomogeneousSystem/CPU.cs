using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FullyConnectedHomogeneousSystem
{
    class CPU
    {
        private string[] work;

        private int id;

        public CPU(int id)
        {
            this.id = id;
            work = new string[1000];
            for (int i = 0; i < 1000; i++)
            {
                work[i] = " ";
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

        public int GetFreeWindowIndex(int start, int weight)
        {
            for (int i = start; i < work.Length; i++)
            {
                bool isFree = false;
                if (work[i] == " ")
                {
                    //вільний тік знайдено
                    isFree = true;
                    //перевіряємо наступні тіки
                    for (int j = i + 1; j < i + weight; j++)
                    {
                        if (work[j] != " ")
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

        public void Set(GraphNode graphNode, int betterStart)
        {
            //погружаємо власне задачу
            for (int i = betterStart; i < betterStart + graphNode.Weight; i++)
            {
                work[i] = graphNode.ID.ToString();
            }
            //позначимо для погруженою задачі час закінчення та процесор
            graphNode.Finish = betterStart + graphNode.Weight - 1;
            graphNode.CPU = this;
        }
    }
}
