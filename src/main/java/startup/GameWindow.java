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

import java.nio.IntBuffer;

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
    private boolean fullscreen;

    private int windowedX;
    private int windowedY;
    private int windowedWidth;
    private int windowedHeight;

    public GameWindow(int width, int height) throws Exception {
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Tetris Game", NULL, NULL);
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

        SoundBuffer testSound = new SoundBuffer("music.ogg");
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
        Thread engineThread = new Thread(gameEngine);
        engineThread.start();
        while (!GLFW.glfwWindowShouldClose(window)) {
            if (gameEngine.getControls().fullscreenToggle) {
                gameEngine.getControls().fullscreenToggle = false;
                setFullscreen(!isFullscreen());
            }
            try {
                gameRenderer.render();
            } catch (Exception ex) {
                engineThread.interrupt();
                throw ex;
            }
            // gameEngine.run();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
        engineThread.interrupt();

        try {
            this.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to close window");
        }
    }

    public void setFullscreen(boolean makeFullscreen) {
        fullscreen = makeFullscreen;
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        if (makeFullscreen) {
            int[] w = new int[1];
            int[] h = new int[1];
            int[] x = new int[1];
            int[] y = new int[1];
            glfwGetWindowSize(window, w, h);
            glfwGetWindowPos(window, x, y);
            windowedX = x[0];
            windowedY = y[0];
            windowedWidth = w[0];
            windowedHeight = h[0];

            glfwWindowHint(GLFW_RED_BITS, mode.redBits());
            glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
            glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
            glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());

            glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
        } else {
            glfwSetWindowMonitor(window, NULL, windowedX, windowedY, windowedWidth, windowedHeight, mode.refreshRate());
        }
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    @Override
    public void close() throws Exception {
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
    }
}