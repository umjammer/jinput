/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.linux;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;


/**
 * ffeffect.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-18 nsano initial version <br>
 */
class ffeffect extends Structure {

    static final short FF_RUMBLE = 0x50;
    static final short FF_CONSTANT = 0x52;

    /** 8 */
    static class ff_envelope extends Structure {
        public short attack_length;
        public short attack_level;
        public short fade_length;
        public short fade_level;

        public ff_envelope() {
        }

        public ff_envelope(Pointer p) {
            super(p);
        }

        public static class ByReference extends ff_envelope implements Structure.ByReference {
        }

        public static class ByValue extends ff_envelope implements Structure.ByValue {
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("attack_length", "attack_level", "fade_length", "fade_level");
        }
    }

    /** 4 */
    static class ff_trigger extends Structure {

        public short button;
        public short interval;

        public ff_trigger() {
        }

        public ff_trigger(Pointer p) {
            super(p);
        }

        public static class ByReference extends ff_trigger implements Structure.ByReference {
        }

        public static class ByValue extends ff_trigger implements Structure.ByValue {
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("button", "interval");
        }
    }

    /** 4 */
    static class ff_replay extends Structure {

        public short length;
        public short delay;

        public ff_replay() {
        }

        public ff_replay(Pointer p) {
            super(p);
        }

        public static class ByReference extends ff_replay implements Structure.ByReference {
        }

        public static class ByValue extends ff_replay implements Structure.ByValue {
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("length", "delay");
        }
    }

    /** 10 */
    static class ff_constant_effect extends Structure {

        public short level; // 2
        public ff_envelope envelope; // 8

        public ff_constant_effect() {
        }

        public ff_constant_effect(Pointer p) {
            super(p);
        }

        public static class ByReference extends ff_constant_effect implements Structure.ByReference {
        }

        public static class ByValue extends ff_constant_effect implements Structure.ByValue {
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("level", "envelope");
        }
    }

    /** 12 */
    static class ff_ramp_effect extends Structure {

        public short start_level; // 2
        public short end_level; // 2
        public ff_envelope envelope; // 8

        public ff_ramp_effect() {
        }

        public ff_ramp_effect(Pointer p) {
            super(p);
        }

        public static class ByReference extends ff_ramp_effect implements Structure.ByReference {
        }

        public static class ByValue extends ff_ramp_effect implements Structure.ByValue {
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("start_level", "end_level", "envelope");
        }
    }

    /** 30 */
    static class ff_periodic_effect extends Structure {

        public short waveform;
        public short period;
        public short magnitude;
        public short offset;
        public short phase;
        public ff_envelope envelope; // 8

        public int custom_len; // 4
        public Pointer /* short */ custom_data; //

        public ff_periodic_effect() {
        }

        public ff_periodic_effect(Pointer p) {
            super(p);
        }

        public static class ByReference extends ff_periodic_effect implements Structure.ByReference {
        }

        public static class ByValue extends ff_periodic_effect implements Structure.ByValue {
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("waveform", "period", "magnitude", "offset", "phase", "envelope", "custom_len", "custom_data");
        }
    }

    /** 12 */
    static class ff_condition_effect extends Structure {

        public short right_saturation;
        public short left_saturation;

        public short right_coeff;
        public short left_coeff;

        public short deadband;
        public short center;

        public ff_condition_effect() {
        }

        public ff_condition_effect(Pointer p) {
            super(p);
        }

        public static class ByReference extends ff_condition_effect implements Structure.ByReference {
        }

        public static class ByValue extends ff_condition_effect implements Structure.ByValue {
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(" right_saturation", "left_saturation", "right_coeff", "left_coeff", "deadband", "center");
        }
    }

    /** 4 */
    static class ff_rumble_effect extends Structure {

        public short strong_magnitude;
        public short weak_magnitude;

        public ff_rumble_effect() {
        }

        public ff_rumble_effect(Pointer p) {
            super(p);
        }

        public static class ByReference extends ff_rumble_effect implements Structure.ByReference {
        }

        public static class ByValue extends ff_rumble_effect implements Structure.ByValue {
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("strong_magnitude", "weak_magnitude");
        }
    }

    public short type; // 2
    public short id; // 2
    public short direction; // 2
    public ff_trigger trigger; // 4
    public ff_replay replay; // 4

    static class U extends Union {
        public ff_constant_effect constant; // 10
        public ff_ramp_effect ramp; // 12
        public ff_periodic_effect periodic; // 30
        public ff_condition_effect[] condition = new ff_condition_effect[2]; // 12 * 2, One for each axis
        public ff_rumble_effect rumble; // 4

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("constant", "ramp", "periodic", "condition", "rumble");
        }
    }

    public U u;

    public ffeffect() {
    }

    public ffeffect(Pointer p) {
        super(p);
    }

    public static class ByReference extends ffeffect implements Structure.ByReference {
    }

    public static class ByValue extends ffeffect implements Structure.ByValue {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("type", "id", "direction", "trigger", "replay", "u");
    }
}
