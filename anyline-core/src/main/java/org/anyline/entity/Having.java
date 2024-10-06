package org.anyline.entity;

public class Having {
    private Aggregation aggregation;
    private String column;
    private Compare compare;
    private Object value;
    private String text;
    public Having(){}
    public Having(String text){
        this.text = text;
    }
    public String text() {
        return text;
    }
    public Having text(String text) {
        this.text = text;
        return this;
    }
    public Aggregation aggregation() {
        return aggregation;
    }

    public Having aggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
        return this;
    }

    public String column() {
        return column;
    }

    public Having column(String column) {
        this.column = column;
        return this;
    }

    public Compare compare() {
        return compare;
    }

    public Having compare(Compare compare) {
        this.compare = compare;
        return this;
    }

    public Object value() {
        return value;
    }

    public Having value(Object value) {
        this.value = value;
        return this;
    }
}
