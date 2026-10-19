package com.sem3.quiz;

/** Holds the basic details of one quiz. */
public class Quiz {
    private final int id;
    private final String title;
    private final String description;
    private final int duration;
    private final String status;

    public Quiz(int id, String title, String description, int duration, String status) {
        this.id = id; this.title = title; this.description = description;
        this.duration = duration; this.status = status;
    }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getDuration() { return duration; }
    public String getStatus() { return status; }
    @Override public String toString() { return title + " (" + duration + " min)"; }
}
