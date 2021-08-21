package mca.resources.data;

import java.util.List;

public class Question {
    private final String name;
    private final String group;
    private final List<Answer> answers;
    private final boolean closeScreen;

    public Question(String name, String group, List<Answer> answers, boolean screen) {
        this.name = name;
        this.group = group;
        this.answers = answers;
        closeScreen = screen;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public List<Answer> getAnswers() {
        return answers;
    }
}
