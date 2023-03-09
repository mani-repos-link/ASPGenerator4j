package main.java;

import main.java.generator.AspGenerator;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Runner {
    public static void main(String[] args) throws InterruptedException, IOException {
        //Path declModelPath = Paths.get();
        File declModelFile = new File("src/main/resources/models/declare/decl-model4.decl");
        int minTraceSize = 2;
        int maxTraceSize = 2;
        int logSize = 5;
        XLog generatedLog = AspGenerator.generateLog(
                declModelFile.toPath(),
                minTraceSize,
                maxTraceSize,
                logSize,
                LocalDateTime.now(),
                Duration.ofHours(4)
        );
        String fileName = "generated_output/log.xes";
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileName));
        XesXmlSerializer serializer = new XesXmlSerializer();
        serializer.serialize(generatedLog, outputStream);
        outputStream.close();
    }
}
