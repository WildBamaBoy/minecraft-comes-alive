package mca.resources.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import mca.client.gui.Constraint;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.player.PlayerEntity;

public class Question {
    private final String id;
    private final String group;
    private final List<Answer> answers;
    private final boolean auto;
    private final boolean silent;

    public Question(String id, String group, List<Answer> answers, boolean auto, boolean silent) {
        this.id = id;
        this.group = group;
        this.answers = answers;
        this.auto = auto;
        this.silent = silent;
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

    public static String getTranslationKey(String question) {
        return "dialogue." + question;
    }

    public static String getTranslationKey(String question, String answer) {
        return "dialogue." + question + "." + answer;
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

    public boolean isSilent() {
        return silent;
    }
}
