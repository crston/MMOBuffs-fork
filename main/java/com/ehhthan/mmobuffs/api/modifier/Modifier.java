package com.ehhthan.mmobuffs.api.modifier;

/**
 * Defines how a value (like duration or stacks) should be modified.
 */
public enum Modifier {
    /** Overwrites the existing value. */
    SET,

    /** Keep existing value if present; apply only if not set. */
    KEEP,

    /** Refresh if current value is lower than incoming value. */
    REFRESH,

    /** Add incoming value to current value. */
    ADD,

    /** Subtract incoming value from current value. */
    SUBTRACT
}
