/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.config;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

// Minimal stand-in for NeoForge's ModConfigSpec, preserving the builder API
// used by EConfig. Values are stored in a simple TOML-like file with comments.
public final class ModConfigSpec {
    private static final Logger LOGGER = LoggerFactory.getLogger("exdeorum/config");

    private final Map<String, ConfigValue<?>> values = new LinkedHashMap<>();
    private final Map<String, String> comments = new LinkedHashMap<>();
    private final Map<String, List<String>> sectionComments = new LinkedHashMap<>();
    private boolean loaded;

    public static <T> Pair<T, ModConfigSpec> configure(Function<Builder, T> consumer) {
        var builder = new Builder();
        var config = consumer.apply(builder);
        return Pair.of(config, builder.build());
    }

    private ModConfigSpec(Builder builder) {
        this.values.putAll(builder.values);
        this.comments.putAll(builder.comments);
        this.sectionComments.putAll(builder.sectionComments);
    }

    // NeoForge reports whether the config file has been read yet.
    public boolean isLoaded() {
        return this.loaded;
    }

    public void load(Path file) {
        this.loaded = true;

        if (!Files.exists(file)) {
            save(file);
            return;
        }

        var raw = new LinkedHashMap<String, String>();

        try {
            String section = "";

            for (var line : Files.readAllLines(file)) {
                var trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    section = trimmed.substring(1, trimmed.length() - 1).trim();
                    continue;
                }

                int equals = trimmed.indexOf('=');

                if (equals < 0) {
                    continue;
                }

                var key = trimmed.substring(0, equals).trim();
                var value = trimmed.substring(equals + 1).trim();
                var path = section.isEmpty() ? key : section + "." + key;
                raw.put(path, value);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read config file {}", file, e);
        }

        for (var value : this.values.values()) {
            var text = raw.get(value.path);

            if (text != null) {
                value.parse(text);
            }
        }

        save(file);
    }

    public void save(Path file) {
        try {
            Files.createDirectories(file.getParent());
            var sb = new StringBuilder();
            String section = null;

            for (var value : this.values.values()) {
                int dot = value.path.lastIndexOf('.');
                var newSection = dot < 0 ? "" : value.path.substring(0, dot);
                var key = dot < 0 ? value.path : value.path.substring(dot + 1);

                if (!newSection.equals(section)) {
                    if (section != null) {
                        sb.append('\n');
                    }
                    section = newSection;
                    sb.append('[').append(newSection).append("]\n");

                    var sectionComment = this.sectionComments.get(newSection);

                    if (sectionComment != null) {
                        for (var commentLine : sectionComment) {
                            sb.append("# ").append(commentLine).append('\n');
                        }
                        sb.append('\n');
                    }
                }

                var comment = this.comments.get(value.path);

                if (comment != null) {
                    for (var commentLine : comment.split("\n")) {
                        sb.append("# ").append(commentLine).append('\n');
                    }
                }

                sb.append(key).append(" = ").append(value.write()).append("\n\n");
            }

            Files.writeString(file, sb.toString());
        } catch (IOException e) {
            LOGGER.error("Failed to write config file {}", file, e);
        }
    }

    public static final class Builder {
        private final Map<String, ConfigValue<?>> values = new LinkedHashMap<>();
        private final Map<String, String> comments = new LinkedHashMap<>();
        private final Map<String, List<String>> sectionComments = new LinkedHashMap<>();
        private final List<String> currentPath = new ArrayList<>();
        private final List<String> pendingComments = new ArrayList<>();

        public Builder comment(String comment) {
            this.pendingComments.add(comment);
            return this;
        }

        public Builder push(String path) {
            if (!this.pendingComments.isEmpty()) {
                this.sectionComments.put(String.join(".", this.currentPath.isEmpty() ? List.of(path) : append(path)), new ArrayList<>(this.pendingComments));
                this.pendingComments.clear();
            }
            this.currentPath.add(path);
            return this;
        }

        public Builder pop() {
            this.currentPath.remove(this.currentPath.size() - 1);
            return this;
        }

