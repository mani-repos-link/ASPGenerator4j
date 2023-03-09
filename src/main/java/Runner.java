package main.java;

import main.java.generator.AspGenerator;
import org.deckfour.xes.model.XLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

public class Runner {
    public static void main(String[] args) throws InterruptedException, IOException {
        //Path declModelPath = Paths.get();
        File declModelFile = new File("src/main/resources/models/declare/decl-model4.decl");
        int minTraceSize = 2;
        int maxTraceSize = 2;
        int logSize = 5;
        XLog log = AspGenerator.generateLog(
                declModelFile.toPath(),
                minTraceSize,
                maxTraceSize,
                logSize,
                LocalDateTime.now(),
                Duration.ofHours(4)
        );
    }
}
