package mca.resources.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import mca.client.gui.Constraint;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

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

    public String getTranslationKey(String answer) {
        return "dialogue." + getId() + "." + answer;
    }

    public boolean isAuto() {
        return auto;
    }

    public List<String> getValidAnswers(PlayerEntity player, VillagerEntityMCA villager) {
        Set<Constraint> constraints = Constraint.allMatching(villager, player);
        List<String> ans = new LinkedList<>();
        for (Answer a : answers) {
            if (a.isValidForConstraint(constraints)) {
                ans.add(a.getName());
            }
        }
        return ans;
    }
}
