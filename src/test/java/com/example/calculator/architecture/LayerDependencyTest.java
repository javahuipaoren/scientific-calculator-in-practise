package com.example.calculator.architecture;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * DDD 分层依赖方向测试。
 *
 * <p>不额外引入架构测试组件，直接扫描主代码 import，确保领域层和应用层
 * 不会反向依赖外层。
 */
@DisplayName("DDD 分层依赖规则")
class LayerDependencyTest {

    private static final Path MAIN_SOURCE_ROOT =
            Path.of("src/main/java/com/example/calculator");

    @Test
    @DisplayName("领域层不得依赖 Spring、应用层、基础设施层或接口层")
    void domainLayerShouldNotDependOnOuterLayers() throws IOException {
        assertNoForbiddenImports(
                MAIN_SOURCE_ROOT.resolve("domain"),
                List.of(
                        "org.springframework",
                        "jakarta.",
                        "com.example.calculator.application",
                        "com.example.calculator.infrastructure",
                        "com.example.calculator.interfaces"));
    }

    @Test
    @DisplayName("应用层不得依赖 Spring、基础设施层或接口层")
    void applicationLayerShouldNotDependOnOuterLayers() throws IOException {
        assertNoForbiddenImports(
                MAIN_SOURCE_ROOT.resolve("application"),
                List.of(
                        "org.springframework",
                        "jakarta.",
                        "com.example.calculator.infrastructure",
                        "com.example.calculator.interfaces"));
    }

    private void assertNoForbiddenImports(
            Path layerPath, List<String> forbiddenPackages) throws IOException {
        try (Stream<Path> sourceFiles = Files.walk(layerPath)) {
            for (Path sourceFile : sourceFiles
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList()) {
                String source = Files.readString(sourceFile);
                for (String forbiddenPackage : forbiddenPackages) {
                    assertFalse(
                            source.contains("import " + forbiddenPackage),
                            () -> sourceFile
                                    + " 不应依赖外层包 "
                                    + forbiddenPackage);
                }
            }
        }
    }
}
