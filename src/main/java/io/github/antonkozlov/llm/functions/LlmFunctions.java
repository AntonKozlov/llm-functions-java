package io.github.antonkozlov.llm.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class LlmFunctions {
    private static JsonNode generateSchema(Class<? extends Tool> tool) throws Exception {
        var schemaGenerator = new SchemaGenerator(new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12,
                OptionPreset.PLAIN_JSON)
            .with(new JacksonModule(
                JacksonOption.RESPECT_JSONPROPERTY_ORDER,
                JacksonOption.RESPECT_JSONPROPERTY_REQUIRED))
            .build());

        ObjectNode jsonSchema = (ObjectNode) schemaGenerator.generateSchema(tool);

        var objectMapper = new ObjectMapper();

        var toolInst = tool.getDeclaredConstructor().newInstance();
        var descr = objectMapper.createObjectNode()
            .put("name", tool.getName())
            .put("description", toolInst.getDescription())
            .set("parameters", jsonSchema);
        
        return descr;
    }

    private static void installSymlink(String toolPath, Path targetDir, String toolName) throws IOException {
        Files.createDirectories(targetDir.resolve("bin"));
        var source = Path.of(toolPath).toAbsolutePath();
        var target = targetDir.resolve("bin").resolve(toolName);
        if (Files.exists(target) && Files.isSymbolicLink(target)) {
            Files.delete(target);
        }
        Files.createSymbolicLink(target, source);
    }

    private static void installFunctionJson(JsonNode newFunction, Path targetDir)
            throws JsonProcessingException, IOException {
        var objectMapper = new ObjectMapper();
        var functionsJsonPath = targetDir.resolve("functions.json");
        var functions = objectMapper.createArrayNode();
        if (functionsJsonPath.toFile().exists()) {
            var content = Files.readAllBytes(functionsJsonPath);
            functions = (ArrayNode)objectMapper.readTree(content);
        }

        var elements = functions.elements();
        while (elements.hasNext()) {
            var e = elements.next();
            if (e.get("name").equals(newFunction.get("name"))) {
                elements.remove();
            }
        }

        functions.add(newFunction);

        Files.write(functionsJsonPath, objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsBytes(functions));
    }


    public static void main(Class<? extends Tool> tool, String[] args) throws Exception {
        if ("--install".equals(args[1])) {
            var targetDir = Path.of(args[2]);
            if (!Files.exists(targetDir)) {
                Files.createDirectory(targetDir);
            }

            installSymlink(args[0], targetDir, tool.getName());

            var newFunction = generateSchema(tool);
            installFunctionJson(newFunction, targetDir);
            return;
        }

        var mapper = new ObjectMapper();
        var toolInst = mapper.readValue(args[1], tool);

        var output = System.getenv().get("LLM_OUTPUT");
        var outputStream = new PrintStream(new FileOutputStream(output));
        toolInst.main(outputStream);
    }
}
