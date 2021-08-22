package mca.resources.data;

import java.util.List;

public class Question {
    private final String id;
    private final List<Answer> answers;
    private final boolean closeScreen;

    public Question(String id, List<Answer> answers, boolean screen) {
        this.id = id;
        this.answers = answers;
        closeScreen = screen;
    }

    public String getId() {
        return id;
    }

    public List<Answer> getAnswers() {
        return answers;
    }
}
