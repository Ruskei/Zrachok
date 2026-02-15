package terminal;

import java.util.Arrays;

/**
 * To account for terminal scaling, the x-values are rendered twice to preserve scale.
 */
public class TerminalDisplay {
    public final int extentX;
    public final int extentY;

    public Color[] colorData;
    private Color[] renderedColorData;
    private boolean firstTime = true;

    public TerminalDisplay(final int extentX, final int extentY) {
        this.extentX = extentX;
        this.extentY = extentY;

        final int size = extentX * extentY;

        renderedColorData = new Color[size];
        for (int i = 0; i < size; i++)
            renderedColorData[i] = new Color(0, 0, 0);

        colorData = new Color[size];
        for (int i = 0; i < size; i++)
            colorData[i] = new Color(0, 0, 0);
    }

    public void update() {
        if (firstTime) {
            Color currentColor = null;

            firstTime = false;
            System.out.print("\033[2J\033[H");

            for (int y = 0; y < extentY; y++) {
                for (int x = 0; x < extentX; x++) {
                    var pixelColor = colorData[y * extentX + x];
                    if (pixelColor != currentColor) {
                        final var colorStr = "\033[38;2;" +
                                pixelColor.r() + ";" +
                                pixelColor.g() + ";" +
                                pixelColor.b() + "m";
                        System.out.print(colorStr);

                        currentColor = pixelColor;
                    }

                    System.out.print("██");
                }

                System.out.println();
            }
        }

        if (Arrays.equals(colorData, renderedColorData)) return;
    }
}
