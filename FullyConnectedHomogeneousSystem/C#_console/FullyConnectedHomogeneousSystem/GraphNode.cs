using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FullyConnectedHomogeneousSystem
{
    class GraphNode
    {
        private int id;
        private Dictionary<GraphNode, int> parents;
        private Dictionary<GraphNode, int> children;
        private int weight;
        private int weightParents;
        private int weightChildren;
        private int finish;
        private CPU cpu;

        public int ID
        {
            get { return this.id; }
        }

        public int Weight
        {
            get { return this.weight; }
        }

        public Dictionary<GraphNode, int> Parents
        {
            get { return this.parents; }
        }

        public Dictionary<GraphNode, int> Children
        {
            get { return this.children; }
        }

        public int WeightParents
        {
            get { return this.weightParents; }
            set { this.weightParents = value; }
        }

        public int WeightChildren
        {
            get { return this.weightChildren; }
            set { this.weightChildren = value; }
        }

        public int Finish
        {
            get { return this.finish; }
            set { this.finish = value; }
        }

        public CPU CPU
        {
            get { return this.cpu; }
            set { this.cpu = value; }
        }

        public GraphNode(int id, int weight)
        {
            this.id = id;
            this.weight = weight;
            parents = new Dictionary<GraphNode, int>();
            children = new Dictionary<GraphNode, int>();
            weightParents = 0;
            weightChildren = 0;
        }

        //ранг при проході вверх
        public int GetWeightParents()
        {
            if (children.Any())
            {
                int max = 0;
                foreach (KeyValuePair<GraphNode, int> child in children)
                {
                    max = Math.Max(max, child.Key.GetWeightParents() + child.Value);
                }
                if (weight + max > weightParents)
                {
                    weightParents = weight + max;
                }
            }
            else
            {
                if (weight > weightParents)
                {
                    weightParents = weight;
                }
            }
            return weightParents;
        }
    }
}
