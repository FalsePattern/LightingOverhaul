package lightingoverhaul.coremod.helper;

import lightingoverhaul.coremod.mixin.interfaces.ITessellatorMixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceHelper {

    public static String readResourceAsString(String path) {
        InputStream is = ITessellatorMixin.class.getResourceAsStream(path);
        if (is == null) return null;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder source = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                source.append(line).append("\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return source.toString();
    }
}
