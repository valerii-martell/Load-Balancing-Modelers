package com.commbus.planner.model;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by KirinTor on 20.12.2017.
 *
 * Допоміжний клас для згортування списку вершин
 * Використовується для збереження списку в форматі XML
 */
@XmlRootElement(name = "nodes")
@XmlSeeAlso({SimpleNodeProperty.class})
public class NodeListWrapper{

        private List nodes;

        @XmlElement(name = "node", nillable = true, required = false)
        public List getNodes() {
            return this.nodes;
        }

        public void setNodes(List nodes) {
            this.nodes = nodes;
        }
}
