package com.sem3.quiz;

/** One multiple-choice question and its four answer options. */
public class Question {
    private final int id;
    private final String text, a, b, c, d, correct;

    public Question(int id, String text, String a, String b, String c, String d, String correct) {
        this.id=id; this.text=text; this.a=a; this.b=b; this.c=c; this.d=d; this.correct=correct;
    }
    public int getId() { return id; }
    public String getText() { return text; }
    public String getA() { return a; }
    public String getB() { return b; }
    public String getC() { return c; }
    public String getD() { return d; }
    public String getCorrect() { return correct; }
    public String option(String letter) {
        switch (letter) {
            case "A": return a; case "B": return b; case "C": return c; case "D": return d;
            default: return "";
        }
    }
}
