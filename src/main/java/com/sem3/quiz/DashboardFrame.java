package com.sem3.quiz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Role-based dashboard. Each role sees the actions relevant to them. */
public class DashboardFrame extends JFrame {
    private final User user;
    private final QuizDAO dao = new QuizDAO();
    private final JPanel content = new JPanel(new BorderLayout(12,12));

    public DashboardFrame(User user) {
        this.user=user;
        setTitle("Java Quiz Platform | "+user.getRole());
        setSize(1050,680); setMinimumSize(new Dimension(850,560)); setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(245,248,252));
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(15,22,15,22));
        JLabel brand = new JLabel("Java Quiz Platform"); brand.setFont(new Font("SansSerif",Font.BOLD,22)); brand.setForeground(new Color(37,99,235));
        JLabel who = new JLabel(user.getName()+"  |  "+user.getRole()); who.setForeground(new Color(71,85,105));
        top.add(brand,BorderLayout.WEST); top.add(who,BorderLayout.EAST); add(top,BorderLayout.NORTH);
        JPanel side = new JPanel(); side.setLayout(new BoxLayout(side,BoxLayout.Y_AXIS)); side.setBackground(new Color(239,246,255));
        side.setBorder(BorderFactory.createEmptyBorder(18,12,18,12)); side.setPreferredSize(new Dimension(205,10));
        addNav(side,"Overview",this::showOverview);
        if (user.getRole().equals("ADMIN")) {
            addNav(side,"User Management",this::showUsers);
            addNav(side,"Quiz Approval",this::showApprovals);
            addNav(side,"Performance Reports",this::showAllResults);
            addNav(side,"System Settings",this::showSettings);
            addNav(side,"System Alerts",this::showAlerts);
        } else if (user.getRole().equals("CREATOR")) {
            addNav(side,"Create Quiz",this::createQuiz);
            addNav(side,"My Quiz History",this::showMyQuizzes);
            addNav(side,"Quiz Results",this::showAllResults);
            addNav(side,"Participant Messages",this::showMessages);
        } else {
            addNav(side,"Available Quizzes",this::showAvailableQuizzes);
            addNav(side,"My Performance",this::showMyResults);
            addNav(side,"Leaderboard",this::showLeaderboard);
            addNav(side,"Messages",this::showMessages);
            addNav(side,"Quiz Reminders",this::showReminders);
        }
        side.add(Box.createVerticalGlue()); addNav(side,"Log out",() -> { dispose(); new LoginFrame().setVisible(true); });
        add(side,BorderLayout.WEST);
        content.setBackground(new Color(245,248,252)); content.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        add(content,BorderLayout.CENTER); showOverview();
    }

    private void addNav(JPanel panel, String text, Runnable action) {
        JButton b = new JButton(text); b.setAlignmentX(Component.LEFT_ALIGNMENT); b.setMaximumSize(new Dimension(180,38));
        b.setFocusPainted(false); b.setBackground(Color.WHITE); b.setForeground(new Color(30,64,175));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(219,234,254)),
            BorderFactory.createEmptyBorder(8,10,8,10)));
        b.addActionListener(e -> action.run()); panel.add(b); panel.add(Box.createVerticalStrut(8));
    }

    private void heading(String title, String description) {
        content.removeAll();
        JPanel p = new JPanel(new BorderLayout(4,4)); p.setOpaque(false);
        JLabel h = new JLabel(title); h.setFont(new Font("SansSerif",Font.BOLD,24)); h.setForeground(new Color(30,41,59));
        JLabel d = new JLabel(description); d.setForeground(new Color(100,116,139));
        p.add(h,BorderLayout.NORTH); p.add(d,BorderLayout.CENTER); content.add(p,BorderLayout.NORTH);
    }

    private void showOverview() {
        heading("Welcome, "+user.getName(), "Your quiz workspace at a glance.");
        JPanel cards = new JPanel(new GridLayout(2,2,14,14)); cards.setOpaque(false);
        String[][] items = user.getRole().equals("ADMIN")
            ? new String[][]{{"Manage users","Create and review user accounts"},{"Review quizzes","Approve or reject creator submissions"},{"Performance","View quiz attempt results"},{"Settings","Update basic platform settings"}}
            : user.getRole().equals("CREATOR")
            ? new String[][]{{"Create a quiz","Add multiple-choice questions"},{"Quiz history","Track pending and approved quizzes"},{"Results","Review participant scores"},{"Messages","Communicate with participants"}}
            : new String[][]{{"Take a quiz","Attempt approved Java quizzes"},{"Performance report","Review your previous scores"},{"Leaderboard","Compare total points"},{"Messages","Contact quiz creators"}};
        for (String[] item : items) {
            JPanel c = new JPanel(new BorderLayout(6,6)); c.setBackground(Color.WHITE);
            c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226,232,240)),BorderFactory.createEmptyBorder(20,20,20,20)));
            JLabel t = new JLabel(item[0]); t.setFont(new Font("SansSerif",Font.BOLD,18)); t.setForeground(new Color(37,99,235));
            c.add(t,BorderLayout.NORTH); c.add(new JLabel("<html>"+item[1]+"</html>"),BorderLayout.CENTER); cards.add(c);
        }
        content.add(cards,BorderLayout.CENTER); refresh();
    }

    private void showUsers() {
        heading("User Management","Add user accounts and review platform roles.");
        JPanel body = new JPanel(new BorderLayout(8,8)); body.setOpaque(false);
        body.add(tablePanel(new String[]{"ID","Name","Email","Role"}, safeUsers()),BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add user");
        JButton edit = new JButton("Edit selected");
        JButton delete = new JButton("Delete selected");
        actions.add(add); actions.add(edit); actions.add(delete);
        body.add(actions,BorderLayout.SOUTH);
        add.addActionListener(e -> {
            JTextField name=new JTextField(), mail=new JTextField(), pass=new JTextField();
            JComboBox<String> role=new JComboBox<>(new String[]{"CREATOR","PARTICIPANT","ADMIN"});
            Object[] form={"Name",name,"Email",mail,"Temporary password",pass,"Role",role};
            if(JOptionPane.showConfirmDialog(this,form,"Create user",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
                try { dao.addUser(name.getText().trim(),mail.getText().trim(),pass.getText(),(String)role.getSelectedItem());
                    showUsers(); JOptionPane.showMessageDialog(this,"User created successfully.");
                } catch(Exception ex) { error(ex); }
            }
        });
        edit.addActionListener(e -> {
            try {
                List<String[]> users = dao.getUsers();
                String chosen = (String) JOptionPane.showInputDialog(this, "Select a user to edit", "Edit user",
                    JOptionPane.PLAIN_MESSAGE, null,
                    users.stream().map(u -> u[0] + " | " + u[1] + " | " + u[2] + " | " + u[3]).toArray(String[]::new), null);
                if (chosen == null) return;
                int id = Integer.parseInt(chosen.split("\\|")[0].trim());
                String[] selected = users.stream().filter(u -> Integer.parseInt(u[0]) == id).findFirst().orElseThrow();
                JTextField name = new JTextField(selected[1]);
                JTextField mail = new JTextField(selected[2]);
                JPasswordField pass = new JPasswordField();
                JComboBox<String> role = new JComboBox<>(new String[]{"ADMIN", "CREATOR", "PARTICIPANT"});
                role.setSelectedItem(selected[3]);
                Object[] form = {"Name", name, "Email", mail, "New password (leave blank to keep current)", pass, "Role", role};
                if (JOptionPane.showConfirmDialog(this, form, "Edit user", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    dao.updateUser(id, name.getText().trim(), mail.getText().trim(), new String(pass.getPassword()), (String) role.getSelectedItem());
                    showUsers(); JOptionPane.showMessageDialog(this, "User updated.");
                }
            } catch (Exception ex) { error(ex); }
        });
        delete.addActionListener(e -> {
            try {
                List<String[]> users = dao.getUsers();
                String chosen = (String) JOptionPane.showInputDialog(this, "Select a user to delete", "Delete user",
                    JOptionPane.WARNING_MESSAGE, null,
                    users.stream().map(u -> u[0] + " | " + u[1] + " | " + u[2] + " | " + u[3]).toArray(String[]::new), null);
                if (chosen == null) return;
                int id = Integer.parseInt(chosen.split("\\|")[0].trim());
                if (id == user.getId()) { JOptionPane.showMessageDialog(this, "You cannot delete the account currently signed in."); return; }
                if (JOptionPane.showConfirmDialog(this, "Delete this user? Existing quiz/attempt/message records may prevent deletion.", "Confirm delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    dao.deleteUser(id); showUsers(); JOptionPane.showMessageDialog(this, "User deleted.");
                }
            } catch (Exception ex) { error(ex); }
        });
        content.add(body,BorderLayout.CENTER); refresh();
    }

    private List<String[]> safeUsers() { try { return dao.getUsers(); } catch(Exception ex) { error(ex); return new ArrayList<>(); } }

    private void showApprovals() {
        heading("Quiz Content Approval","Approve or reject quizzes submitted by creators.");
        try {
            List<Quiz> quizzes=dao.getQuizzes("ADMIN",user.getId(),"PENDING");
            String[] cols={"ID","Title","Description","Duration","Status"};
            Object[][] data=new Object[quizzes.size()][5];
            for(int i=0;i<quizzes.size();i++){Quiz q=quizzes.get(i);data[i]=new Object[]{q.getId(),q.getTitle(),q.getDescription(),q.getDuration()+" min",q.getStatus()};}
            JTable table=new JTable(data,cols); JPanel p=new JPanel(new BorderLayout(8,8)); p.add(new JScrollPane(table),BorderLayout.CENTER);
            JPanel actions=new JPanel(); JButton approve=new JButton("Approve selected"), reject=new JButton("Reject selected");
            approve.addActionListener(e -> changeStatus(table,"APPROVED")); reject.addActionListener(e -> changeStatus(table,"REJECTED"));
            actions.add(approve);actions.add(reject);p.add(actions,BorderLayout.SOUTH);p.setOpaque(false);content.add(p,BorderLayout.CENTER);
        } catch(Exception ex){error(ex);}
        refresh();
    }

    private void changeStatus(JTable table,String status) {
        int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select a quiz first.");return;}
        try {dao.updateQuizStatus(Integer.parseInt(table.getValueAt(row,0).toString()),status);showApprovals();}
        catch(Exception ex){error(ex);}
    }

    private void createQuiz() {
        JTextField title=new JTextField(), desc=new JTextField(), duration=new JTextField("10");
        JTextField count=new JTextField("3");
        Object[] basics={"Quiz title",title,"Description",desc,"Duration in minutes",duration,"Number of questions (1-20)",count};
        if(JOptionPane.showConfirmDialog(this,basics,"New quiz",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try {
            int minutes=Integer.parseInt(duration.getText().trim()), n=Integer.parseInt(count.getText().trim());
            if(title.getText().trim().isEmpty()||minutes<1||n<1||n>20)throw new IllegalArgumentException("Enter a title, positive duration, and 1–20 questions.");
            List<Question> questions=new ArrayList<>();
            for(int i=1;i<=n;i++){
                JTextField q=new JTextField(),a=new JTextField(),b=new JTextField(),c=new JTextField(),d=new JTextField();
                JComboBox<String> correct=new JComboBox<>(new String[]{"A","B","C","D"});
                Object[] fields={"Question "+i,q,"Option A",a,"Option B",b,"Option C",c,"Option D",d,"Correct option",correct};
                if(JOptionPane.showConfirmDialog(this,fields,"Question "+i,JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
                if(q.getText().trim().isEmpty()||a.getText().trim().isEmpty()||b.getText().trim().isEmpty()||c.getText().trim().isEmpty()||d.getText().trim().isEmpty())
                    throw new IllegalArgumentException("Question and all four options are required.");
                questions.add(new Question(0,q.getText().trim(),a.getText().trim(),b.getText().trim(),c.getText().trim(),d.getText().trim(),(String)correct.getSelectedItem()));
            }
            dao.createQuiz(title.getText().trim(),desc.getText().trim(),minutes,user.getId(),questions);
            JOptionPane.showMessageDialog(this,"Quiz saved as PENDING. An admin must approve it before participants can take it.");
            showMyQuizzes();
        }catch(Exception ex){error(ex);}
    }

    private void showMyQuizzes() {
        heading("My Quiz History","Your quizzes and their approval status.");
        try {List<Quiz> qs=dao.getQuizzes("CREATOR",user.getId(),null);Object[][] data=new Object[qs.size()][4];
            for(int i=0;i<qs.size();i++){Quiz q=qs.get(i);data[i]=new Object[]{q.getId(),q.getTitle(),q.getDuration()+" min",q.getStatus()};}
            content.add(new JScrollPane(new JTable(data,new String[]{"ID","Title","Duration","Status"})),BorderLayout.CENTER);
        }catch(Exception ex){error(ex);} refresh();
    }

    private void showAvailableQuizzes() {
        heading("Available Quizzes","Choose an approved quiz to start a timed attempt.");
        try {
            List<Quiz> qs=dao.getQuizzes("PARTICIPANT",user.getId(),null);
            String[] cols={"ID","Title","Description","Duration"};Object[][] data=new Object[qs.size()][4];
            for(int i=0;i<qs.size();i++){Quiz q=qs.get(i);data[i]=new Object[]{q.getId(),q.getTitle(),q.getDescription(),q.getDuration()+" min"};}
            JTable table=new JTable(data,cols);JPanel p=new JPanel(new BorderLayout(8,8));p.add(new JScrollPane(table),BorderLayout.CENTER);
            JButton start=new JButton("Start selected quiz");start.addActionListener(e->{int row=table.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Select a quiz first.");return;}
                Quiz selected=qs.get(row);try{List<Question> questions=dao.getQuestions(selected.getId());if(questions.isEmpty()){JOptionPane.showMessageDialog(this,"This quiz has no questions.");return;}
                    new QuizAttemptDialog(this,dao,user,selected,questions).setVisible(true);showMyResults();
                }catch(Exception ex){error(ex);}});
            p.add(start,BorderLayout.SOUTH);p.setOpaque(false);content.add(p,BorderLayout.CENTER);
        }catch(Exception ex){error(ex);}refresh();
    }

    private void showMyResults(){showResults(false);}
    private void showAllResults(){showResults(true);}
    private void showResults(boolean all) {
        heading(all?"Performance Reports":"My Performance Report",all?"Review recorded quiz scores.":"Your completed quiz attempts and scores.");
        try {List<String[]> rows=dao.getAttempts(user.getId(),all);String[] cols=all?new String[]{"Quiz","Participant","Score","Attempted at"}:new String[]{"Quiz","Participant","Score","Attempted at"};
            content.add(tablePanel(cols,rows),BorderLayout.CENTER);
            if(rows.isEmpty()) content.add(new JLabel("No quiz attempts have been recorded yet."),BorderLayout.SOUTH);
        }catch(Exception ex){error(ex);}refresh();
    }

    private void showLeaderboard() {
        heading("Leaderboard","Rankings are based on total correct answers across completed quizzes.");
        try {List<String[]> rows=dao.getLeaderboard();Object[][] data=new Object[rows.size()][4];
            for(int i=0;i<rows.size();i++)data[i]=new Object[]{i+1,rows.get(i)[0],rows.get(i)[1],rows.get(i)[2]};
            content.add(new JScrollPane(new JTable(data,new String[]{"Rank","Participant","Points","Questions attempted"})),BorderLayout.CENTER);
        }catch(Exception ex){error(ex);}refresh();
    }

    private void showReminders() {
        heading("Quiz Reminders", "Save reminders for quizzes you want to complete or review.");
        JPanel panel = new JPanel(new BorderLayout(10, 10)); panel.setOpaque(false);
        JTextField reminder = new JTextField();
        JButton add = new JButton("Add reminder");
        JPanel form = new JPanel(new BorderLayout(8, 8)); form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Reminder (for example: Revise Java Basics before Friday)"), BorderLayout.NORTH);
        form.add(reminder, BorderLayout.CENTER); form.add(add, BorderLayout.EAST);
        try { panel.add(tablePanel(new String[]{"Reminder", "Created at"}, dao.getReminders(user.getId())), BorderLayout.CENTER); }
        catch (Exception ex) { error(ex); }
        panel.add(form, BorderLayout.SOUTH);
        add.addActionListener(e -> {
            String text = reminder.getText().trim();
            if (text.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter a reminder first."); return; }
            try { dao.addReminder(user.getId(), text); showReminders(); }
            catch (Exception ex) { error(ex); }
        });
        content.add(panel, BorderLayout.CENTER); refresh();
    }

    private void showMessages() {
        heading("Messages","Send a short message to another registered user.");
        JPanel p=new JPanel(new BorderLayout(8,8));p.setOpaque(false);
        try {p.add(tablePanel(new String[]{"From","To","Message","Sent at"},dao.getMessages(user.getId())),BorderLayout.CENTER);}
        catch(Exception ex){error(ex);}
        JPanel form=new JPanel(new FlowLayout(FlowLayout.LEFT));form.setBackground(Color.WHITE);
        JTextField receiver=new JTextField("1",5), message=new JTextField(28);JButton send=new JButton("Send");
        form.add(new JLabel("Receiver user ID"));form.add(receiver);form.add(message);form.add(send);
        send.addActionListener(e->{try{dao.sendMessage(user.getId(),Integer.parseInt(receiver.getText().trim()),message.getText().trim());showMessages();}
            catch(Exception ex){error(ex);}});
        p.add(form,BorderLayout.SOUTH);content.add(p,BorderLayout.CENTER);refresh();
    }

    private void showSettings() {
        heading("System Settings","Update basic platform configuration.");
        JPanel form=new JPanel(new GridLayout(3,2,10,10));form.setBackground(Color.WHITE);form.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JTextField name=new JTextField("Java Quiz Platform"),duration=new JTextField("10");
        form.add(new JLabel("Platform name"));form.add(name);form.add(new JLabel("Default quiz duration (minutes)"));form.add(duration);
        JButton save=new JButton("Save settings");form.add(new JLabel(""));form.add(save);
        save.addActionListener(e->{try{dao.saveSetting("platform_name",name.getText().trim());dao.saveSetting("default_duration_minutes",duration.getText().trim());JOptionPane.showMessageDialog(this,"Settings saved.");}
            catch(Exception ex){error(ex);}});
        content.add(form,BorderLayout.CENTER);refresh();
    }

    private void showAlerts() {
        heading("System Alerts","Basic operational reminders.");
        JTextArea alerts=new JTextArea("• Approve quizzes before participants can access them.\n\n• Make sure MySQL is running before starting the app.\n\n• Use demo accounts only for classroom testing.\n\n• Back up the database before making changes.");
        alerts.setEditable(false);alerts.setFont(new Font("SansSerif",Font.PLAIN,16));alerts.setBackground(Color.WHITE);alerts.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        content.add(alerts,BorderLayout.CENTER);refresh();
    }

    private JPanel tablePanel(String[] columns,List<String[]> rows) {
        Object[][] data=new Object[rows.size()][columns.length];for(int i=0;i<rows.size();i++)data[i]=rows.get(i);
        JTable table=new JTable(new DefaultTableModel(data,columns){@Override public boolean isCellEditable(int r,int c){return false;}});
        table.setRowHeight(26);JPanel p=new JPanel(new BorderLayout());p.add(new JScrollPane(table),BorderLayout.CENTER);p.setOpaque(false);return p;
    }
    private void error(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Something went wrong",JOptionPane.ERROR_MESSAGE);}
    private void refresh(){content.revalidate();content.repaint();}
}
