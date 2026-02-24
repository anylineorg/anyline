package org.anyline.entity.geometry;

public class Rectangle {
    private Point start;
    private Point end;
    public Rectangle(Point start, Point end) {
        this.start = start;
        this.end = end;
    }
    public Point getStart() {
        return start;
    }
    public Point getEnd() {
        return end;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public void setEnd(Point end) {
        this.end = end;
    }
}
