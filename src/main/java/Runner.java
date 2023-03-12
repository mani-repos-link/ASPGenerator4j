package main.java;

import main.java.generator.AspGenerator;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
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
//        File declModelFile = new File("src/main/resources/models/declare/decl-model4.decl");
//        File declModelFile = new File("src/main/resources/models/declare/decl-model5.decl");
        File declModelFile = new File("src/main/resources/models/declare/model4.decl");
        int logSize = 5;
        int minTraceSize = 20;
        int maxTraceSize = 30;
        XLog generatedLog = AspGenerator.generateLog(
                declModelFile.toPath(),
                minTraceSize,
                maxTraceSize,
                logSize,
                LocalDateTime.now(),
                Duration.ofHours(4)
        );
        int counter = 0;
        for (XTrace x : generatedLog) {
            XConceptExtension.instance()
                    .assignName(x, "Case No. " + counter++ + " [positive]");
        }

        String fileName = "generated_output/log2.xes";
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileName));
        XesXmlSerializer serializer = new XesXmlSerializer();
        serializer.serialize(generatedLog, outputStream);

        outputStream.close();
    }
}
