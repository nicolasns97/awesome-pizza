package com.adesso.awesomepizza.order;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TrackingCodeGeneratorTest {

    private final TrackingCodeGenerator generator = new TrackingCodeGenerator();

    @Test
    void generatesDistinctCodes() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            codes.add(generator.generate());
        }
        assertThat(codes).hasSize(10_000);
    }
}
