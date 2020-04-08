using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace StarWithActiveCenter
{
    class Node
    {
        private int id;
        private Dictionary<Node, int> dependencies;
        private Dictionary<Node, int> adjectives;
        private int weight;
        private int weightDependencies;
        private int weightAdjectives;
        private int finish;
        private Element element;

        public Node(int id, int weight)
        {
            this.id = id;
            this.weight = weight;
            dependencies = new Dictionary<Node, int>();
            adjectives = new Dictionary<Node, int>();
            weightDependencies = 0;
            weightAdjectives = 0;
        }

        public int GetWeightDependencies()
        {
            if (adjectives.Any())
            {
                int max = 0;
                foreach (KeyValuePair<Node, int> adjective in adjectives)
                    max = Math.Max(max, adjective.Key.GetWeightDependencies() + adjective.Value);
                if (weight + max > weightDependencies) weightDependencies = weight + max;
            }
            else
            {
                if (weight > weightDependencies) weightDependencies = weight;
            }
            return weightDependencies;
        }

        public int ID
        {
            get { return this.id; }
        }

        public int Weight
        {
            get { return this.weight; }
        }

        public Dictionary<Node, int> Dependencies
        {
            get { return this.dependencies; }
        }

        public Dictionary<Node, int> Adjectives
        {
            get { return this.adjectives; }
        }

        public int WeightDependencies
        {
            get { return this.weightDependencies; }
            set { this.weightDependencies = value; }
        }

        public int WeightAdjectives
        {
            get { return this.weightAdjectives; }
            set { this.weightAdjectives = value; }
        }

        public int Finish
        {
            get { return this.finish; }
            set { this.finish = value; }
        }

        public Element Element
        {
            get { return this.element; }
            set { this.element = value; }
        }
    }
}
