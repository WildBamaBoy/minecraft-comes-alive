package mca.resources;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

import mca.MCA;
import mca.resources.data.Question;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class Dialogues extends JsonDataLoader {
    protected static final Identifier ID = new Identifier(MCA.MOD_ID, "dialogues");

    private static Dialogues INSTANCE;

    public static final Dialogues getInstance() {
        return INSTANCE;
    }

    private final Map<Identifier, Question> questions = new HashMap<>();
    private final Map<String, List<Question>> questionGroups = new HashMap<>();

    public Dialogues() {
        super(Resources.GSON, "dialogues");
        INSTANCE = this;
    }

    public Dialogues(Gson gson, String dataType) {
        super(gson, dataType);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        questions.clear();
        data.forEach(this::loadDialogue);
    }

    private void loadDialogue(Identifier id, JsonElement element) {
        Question[] questions = Resources.GSON.fromJson(element, Question[].class);

        for (Question q : questions) {
            this.questions.put(new Identifier(id.toString() + "." + q.getName()), q);
            if (q.getGroup() != null) {
                this.questionGroups.computeIfAbsent(q.getGroup(), x -> new LinkedList<>());
                this.questionGroups.get(q.getGroup()).add(q);
            }
        }
    }

    public Question getQuestion(Identifier i) {
        return questions.get(i);
    }
}
