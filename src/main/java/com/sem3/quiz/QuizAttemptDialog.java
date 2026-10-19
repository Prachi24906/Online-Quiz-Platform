package com.sem3.quiz;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.*;

/** A simple timed quiz window. A background scheduled task updates the countdown. */
public class QuizAttemptDialog extends JDialog {
    private final QuizDAO dao;
    private final User participant;
    private final Quiz quiz;
    private final List<Question> questions;
    private final ButtonGroup choices = new ButtonGroup();
    private final JRadioButton[] radios = new JRadioButton[4];
    private final JLabel questionLabel = new JLabel();
    private final JLabel timerLabel = new JLabel();
    private final JButton nextButton = new JButton("Next question");
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private int index = 0, secondsLeft, secondsUsed = 0;
    private final String[] answers;
    private boolean submitted = false;

    public QuizAttemptDialog(JFrame parent, QuizDAO dao, User participant, Quiz quiz, List<Question> questions) {
        super(parent,"Timed Quiz: "+quiz.getTitle(),true);
        this.dao=dao;this.participant=participant;this.quiz=quiz;this.questions=questions;
        this.answers=new String[questions.size()];this.secondsLeft=quiz.getDuration()*60;
        setSize(650,430);setLocationRelativeTo(parent);setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        JPanel main=new JPanel(new BorderLayout(12,12));main.setBorder(BorderFactory.createEmptyBorder(20,22,20,22));main.setBackground(Color.WHITE);
        JPanel top=new JPanel(new BorderLayout());top.setOpaque(false);
        JLabel title=new JLabel(quiz.getTitle());title.setFont(new Font("SansSerif",Font.BOLD,20));timerLabel.setForeground(new Color(220,38,38));top.add(title,BorderLayout.WEST);top.add(timerLabel,BorderLayout.EAST);
        main.add(top,BorderLayout.NORTH);
        JPanel middle=new JPanel();middle.setLayout(new BoxLayout(middle,BoxLayout.Y_AXIS));middle.setOpaque(false);
        questionLabel.setFont(new Font("SansSerif",Font.BOLD,16));middle.add(questionLabel);middle.add(Box.createVerticalStrut(16));
        String[] labels={"A","B","C","D"};
        for(int i=0;i<4;i++){radios[i]=new JRadioButton();radios[i].setOpaque(false);choices.add(radios[i]);middle.add(radios[i]);}
        main.add(middle,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));bottom.setOpaque(false);
        JButton submit=new JButton("Submit quiz");bottom.add(nextButton);bottom.add(submit);main.add(bottom,BorderLayout.SOUTH);
        nextButton.addActionListener(e->saveAnswerAndNext());submit.addActionListener(e->finishQuiz());
        addWindowListener(new java.awt.event.WindowAdapter(){@Override public void windowClosing(java.awt.event.WindowEvent e){
            int answer=JOptionPane.showConfirmDialog(QuizAttemptDialog.this,"Submit this quiz now?","Finish quiz",JOptionPane.YES_NO_OPTION);
            if(answer==JOptionPane.YES_OPTION)finishQuiz();
        }});
        add(main);showQuestion();
        // Swing UI updates must be placed on the Event Dispatch Thread.
        timer.scheduleAtFixedRate(()->SwingUtilities.invokeLater(()->{
            if(submitted)return;secondsLeft--;secondsUsed++;
            timerLabel.setText("Time left: "+(secondsLeft/60)+":"+String.format("%02d",secondsLeft%60));
            if(secondsLeft<=0)finishQuiz();
        }),1,1,TimeUnit.SECONDS);
    }

    private void showQuestion(){
        Question q=questions.get(index);questionLabel.setText("<html>Question "+(index+1)+" of "+questions.size()+": "+q.getText()+"</html>");
        String[] options={q.getA(),q.getB(),q.getC(),q.getD()};
        choices.clearSelection();
        for(int i=0;i<4;i++){radios[i].setText((char)('A'+i)+". "+options[i]);radios[i].setSelected(((char)('A'+i)+"").equals(answers[index]));}
        nextButton.setEnabled(index<questions.size()-1);nextButton.setText(index==questions.size()-1?"Last question":"Next question");
    }

    private void saveAnswerAndNext(){
        for(int i=0;i<4;i++)if(radios[i].isSelected())answers[index]=String.valueOf((char)('A'+i));
        if(index<questions.size()-1){index++;showQuestion();}
    }

    private void finishQuiz(){
        if(submitted)return;
        // Save the current answer too, including when Submit is clicked on the last question.
        for(int i=0;i<4;i++)if(radios[i].isSelected())answers[index]=String.valueOf((char)('A'+i));
        submitted=true;timer.shutdownNow();
        int score=0;for(int i=0;i<questions.size();i++)if(questions.get(i).getCorrect().equals(answers[i]))score++;
        try{dao.saveAttempt(quiz.getId(),participant.getId(),score,questions.size(),secondsUsed);}
        catch(Exception ex){JOptionPane.showMessageDialog(this,"Could not save result: "+ex.getMessage());}
        // Build question-by-question feedback after saving the overall score.
        StringBuilder review = new StringBuilder();
        review.append("Quiz submitted!\nScore: ").append(score).append(" / ").append(questions.size())
              .append("\nPercentage: ").append(score * 100 / questions.size()).append("%\n\nDetailed review\n");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            review.append("\n").append(i + 1).append(". ").append(q.getText()).append("\n")
                  .append("Your answer: ").append(answers[i] == null ? "Not answered" : answers[i]).append("\n")
                  .append("Correct answer: ").append(q.getCorrect()).append("\n")
                  .append(answers[i] != null && q.getCorrect().equals(answers[i]) ? "Result: Correct\n" : "Result: Incorrect\n");
        }
        JTextArea reviewArea = new JTextArea(review.toString(), 18, 52);
        reviewArea.setEditable(false);
        reviewArea.setLineWrap(true);
        reviewArea.setWrapStyleWord(true);
        reviewArea.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(reviewArea), "Quiz result and feedback", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
