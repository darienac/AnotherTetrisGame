package render;

import java.util.HashMap;
import java.util.Map;

public class TextureCache {
    public static final String DEFAULT_WHITE = "defaultWhite.png";
    public static final String DEFAULT_BLACK = "defaultBlack.png";

    private static TextureCache instance = null;

    public static TextureCache getInstance() {
        if (instance == null) {
            instance = new TextureCache();
        }
        return instance;
    }

    private Map<String, Texture> textures = new HashMap<>();

    private TextureCache() {
    }

    public Texture getTexture(String path) throws Exception {
        if (path == null) {
            return getTexture(DEFAULT_WHITE);
        }
        if (!textures.containsKey(path)) {
            textures.put(path, new Texture(path));
        }
        return textures.get(path);
    }
}