        private List<String> append(String path) {
            var list = new ArrayList<>(this.currentPath);
            list.add(path);
            return list;
        }

        public BooleanValue define(String path, boolean defaultValue) {
            return defineBoolean(fullPath(path), defaultValue);
        }

        public IntValue defineInRange(String path, int defaultValue, int min, int max) {
            return defineInt(fullPath(path), defaultValue, min, max);
        }

        public DoubleValue defineInRange(String path, double defaultValue, double min, double max) {
            return defineDouble(fullPath(path), defaultValue, min, max);
        }

        public ConfigValue<String> define(String path, String defaultValue) {
            return defineString(fullPath(path), defaultValue);
        }

        public ConfigValue<String> define(List<String> path, String defaultValue, Predicate<String> validator) {
            return defineString(String.join(".", path), defaultValue);
        }

        private BooleanValue defineBoolean(String path, boolean defaultValue) {
            var value = new BooleanValue(path, defaultValue);
            this.values.put(path, value);
            return value;
        }

        private IntValue defineInt(String path, int defaultValue, int min, int max) {
            var value = new IntValue(path, defaultValue, min, max);
            this.values.put(path, value);
            return value;
        }

        private DoubleValue defineDouble(String path, double defaultValue, double min, double max) {
            var value = new DoubleValue(path, defaultValue, min, max);
            this.values.put(path, value);
            return value;
        }

        private ConfigValue<String> defineString(String path, String defaultValue) {
            var value = new StringValue(path, defaultValue);
            this.values.put(path, value);
            return value;
        }

        private String fullPath(String path) {
            var full = String.join(".", this.currentPath) + "." + path;
            var comment = String.join("\n", this.pendingComments);

            if (!comment.isEmpty()) {
                this.comments.put(full, comment);
            }
            this.pendingComments.clear();
            return full;
        }

        private ModConfigSpec build() {
            return new ModConfigSpec(this);
        }
    }

    public static abstract class ConfigValue<T> {
        final String path;
        T value;

        ConfigValue(String path, T defaultValue) {
            this.path = path;
            this.value = defaultValue;
        }

        public T get() {
            return this.value;
        }

        abstract void parse(String text);

        abstract String write();
    }

    public static final class BooleanValue extends ConfigValue<Boolean> {
        BooleanValue(String path, Boolean defaultValue) {
            super(path, defaultValue);
        }

        public boolean getAsBoolean() {
            return this.value;
        }

        @Override
        void parse(String text) {
            if (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false")) {
                this.value = Boolean.parseBoolean(text);
            }
        }

        @Override
        String write() {
            return String.valueOf(this.value);
        }
    }

    public static final class IntValue extends ConfigValue<Integer> {
        private final int min;
        private final int max;

        IntValue(String path, Integer defaultValue, int min, int max) {
            super(path, defaultValue);
            this.min = min;
            this.max = max;
        }

        public int getAsInt() {
            return this.value;
        }

        @Override
        void parse(String text) {
            try {
                int parsed = Integer.parseInt(text);

                if (parsed >= this.min && parsed <= this.max) {
                    this.value = parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        @Override
        String write() {
            return String.valueOf(this.value);
        }
    }

    public static final class DoubleValue extends ConfigValue<Double> {
        private final double min;
        private final double max;

        DoubleValue(String path, Double defaultValue, double min, double max) {
            super(path, defaultValue);
            this.min = min;
            this.max = max;
        }

        public double getAsDouble() {
            return this.value;
        }

        @Override
        void parse(String text) {
            try {
                double parsed = Double.parseDouble(text);

                if (parsed >= this.min && parsed <= this.max) {
                    this.value = parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        @Override
        String write() {
            return String.valueOf(this.value);
        }
    }

    public static final class StringValue extends ConfigValue<String> {
        StringValue(String path, String defaultValue) {
            super(path, defaultValue);
        }

        @Override
        void parse(String text) {
            if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
                this.value = text.substring(1, text.length() - 1);
            } else if (!text.isEmpty()) {
                this.value = text;
            }
        }

        @Override
        String write() {
            return '"' + this.value + '"';
        }
    }
}
