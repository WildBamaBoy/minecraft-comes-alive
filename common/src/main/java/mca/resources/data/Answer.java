package mca.resources.data;

import java.util.List;
import mca.entity.interaction.InteractionPredicate;

public class Answer {
    private final String name;

    private final List<InteractionPredicate> conditions;

    private final List<AnswerAction> next;

    public Answer(String name, List<InteractionPredicate> conditions, List<AnswerAction> next) {
        this.name = name;
        this.conditions = conditions;
        this.next = next;
    }

    public String getName() {
        return name;
    }

    public List<InteractionPredicate> getConditions() {
        return conditions;
    }

    public List<AnswerAction> getNext() {
        return next;
    }
}
