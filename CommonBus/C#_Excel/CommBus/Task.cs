using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CommBus
{
    class Task
    {
        private int id;
        private Dictionary<Task, int> senders;
        private Dictionary<Task, int> receivers;
        private int length;
        private int rankUp;
        private int rankDown;
        private int startTick;
        private int finishTick;
        private Processor processor;

        public Task(int id, int length)
        {
            this.id = id;
            this.length = length;
            senders = new Dictionary<Task, int>();
            receivers = new Dictionary<Task, int>();
            rankUp = 0;
            rankDown = 0;
        }

        public int GetRankUp()
        {
            if (receivers.Any())
            {
                int max = 0;
                foreach (KeyValuePair<Task, int> receiver in receivers)
                {
                    max = Math.Max(max, receiver.Key.GetRankUp() + receiver.Value);
                }
                if (length + max > rankUp)
                {
                    rankUp = length + max;
                }
            }
            else
            {
                if (length > rankUp)
                {
                    rankUp = length;
                }
            }
            return rankUp;
        }

        public int GetRankDown()
        {
            //rankDown = 0;
            if (senders.Any())
            {
                foreach (KeyValuePair<Task, int> sender in senders)
                {
                    if (Math.Max(rankDown, sender.Key.GetRankDown() + sender.Value + sender.Key.Length) > rankDown)
                    {
                        rankDown = Math.Max(rankDown, sender.Key.GetRankDown() + sender.Value + sender.Key.Length);
                    }
                }
            }
            return rankDown;
        }

        public int GetFirstAvailableTick()
        {
            if (senders.Any())
            {
                int tick = 0;
                foreach (KeyValuePair<Task, int> sender in senders)
                {
                    tick = Math.Max(tick, sender.Key.Processor.CurrentLastTick);
                }
                return tick;
            }
            else
            {
                return 0;
            }
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

        public int RankUp
        {
            get { return this.rankUp; }
            set { this.rankUp = value; }
        }

        public int RankDown
        {
            get { return this.rankDown; }
            set { this.rankDown = value; }
        }

        public Processor Processor
        {
            get { return this.processor; }
            set { this.processor = value; }
        }

        public void Print()
        {
            Console.Write("\nTask number:" + id + " \nSenders: ");
            foreach (KeyValuePair<Task, int> sender in senders)
            {
                Console.Write("[ID: " + sender.Key.ID + " Communication length: " + sender.Value + "] ");
            }
            Console.Write("\nReceivers: ");
            foreach (KeyValuePair<Task, int> receiver in receivers)
            {
                Console.Write("[ID: " + receiver.Key.ID + " Communication length: " + receiver.Value + "] ");
            }
            Console.Write("\nTask length: " + length);
            Console.Write("\nRankUp: " + rankUp);
            Console.Write("\nRankDown: " + rankDown + "\n");
        }

    }
}
