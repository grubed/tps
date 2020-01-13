package com.test.tps.common;


public class Edge {

    private Integer start;
    private Integer end;
    private Integer weight;

    public Edge(Integer start, Integer end, Integer weight) {
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
