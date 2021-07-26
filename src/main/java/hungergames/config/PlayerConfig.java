package hungergames.config;

import org.jetbrains.annotations.Nullable;

public class PlayerConfig {
    private final String name;
    private final Double meleeSkill;
    private final Double rangedSkill;
    private final Double intelligence;

    public String getName() {
        return name;
    }

    public Double getMeleeSkill() {
        return meleeSkill;
    }

    public Double getRangedSkill() {
        return rangedSkill;
    }

    public Double getIntelligence() {
        return intelligence;
    }

    public PlayerConfig(@Nullable String name, @Nullable Double meleeSkill, @Nullable Double rangedSkill, @Nullable Double intelligence) {
        this.name = name;
        this.meleeSkill = meleeSkill;
        this.rangedSkill = rangedSkill;
        this.intelligence = intelligence;
    }
}
