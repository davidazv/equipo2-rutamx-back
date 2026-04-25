package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Random;
import java.util.Set;

@ApplicationScoped
public class RouteColorAssigner {

    private static final String[] COLOR_PALETTE = {
        "D40D0D", "141982", "7A9A01", "8D1A96", "FF9A03",
        "F9D616", "0071C1", "D81E05", "A02D96", "F94F8E",
        "00A099", "E20613", "A5C731", "FC9408", "2D2E82",
        "FFE616", "5155A4", "00673E", "F47325", "00B3EE",
        "1F5AF0", "4EC3E0", "FF0000", "83332E", "009B3A",
        "8B5CF6", "22C55E", "3B82F6", "D97706", "0891B2"
    };

    public String assignColor(Set<String> usedColors) {
        for (String color : COLOR_PALETTE) {
            if (!usedColors.contains(color.toUpperCase())) {
                return color;
            }
        }
        return generateRandomColor(usedColors);
    }

    private String generateRandomColor(Set<String> usedColors) {
        Random random = new Random();
        for (int attempt = 0; attempt < 100; attempt++) {
            String color = String.format("%02X%02X%02X",
                    random.nextInt(200) + 30,
                    random.nextInt(200) + 30,
                    random.nextInt(200) + 30);
            if (!usedColors.contains(color.toUpperCase())) {
                return color;
            }
        }
        return "6B7280";
    }
}
