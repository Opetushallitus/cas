package fi.vm.sade.auth;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ResourceHelper {

    private ResourceHelper() {
    }

    public static String loadAsString(String resourceName) {
        return loadAsString(resourceName, StandardCharsets.UTF_8);
    }

    public static String loadAsString(String resourceName, Charset charset) {
        try {
            Path path = Paths.get(ResourceHelper.class.getClassLoader().getResource(resourceName).toURI());
            return new String(Files.readAllBytes(path), charset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
