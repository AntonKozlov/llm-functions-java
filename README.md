# llm-functions-java

Implementation of https://github.com/sigoden/llm-functions for Java.

The repo contains the library for implementation of the functions.

Refer to DemoTool.java for an exmpale of a tool.

# Example

```
./DemoTool.java --install function-test

export AICHAT_FUNCTIONS_DIR=$PWD/function-test

aichat -r %functions% demonstrate tool caling with param = 123
```
