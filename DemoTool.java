///usr/bin/env jbang "$0" "$0" "$@" ; exit $?
//DEPS io.github.antonkozlov:llm-functions-java:1.0.0-SNAPSHOT

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.io.PrintStream;
import io.github.antonkozlov.llm.functions.Tool;
import io.github.antonkozlov.llm.functions.LlmFunctions;

public class DemoTool implements Tool {
    public String getDescription() {
        return "Demo tool that shows tool caling";
    }

    @JsonProperty(required = true)
    @JsonPropertyDescription("a parameter")
    public String param;

    public void main(PrintStream llmOutput) {
        llmOutput.println("tool returns test parameter = " + param);
    }

    public static void main(String[] args) throws Exception {
        LlmFunctions.main(DemoTool.class, args);
    }
}
