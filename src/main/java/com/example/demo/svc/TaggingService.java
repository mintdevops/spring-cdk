package com.example.demo.svc;

import com.example.demo.config.AppConfig;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Getter
@Setter
public class TaggingService {

    private final AppConfig conf;
    private final NetworkStackService networkStackService;
    private final ImageStackService imageStackService;
    private Map<String, String> appTags = new HashMap<>();
    private Map<String, String> pipelineTags = new HashMap<>();
    private Map<String, String> envTags = new HashMap<>();
    private String namespace;

    public static Map<String, String> fullyQualifiedTags(String namespace, String qualifier, Map<String,
    String> tags) {
        Map<String, String> mutatedTags = new HashMap<>();

        log.debug("Adding tags {}", String.join(",", tags.keySet()));

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            // TODO: Implement sanitizeString() method compatible with CF
            String tagName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);;
            String tagValue = entry.getValue().replaceAll("^\"|\"$", "");

            String fqdn = String.format("%s%s",
                    (namespace == null || namespace.isEmpty() ? "" : namespace.replaceAll("^\"|\"$", "") + "."),
                    (qualifier == null || qualifier.isEmpty() ? "" : qualifier.replaceAll("^\"|\"$", "") + "/"));

            mutatedTags.put(String.format("%s%s", fqdn.trim(), tagName), tagValue);
        }

        return mutatedTags;
    }

    @PostConstruct
    public void loadUserTags() {
        this.namespace = config.getTagNamespace();
        this.appTags = config.getTags();
        this.pipelineTags = config.getPipeline().getTags();
        this.envTags = config.getPipeline().getEnv().getTags();
    }

    public void addApplicationTags(Construct app) {
        log.debug("addApplicationTags");
        //Stream.concat(map1.entrySet().stream(), map2.entrySet().stream());

        Map<String, String> resolvedTags = TagManager.fullyQualifiedTags(namespace, "app",
                this.appTags);

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            Tags.of(app).add(entry.getKey(), entry.getValue());
        }       
    }

    public void addStackTags(Construct stack, String qualifier) {
        log.debug("addStackTags");
        //Stream.concat(map1.entrySet().stream(), map2.entrySet().stream());

        TagManager.fullyQualifiedTags(namespace, qualifier,
                this.appTags);

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            Tags.of(stack).add(entry.getKey(), entry.getValue());
        }      
    }
}