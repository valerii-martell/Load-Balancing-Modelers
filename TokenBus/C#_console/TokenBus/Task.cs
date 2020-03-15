using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TokenBus
{
    class Task
    {
        private int id;
        private Dictionary<Task, int> dependencies;
        private Dictionary<Task, int> adjectives;
        private int length;
        private int rank;
        private int finish;
        private Processor processor;

        public Task(int id, int length)
        {
            this.id = id;
            this.length = length;
            dependencies = new Dictionary<Task, int>();
            adjectives = new Dictionary<Task, int>();
        }

        public int GetRank()
        {
            if (adjectives.Any())
            {
                int max = 0;
                foreach (KeyValuePair<Task, int> adjective in adjectives)
                    max = Math.Max(max, (adjective.Key.GetRank() + adjective.Value));
                if (length + max > rank)
                    rank = length + max;
            }
            else
            {
                if (length > rank) rank = length;
            }
            return rank;
        }

        public void SortDependenciesByFinishTime()
        {
            List<Task> sortedkeys = new List<Task>();
            Dictionary<Task, int> sortedDependencies = new Dictionary<Task, int>();
            foreach(KeyValuePair<Task, int> dependency in dependencies)
            {
                sortedkeys.Add(dependency.Key);
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

            for(int i = 0; i < sortedkeys.Count; i++)
            {
                sortedDependencies.Add(sortedkeys[i], dependencies[sortedkeys[i]]);
            }
            this.dependencies = sortedDependencies;
        }

        public int ID
        {
            get { return this.id; }
        }

        public int Length
        {
            get { return this.length; }
        }

        public Dictionary<Task, int> Dependencies
        {
            get { return this.dependencies; }
        }

        public Dictionary<Task, int> Adjectives
        {
            get { return this.adjectives; }
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
        
        /*public void Print()
        {
            Console.Write("\nTask number:" + id + " \nSenders: ");
            foreach (Task dependency in dependencies)
            {
                Console.Write("[ID: " + dependency.ID + " Communication length: " + 1 + "] ");
            }
            Console.Write("\nReceivers: ");
            foreach (Task adjective in adjectives)
            {
                Console.Write("[ID: " + adjective.ID + " Communication length: " + 1 + "] ");
            }
            Console.Write("\nTask length: " + length);
            Console.Write("\nRankUp: " + rank);
        }*/       
    }
}