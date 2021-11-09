package xyz.grantlmul.xmcl;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.LinkedList;
import java.util.UUID;

public class UuidProperty extends ObjectProperty<UUID> {
    private String name;
    private UUID value;
    private ObservableValue<? extends UUID> observing;
    ChangeListener<? super UUID> changeListener = new ChangeListener<UUID>() {
        @Override
        public void changed(ObservableValue<? extends UUID> observable, UUID oldValue, UUID newValue) {
            set(newValue);
        }
    };
    @Override
    public void bind(ObservableValue<? extends UUID> observable) {
        this.observing = observable;
        if (observable != null) {
            observable.addListener(changeListener);
        }
    }

    @Override
    public void unbind() {
        this.observing.removeListener(changeListener);
    }

    @Override
    public boolean isBound() {
        return observing != null;
    }

    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID get() {
        return value;
    }

    @Override
    public void set(UUID value) {
        changeListeners.forEach(cl -> cl.changed(this, this.value, value));
        this.value = value;
    }

    LinkedList<ChangeListener> changeListeners = new LinkedList<>();
    @Override
    public void addListener(ChangeListener<? super UUID> listener) {

    }

    @Override
    public void removeListener(ChangeListener<? super UUID> listener) {

    }

    LinkedList<InvalidationListener> invalidationListeners = new LinkedList<>();
    @Override
    public void addListener(InvalidationListener listener) {
        invalidationListeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        invalidationListeners.remove(listener);
    }
}
