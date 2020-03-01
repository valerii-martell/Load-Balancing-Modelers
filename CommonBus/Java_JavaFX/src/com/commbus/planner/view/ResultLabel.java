package com.commbus.planner.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Created by KirinTor on 20.12.2017.
 *
 * Клас для швидкого задання спеціальних заголовків
 */
public class ResultLabel extends Label {

    public ResultLabel(String text, String color, boolean isBold, int height, int width){
        super(text);
        setSize(height, width);
        setAlignment (Pos.CENTER);
        setStyle("-fx-background-color:"+color);
        if (isBold){
            setFont(Font.font("Roboto Medium", FontWeight.BOLD,14));
        }else{
            setFont(Font.font("Roboto Medium",14));
        }
    }

    public ResultLabel(String text, int height, int width){
        super(text);
        setSize(height, width);
        setAlignment (Pos.CENTER);
        setTextFill(Paint.valueOf("#646464"));
    }

    public void setSize(int height, int width){
        this.setMaxHeight(height);
        this.setPrefHeight(height);
        this.setMinHeight(height);
        this.setMaxWidth(width);
        this.setPrefWidth(width);
        this.setMinWidth(width);
    }

    public void setColor(String color){
        setStyle("-fx-background-color:"+color);
    }

}
