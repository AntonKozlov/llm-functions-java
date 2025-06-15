
package io.github.antonkozlov.llm.functions;

import java.io.PrintStream;

// import java.lang.annotation.Retention;
// import java.lang.annotation.RetentionPolicy;

// @Retention(RetentionPolicy.RUNTIME)
// @interface Tool {
//     String description();
// }

public interface Tool {

    String getDescription();

    void main(PrintStream llmOutput) throws Exception;
}
