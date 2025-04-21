package com.ehhthan.mmobuffs.api.effect.stack;

import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;

public enum StackType {
    NORMAL {
        public boolean tick(ActiveStatusEffect effect) {
            effect.setStacks(0); effect.deactivate(); return false;
        }
    },
    CASCADING {
        public boolean tick(ActiveStatusEffect effect) {
            effect.setStacks(effect.getStacks() - 1);
            if (effect.getStacks() <= 0) { effect.deactivate(); return false; }
            effect.setDuration(effect.getStartDuration()); return true;
        }
    },
    TIMESTACK {
        public boolean tick(ActiveStatusEffect effect) {
            effect.setStacks(effect.getStacks() - 1);
            if (effect.getStacks() <= 0) { effect.deactivate(); return false; }
            effect.setDuration(effect.getStartDuration()); return true;
        }
    },
    ATTACK {
        public boolean tick(ActiveStatusEffect effect) {
            effect.setStacks(effect.getStacks() - 1);
            if (effect.getStacks() <= 0) effect.deactivate();
            return false;
        }
    },
    HURT { public boolean tick(ActiveStatusEffect effect) { return ATTACK.tick(effect); } },
    COMBAT { public boolean tick(ActiveStatusEffect effect) { return ATTACK.tick(effect); } };

    public abstract boolean tick(ActiveStatusEffect effect);
}