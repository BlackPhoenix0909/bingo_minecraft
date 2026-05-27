#!/usr/bin/env python3
"""
Generates a simple Minecraft-style bingo GUI texture (icons/particles).
Run this to create placeholder textures for the mod.
Requires Pillow: pip install Pillow
"""

try:
    from PIL import Image, ImageDraw, ImageFont
    import os

    def create_bingo_icon(path, size=32):
        """Create a simple bingo-themed icon."""
        img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)

        # Dark background
        draw.rectangle([0, 0, size-1, size-1], fill=(55, 55, 55, 255))
        # Border
        draw.rectangle([0, 0, size-1, size-1], outline=(170, 170, 170, 255), width=2)

        # Gold B letter
        draw.rectangle([4, 4, size-5, size-5], fill=(40, 90, 27, 200))

        # B I N G O text (simplified)
        # Just fill with a gold diamond pattern
        mid = size // 2
        for dx, dy in [(mid, 4), (4, mid), (mid, size-5), (size-5, mid)]:
            draw.ellipse([dx-3, dy-3, dx+3, dy+3], fill=(255, 215, 0, 255))
        draw.ellipse([mid-3, mid-3, mid+3, mid+3], fill=(255, 215, 0, 255))

        img.save(path)
        print(f"Created: {path}")

    def create_checkmark_texture(path, size=16):
        """Create a green checkmark texture for collected items."""
        img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)

        # Green semi-transparent overlay
        draw.rectangle([0, 0, size-1, size-1], fill=(0, 200, 0, 120))

        # Checkmark (white)
        # Simple check: \
        points = [
            (2, 8), (5, 11), (13, 3)
        ]
        draw.line([(2, 8), (5, 11)], fill=(255, 255, 255, 255), width=2)
        draw.line([(5, 11), (13, 3)], fill=(255, 255, 255, 255), width=2)

        img.save(path)
        print(f"Created: {path}")

    # Output dir
    texture_dir = "src/main/resources/assets/bingo/textures/gui"
    os.makedirs(texture_dir, exist_ok=True)

    create_bingo_icon(os.path.join(texture_dir, "bingo_icon.png"), 32)
    create_checkmark_texture(os.path.join(texture_dir, "checkmark.png"), 16)

    # Also create the mod icon
    create_bingo_icon("src/main/resources/assets/bingo/icon.png", 128)

    print("\nAll textures generated successfully!")
    print("You can also replace these with custom pixel art for a more authentic look.")

except ImportError:
    print("Pillow not installed. Install with: pip install Pillow")
    print("Textures not generated - you can add them manually.")
    print("Required textures:")
    print("  - src/main/resources/assets/bingo/textures/gui/bingo_icon.png (32x32)")
    print("  - src/main/resources/assets/bingo/textures/gui/checkmark.png (16x16)")
    print("  - src/main/resources/assets/bingo/icon.png (128x128 or 64x64)")
