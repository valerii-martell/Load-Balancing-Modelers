package com.commbus.planner.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by KirinTor on 20.12.2017.
 *
 * Відображати в таблиці дані звичайних типів неможливо
 * Тому необхідний проміжний буфер між вершиною model та вершиною view
 */
@XmlType(name="SimpleNodeProperty")
public class SimpleNodeProperty {

    private final StringProperty id;
    private final StringProperty rank;
    private final StringProperty dependencies;
    private final StringProperty communicationsLengths;

    public SimpleNodeProperty() {
        this(null, null, null, null);
    }

    public SimpleNodeProperty(String id, String rank, String dependencies, String communicationsLengths) {
        this.id = new SimpleStringProperty(id);
        this.rank = new SimpleStringProperty(rank);
        this.dependencies = new SimpleStringProperty(dependencies);
        this.communicationsLengths = new SimpleStringProperty(communicationsLengths);


    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getRank() {
        return rank.get();
    }

    public StringProperty rankProperty() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank.set(rank);
    }

    public String getDependencies() {
        return dependencies.get();
    }

    public StringProperty dependenciesProperty() {
        return dependencies;
    }

    public void setDependencies(String dependencies) {
        this.dependencies.set(dependencies);
    }

    public String getCommunicationsLengths() {
        return communicationsLengths.get();
    }

    public StringProperty communicationsLengthsProperty() {
        return communicationsLengths;
    }

    public void setCommunicationsLengths(String communicationsLengths) {
        this.communicationsLengths.set(communicationsLengths);
    }
}
