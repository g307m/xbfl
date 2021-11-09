package xyz.grantlmul.xmcl;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Dimensions {
    public StringProperty h = new SimpleStringProperty();
    public StringProperty v = new SimpleStringProperty();
    public Dimensions(int h, int v) {
        this.h.setValue(String.valueOf(h));
        this.v.setValue(String.valueOf(v));
    }
}
