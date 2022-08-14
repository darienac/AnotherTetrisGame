package startup;

import audio.SoundBuffer;
import audio.SoundManager;
import audio.SoundSource;
import game.GameEngine;
import game.GameState;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import render.GameRenderer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GameWindow implements AutoCloseable {
    private long window;
    private GameState gameState;
    private GameRenderer gameRenderer;
    private GameEngine gameEngine;
    private SoundManager soundManager;
    private int width;
    private int height;
    private boolean resized;

    public GameWindow(int width, int height) throws Exception {
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = GLFW.glfwCreateWindow(width, height, "Tetris Game", NULL, NULL);
        this.width = width;
        this.height = height;
        this.resized = false;
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                GLFW.glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
        });
        GLFW.glfwSetFramebufferSizeCallback(window, (window, newWidth, newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
            resized = true;
        });

        GLFWVidMode vidmode = glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

        glfwSetWindowPos(
                window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);

        GLCapabilities caps = GL.createCapabilities();
        if (!caps.GL_ARB_shader_objects) {
            throw new AssertionError("This game requires the ARB_shader_objects extension.");
        }
        if (!caps.GL_ARB_vertex_shader) {
            throw new AssertionError("This game requires the ARB_vertex_shader extension.");
        }
        if (!caps.GL_ARB_fragment_shader) {
            throw new AssertionError("This game requires the ARB_fragment_shader extension.");
        }
        GLUtil.setupDebugMessageCallback();

        gameState = new GameState();
        gameRenderer = new GameRenderer(gameState, this);
        gameEngine = new GameEngine(gameState, gameRenderer.getGameScene(), this);
        soundManager = new SoundManager(gameRenderer.getGameScene().getCamera());
        soundManager.init();
        SoundBuffer testSound = new SoundBuffer("test1.ogg");
        SoundSource soundSource = new SoundSource(true, true);
        soundSource.setBuffer(testSound.getBufferId());
        soundSource.play();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public long getWindowId() {
        return window;
    }

    public void run() throws Exception {
        while (!GLFW.glfwWindowShouldClose(window)) {
            gameRenderer.render();
            gameEngine.run();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }

        try {
            this.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to close window");
        }
    }

    @Override
    public void close() throws Exception {
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
    }
}