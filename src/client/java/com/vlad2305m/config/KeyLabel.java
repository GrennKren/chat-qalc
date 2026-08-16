package com.vlad2305m.config;

import org.lwjgl.glfw.GLFW;

public final class KeyLabel {

    private KeyLabel() {}

    public static String format(int key, int modifiers) {
        StringBuilder sb = new StringBuilder();
        if ((modifiers & GLFW.GLFW_MOD_SHIFT)   != 0) sb.append("Shift+");
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) sb.append("Ctrl+");
        if ((modifiers & GLFW.GLFW_MOD_ALT)     != 0) sb.append("Alt+");
        if ((modifiers & GLFW.GLFW_MOD_SUPER)   != 0) sb.append("Super+");

        if (key == 0) {
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '+') {
                sb.setLength(sb.length() - 1);
            }
            return sb.length() == 0 ? "None" : sb.toString();
        }

        sb.append(keyName(key));
        return sb.toString();
    }

    public static String keyName(int key) {
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char) ('A' + (key - GLFW.GLFW_KEY_A)));
        }
        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            return String.valueOf((char) ('0' + (key - GLFW.GLFW_KEY_0)));
        }
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F25) {
            return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        }
        if (key >= GLFW.GLFW_KEY_KP_0 && key <= GLFW.GLFW_KEY_KP_9) {
            return "Numpad " + (key - GLFW.GLFW_KEY_KP_0);
        }

        return switch (key) {
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_BACKSPACE -> "Backspace";
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_ESCAPE -> "Esc";
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> "Shift";
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> "Ctrl";
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> "Alt";
            case GLFW.GLFW_KEY_UP -> "Up";
            case GLFW.GLFW_KEY_DOWN -> "Down";
            case GLFW.GLFW_KEY_LEFT -> "Left";
            case GLFW.GLFW_KEY_RIGHT -> "Right";
            case GLFW.GLFW_KEY_HOME -> "Home";
            case GLFW.GLFW_KEY_END -> "End";
            case GLFW.GLFW_KEY_INSERT -> "Insert";
            case GLFW.GLFW_KEY_DELETE -> "Delete";
            case GLFW.GLFW_KEY_PAGE_UP -> "Page Up";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "Page Down";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "Caps Lock";
            case GLFW.GLFW_KEY_NUM_LOCK -> "Num Lock";
            case GLFW.GLFW_KEY_SCROLL_LOCK -> "Scroll Lock";
            case GLFW.GLFW_KEY_PAUSE -> "Pause";
            case GLFW.GLFW_KEY_MENU -> "Menu";
            case GLFW.GLFW_KEY_PRINT_SCREEN -> "Print Screen";
            case 96 -> "`";
            case 45 -> "-";
            case 61 -> "=";
            case 91 -> "[";
            case 93 -> "]";
            case 92 -> "\\";
            case 59 -> ";";
            case 39 -> "'";
            case 44 -> ",";
            case 46 -> ".";
            case 47 -> "/";
            default -> {
                String glfwName = GLFW.glfwGetKeyName(key, 0);
                if (glfwName != null) yield glfwName.toUpperCase();
                yield "Key " + key;
            }
        };
    }

    public static boolean isCaptureable(int key) {
        return key != 0
                && key != GLFW.GLFW_KEY_ESCAPE
                && !isModifierKey(key);
    }

    public static boolean isModifierKey(int key) {
        return key == GLFW.GLFW_KEY_LEFT_SHIFT
                || key == GLFW.GLFW_KEY_RIGHT_SHIFT
                || key == GLFW.GLFW_KEY_LEFT_CONTROL
                || key == GLFW.GLFW_KEY_RIGHT_CONTROL
                || key == GLFW.GLFW_KEY_LEFT_ALT
                || key == GLFW.GLFW_KEY_RIGHT_ALT
                || key == GLFW.GLFW_KEY_LEFT_SUPER
                || key == GLFW.GLFW_KEY_RIGHT_SUPER;
    }

    public static int modifierBit(int key) {
        return switch (key) {
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> GLFW.GLFW_MOD_SHIFT;
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> GLFW.GLFW_MOD_CONTROL;
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> GLFW.GLFW_MOD_ALT;
            case GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER -> GLFW.GLFW_MOD_SUPER;
            default -> 0;
        };
    }
}
