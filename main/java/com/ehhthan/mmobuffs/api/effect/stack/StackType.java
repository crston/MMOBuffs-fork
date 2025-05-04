package com.ehhthan.mmobuffs.api.effect.stack;

/**
 * Defines how effect stacks behave over time or on actions.
 */
public enum StackType {

    /**
     * Default type. All stacks removed when duration expires.
     * Multiplies value by stack count.
     */
    NORMAL,

    /**
     * Removes one stack and refreshes duration until no stacks remain.
     * Multiplies value by stack count.
     */
    CASCADING,

    /**
     * Like CASCADING but does not multiply value.
     */
    TIMESTACK,

    /**
     * Removes a stack when the player attacks something.
     * No multiplier. Removed entirely if duration expires.
     */
    ATTACK,

    /**
     * Removes a stack when the player takes damage.
     * No multiplier. Removed entirely if duration expires.
     */
    HURT,

    /**
     * Removes a stack on either attack or hurt (combined).
     * No multiplier. Removed if duration expires.
     */
    COMBAT
}
