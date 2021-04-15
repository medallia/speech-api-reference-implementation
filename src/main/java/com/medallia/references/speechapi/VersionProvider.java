package com.medallia.references.speechapi;

import java.io.InputStreamReader;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * This is a Picocli provider for project version information, reading from
 * the Maven build system.
 */
public class VersionProvider implements IVersionProvider {

    @Spec
    private CommandSpec spec;

    @Override
    public String[] getVersion() throws Exception {
        final MavenXpp3Reader reader = new MavenXpp3Reader();

        final Model model = reader.read(new InputStreamReader(
            SpeechApiUploadApplication.class.getResourceAsStream(
                "/META-INF/maven/medallia-reference-implementations/speech-api-upload/pom.xml"
            )
        ));

        final String version = model.getVersion();

        return new String[] {
            String.format("version %s", version)
        };
    }

}
