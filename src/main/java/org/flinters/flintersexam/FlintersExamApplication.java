package org.flinters.flintersexam;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FlintersExamApplication {

    public static void main(String[] args) {
        int exitCode = 0;
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(FlintersExamApplication.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run(args)) {
            // Spring Boot context closes automatically on try-with-resources completion
        } catch (Exception e) {
            exitCode = 1;
        }
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
