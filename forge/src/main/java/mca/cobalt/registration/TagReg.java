package mca.cobalt.registration;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.tag.Tag;
import net.minecraft.tag.Tag.Identified;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;

final class TagReg<T> implements Identified<T> {

    private final Identifier id;
    private final Supplier<TagGroup<T>> container;

    private volatile Target<T> target;

    public TagReg(Identifier id, Supplier<TagGroup<T>> container) {
        this.id = id;
        this.container = container;
    }

    @Override
    public boolean contains(T t) {
        return getTag().contains(t);
    }

    @Override
    public List<T> values() {
        return getTag().values();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    private Tag<T> getTag() {
        Target<T> target = this.target;
        TagGroup<T> reqContainer = container.get();
        Tag<T> ret;

        if (target == null || target.container != reqContainer) {
            ret = reqContainer.getTagOrEmpty(getId());
            this.target = new Target<>(reqContainer, ret);
        } else {
            ret = target.tag;
        }

        return ret;
    }

    private static final class Target<T> {
        Target(TagGroup<T> container, Tag<T> tag) {
            this.container = container;
            this.tag = tag;
        }

        final TagGroup<T> container;
        final Tag<T> tag;
    }
}
