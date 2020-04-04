using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FDDI
{
    class Task
    {
        private int id;
        private Dictionary<Task, int> senders;
        private Dictionary<Task, int> receivers;
        private int length;
        private int rank;
        private int finish;
        private Processor processor;

        public Task(int id, int length)
        {
            this.id = id;
            this.length = length;
            senders = new Dictionary<Task, int>();
            receivers = new Dictionary<Task, int>();
        }

        public int GetRank()
        {
            if (receivers.Any())
            {
                int max = 0;
                foreach (KeyValuePair<Task, int> receiver in receivers)
                    max = Math.Max(max, (receiver.Key.GetRank() + receiver.Value));
                if (length + max > rank)
                    rank = length + max;
            }
            else
            {
                if (length > rank) rank = length;
            }
            return rank;
        }

        public void SortSendersByFinishTime()
        {
            List<Task> sortedkeys = new List<Task>();
            Dictionary<Task, int> sortedSenders = new Dictionary<Task, int>();
            foreach (KeyValuePair<Task, int> sender in senders)
            {
                sortedkeys.Add(sender.Key);
            }
            for (int i = 0; i < sortedkeys.Count - 1; i++)
            {
                for (int j = i + 1; j < sortedkeys.Count; j++)
                {
                    if (sortedkeys[i].Finish > sortedkeys[j].Finish)
                    {
                        Task buffer = sortedkeys[j];
                        sortedkeys[j] = sortedkeys[i];
                        sortedkeys[i] = buffer;
                    }
                }
            }

            for (int i = 0; i < sortedkeys.Count; i++)
            {
                sortedSenders.Add(sortedkeys[i], senders[sortedkeys[i]]);
            }
            this.senders = sortedSenders;
        }

        public int ID
        {
            get { return this.id; }
        }

        public int Length
        {
            get { return this.length; }
        }

        public Dictionary<Task, int> Senders
        {
            get { return this.senders; }
        }

        public Dictionary<Task, int> Receivers
        {
            get { return this.receivers; }
        }

        public int Rank
        {
            get { return this.rank; }
            set { this.rank = value; }
        }

        public int Finish
        {
            get { return this.finish; }
            set { this.finish = value; }
        }

        public Processor Processor
        {
            get { return this.processor; }
            set { this.processor = value; }
        }

        public void Print()
        {
            Console.Write("\nTask number:" + id + " \nSenders: ");
            foreach (KeyValuePair<Task, int> dependency in senders)
            {
                Console.Write("[ID: " + dependency.Key.ID + " Communication length: " + dependency.Value + "] ");
            }
            Console.Write("\nReceivers: ");
            foreach (KeyValuePair<Task, int> adjective in receivers)
            {
                Console.Write("[ID: " + adjective.Key.ID + " Communication length: " + adjective.Value + "] ");
            }
            Console.Write("\nTask length: " + length);
            Console.Write("\nRankUp: " + rank);
        }
    }
}
