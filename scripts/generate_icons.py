import os
from PIL import Image, ImageDraw

def render_icon(size, is_round=False):
    # Supersampling factor for smooth vector-like rasterization
    ss = 4
    canvas_size = size * ss
    img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    scale = canvas_size / 108.0
    c = canvas_size / 2.0

    # Background
    bg_color = (16, 20, 38, 255) # #101426
    if is_round:
        draw.ellipse([0, 0, canvas_size - 1, canvas_size - 1], fill=bg_color)
    else:
        corner_r = canvas_size * 0.22
        draw.rounded_rectangle([0, 0, canvas_size - 1, canvas_size - 1], radius=corner_r, fill=bg_color)

    # Ambient aura
    aura_r = 28 * scale
    for r in range(int(aura_r), 0, -2):
        alpha = int(22 * (1.0 - r / aura_r))
        draw.ellipse([c - r, c - r, c + r, c + r], fill=(200, 162, 74, alpha))

    # Scroll parchment backing
    p_left = 32 * scale
    p_right = 76 * scale
    p_top = 32 * scale
    p_bottom = 76 * scale
    draw.rounded_rectangle([p_left, p_top, p_right, p_bottom], radius=2 * scale, fill=(27, 34, 61, 255), outline=(200, 162, 74, 255), width=max(1, int(1.5 * scale)))

    # Inner margin
    m_inset = 4 * scale
    draw.rectangle([p_left + m_inset, p_top + m_inset, p_right - m_inset, p_bottom - m_inset], outline=(200, 162, 74, 100), width=max(1, int(0.8 * scale)))

    # Roller bars
    # Top bar
    tb_left = 28 * scale
    tb_right = 80 * scale
    tb_top = 30 * scale
    tb_bottom = 34 * scale
    draw.rounded_rectangle([tb_left, tb_top, tb_right, tb_bottom], radius=2 * scale, fill=(235, 205, 130, 255))
    # Top bar finials
    f_r = 2 * scale
    draw.ellipse([25 * scale - f_r, 32 * scale - f_r, 25 * scale + f_r, 32 * scale + f_r], fill=(200, 162, 74, 255))
    draw.ellipse([83 * scale - f_r, 32 * scale - f_r, 83 * scale + f_r, 32 * scale + f_r], fill=(200, 162, 74, 255))

    # Bottom bar
    bb_top = 74 * scale
    bb_bottom = 78 * scale
    draw.rounded_rectangle([tb_left, bb_top, tb_right, bb_bottom], radius=2 * scale, fill=(235, 205, 130, 255))
    # Bottom bar finials
    draw.ellipse([25 * scale - f_r, 76 * scale - f_r, 25 * scale + f_r, 76 * scale + f_r], fill=(200, 162, 74, 255))
    draw.ellipse([83 * scale - f_r, 76 * scale - f_r, 83 * scale + f_r, 76 * scale + f_r], fill=(200, 162, 74, 255))

    # Central golden bookmark / danda (M52,28h4v52h-4z)
    d_left = 52 * scale
    d_right = 56 * scale
    d_top = 28 * scale
    d_bottom = 80 * scale
    draw.rectangle([d_left, d_top, d_right, d_bottom], fill=(200, 162, 74, 255))

    # Finial triangle at top and tail at bottom
    draw.polygon([(c, 24 * scale), (d_right + scale, 28 * scale), (d_left - scale, 28 * scale)], fill=(235, 205, 130, 255))
    draw.polygon([(d_left, 80 * scale), (d_right, 80 * scale), (c, 84 * scale)], fill=(235, 205, 130, 255))

    # High quality resize
    return img.resize((size, size), Image.Resampling.LANCZOS)

def main():
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    base_res = "app/src/main/res"
    for folder, size in densities.items():
        dir_path = os.path.join(base_res, folder)
        os.makedirs(dir_path, exist_ok=True)
        
        # ic_launcher.png
        icon = render_icon(size, is_round=False)
        icon.save(os.path.join(dir_path, "ic_launcher.png"), "PNG")
        
        # ic_launcher_round.png
        icon_round = render_icon(size, is_round=True)
        icon_round.save(os.path.join(dir_path, "ic_launcher_round.png"), "PNG")
        print(f"Generated {folder}: {size}x{size}")

    # Store listing high-res icon 512x512
    os.makedirs("docs/store_assets", exist_ok=True)
    icon_512 = render_icon(512, is_round=False)
    icon_512.save("docs/store_assets/icon_512.png", "PNG")
    print("Generated docs/store_assets/icon_512.png (512x512)")

if __name__ == "__main__":
    main()
