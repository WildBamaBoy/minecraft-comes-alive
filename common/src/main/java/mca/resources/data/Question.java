package mca.resources.data;

import java.util.List;

public class Question {
    private final String id;
    private final String group;
    private final List<Answer> answers;
    private final boolean auto;

    public Question(String id, String group, List<Answer> answers, boolean auto) {
        this.id = id;
        this.group = group;
        this.answers = answers;
        this.auto = auto;
    }

    public String getId() {
        return id;
    }

    public String getGroup() {
        return group == null ? id : group;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public boolean isCloseScreen() {
        return answers == null;
    }

    public Answer getAnswer(String answer) {
        for (Answer a : answers) {
            if (a.getName().equals(answer)) {
                return a;
            }
        }
        return null;
    }

    public String getTranslationKey() {
        return "dialogue." + getId();
    }

    public boolean isAuto() {
        return auto;
    }
}
