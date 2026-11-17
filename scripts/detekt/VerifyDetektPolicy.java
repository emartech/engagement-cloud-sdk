import dev.detekt.api.RuleSetProvider;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Enumeration;
import java.net.URL;

/** Validates the policy against the actual pinned engine/extension providers, never a remote catalog. */
public class VerifyDetektPolicy {
    private static final Set<String> NON_RULE_SECTIONS = Set.of("config", "processors", "console-reports");
    private static final Set<String> SECTION_OPTIONS = Set.of("active", "excludes", "includes", "severity");
    private static final Set<String> RULE_OPTIONS = Set.of("active", "excludes", "includes", "severity", "aliases", "ignoreAnnotated");
    // Documented in the pinned Compose 0.6.8 Material2 implementation; absent from its default YAML.
    private static final Map<String, Set<String>> EXTRA_OPTIONS = Map.of("Compose.Material2", Set.of("allowedFromM2"));

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value, String path) {
        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalArgumentException(path + " must be a YAML mapping");
        }
        return (Map<String, Object>) value;
    }

    public static void main(String[] args) throws Exception {
        Path policyPath = Path.of(args[0]);
        var loader = new Load(LoadSettings.builder().setLabel(policyPath.toString()).build());
        Map<String, Object> policy;
        try (var input = Files.newInputStream(policyPath)) {
            policy = map(loader.loadFromInputStream(input), "policy");
        }

        var expected = new TreeSet<String>();
        var sections = new TreeSet<String>();
        int providers = 0;
        for (var provider : ServiceLoader.load(RuleSetProvider.class)) {
            providers++;
            String section = provider.getRuleSetId().getValue();
            sections.add(section);
            for (var name : provider.instance().getRules().keySet()) {
                if (!expected.add(section + "." + name.getValue())) {
                    throw new IllegalArgumentException("Duplicate runtime rule: " + section + "." + name);
                }
            }
        }
        if (providers == 0 || !sections.contains("libraries") || !sections.contains("Compose")) {
            throw new IllegalArgumentException("The pinned built-in, libraries and Compose providers must all load");
        }

        var defaults = new HashMap<String, Object>();
        var classLoader = VerifyDetektPolicy.class.getClassLoader();
        try (var input = classLoader.getResourceAsStream("default-detekt-config.yml")) {
            defaults.putAll(map(loader.loadFromInputStream(input), "built-in defaults"));
        }
        Enumeration<URL> extensions = classLoader.getResources("config/config.yml");
        while (extensions.hasMoreElements()) {
            try (var input = extensions.nextElement().openStream()) {
                defaults.putAll(map(loader.loadFromInputStream(input), "extension defaults"));
            }
        }

        var actual = new TreeSet<String>();
        for (var entry : policy.entrySet()) {
            String section = entry.getKey();
            if (NON_RULE_SECTIONS.contains(section)) continue;
            if (!sections.contains(section)) {
                throw new IllegalArgumentException("Unknown rule set: " + section);
            }
            var values = map(entry.getValue(), section);
            if (!(values.get("active") instanceof Boolean)) {
                throw new IllegalArgumentException(section + " needs an explicit active state");
            }
            for (var rule : values.entrySet()) {
                if (SECTION_OPTIONS.contains(rule.getKey())) continue;
                String id = section + "." + rule.getKey();
                var config = map(rule.getValue(), id);
                if (!(config.get("active") instanceof Boolean)) {
                    throw new IllegalArgumentException(id + " needs an explicit active state");
                }
                var allowed = new TreeSet<>(RULE_OPTIONS);
                var sectionDefaults = map(defaults.get(section), section + " defaults");
                if (sectionDefaults.get(rule.getKey()) instanceof Map<?, ?>) {
                    allowed.addAll(map(sectionDefaults.get(rule.getKey()), id + " defaults").keySet());
                }
                allowed.addAll(EXTRA_OPTIONS.getOrDefault(id, Set.of()));
                var unknownOptions = new TreeSet<>(config.keySet());
                unknownOptions.removeAll(allowed);
                if (!unknownOptions.isEmpty()) {
                    throw new IllegalArgumentException(id + " has unknown options: " + unknownOptions);
                }
                actual.add(id);
            }
        }

        var missing = new TreeSet<>(expected);
        missing.removeAll(actual);
        var obsolete = new TreeSet<>(actual);
        obsolete.removeAll(expected);
        if (!missing.isEmpty() || !obsolete.isEmpty()) {
            throw new IllegalArgumentException("Policy/catalog mismatch; missing=" + missing + "; unknown=" + obsolete);
        }
        System.out.println("Detekt policy is complete: " + expected.size() +
            " explicit rules verified against " + providers + " pinned runtime providers.");
    }
}
