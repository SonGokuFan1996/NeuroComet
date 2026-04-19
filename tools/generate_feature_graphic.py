import sys
import os
import math

try:
    from PIL import Image, ImageDraw, ImageFont, ImageFilter
except ImportError:
    print("Error: The 'Pillow' library is required. Please run: pip install Pillow")
    sys.exit(1)

def draw_infinity(draw, center_x, center_y, width, height, colors, stroke_width, alpha=255):
    """
    Draws a 1:1 professional infinity symbol matching the app's cubic bezier path.
    """
    loop_w = width * 0.35
    loop_h = height * 0.38
    
    steps = 400
    for s in range(steps):
        t = s / steps
        segment_t = (t * 4) % 1.0
        
        if t < 0.25: # Right Top
            p0 = (center_x, center_y)
            p1 = (center_x + loop_w * 0.5, center_y - loop_h * 0.9)
            p2 = (center_x + loop_w * 0.95, center_y - loop_h * 0.7)
            p3 = (center_x + loop_w, center_y)
        elif t < 0.5: # Right Bottom
            p0 = (center_x + loop_w, center_y)
            p1 = (center_x + loop_w * 0.95, center_y + loop_h * 0.7)
            p2 = (center_x + loop_w * 0.5, center_y + loop_h * 0.9)
            p3 = (center_x, center_y)
        elif t < 0.75: # Left Bottom
            p0 = (center_x, center_y)
            p1 = (center_x - loop_w * 0.5, center_y + loop_h * 0.9)
            p2 = (center_x - loop_w * 0.95, center_y + loop_h * 0.7)
            p3 = (center_x - loop_w, center_y)
        else: # Left Top
            p0 = (center_x - loop_w, center_y)
            p1 = (center_x - loop_w * 0.95, center_y - loop_h * 0.7)
            p2 = (center_x - loop_w * 0.5, center_y - loop_h * 0.9)
            p3 = (center_x, center_y)

        cx = (1-segment_t)**3 * p0[0] + 3*(1-segment_t)**2 * segment_t * p1[0] + 3*(1-segment_t) * segment_t**2 * p2[0] + segment_t**3 * p3[0]
        cy = (1-segment_t)**3 * p0[1] + 3*(1-segment_t)**2 * segment_t * p1[1] + 3*(1-segment_t) * segment_t**2 * p2[1] + segment_t**3 * p3[1]
        
        num_colors = len(colors)
        color_pos = t * (num_colors - 1)
        color_idx = int(color_pos)
        blend = color_pos - color_idx
        
        c1 = colors[color_idx]
        c2 = colors[min(color_idx + 1, num_colors - 1)]
        curr_color = (
            int(c1[0] + (c2[0] - c1[0]) * blend),
            int(c1[1] + (c2[1] - c1[1]) * blend),
            int(c1[2] + (c2[2] - c1[2]) * blend),
            alpha
        )
        
        draw.ellipse([cx - stroke_width/2, cy - stroke_width/2, cx + stroke_width/2, cy + stroke_width/2], fill=curr_color)

def generate_feature_graphic(output_path="feature_graphic.png"):
    WIDTH, HEIGHT = 1024, 500
    
    # 1. Background: Deep Indigo Gradient
    base_color = (26, 26, 46) # #1a1a2e
    secondary_color = (46, 46, 86) # #2e2e56
    
    img = Image.new('RGB', (WIDTH, HEIGHT), base_color)
    draw = ImageDraw.Draw(img)
    
    for y in range(HEIGHT):
        r = int(base_color[0] + (secondary_color[0] - base_color[0]) * (y / HEIGHT))
        g = int(base_color[1] + (secondary_color[1] - base_color[1]) * (y / HEIGHT))
        b = int(base_color[2] + (secondary_color[2] - base_color[2]) * (y / HEIGHT))
        draw.line([(0, y), (WIDTH, y)], fill=(r, g, b))

    # 2. Rainbow Colors
    rainbow_colors = [
        (229, 115, 115), (255, 183, 77), (255, 241, 118), (129, 199, 132),
        (100, 181, 246), (186, 104, 200), (244, 143, 177), (229, 115, 115)
    ]

    # 3. Balanced Layout Calculation
    # Let's shift the whole "Logo + Text" block to the right to remove left blank space
    center_x = 295 # Shifted right from 260 to reduce left blank space
    center_y = HEIGHT // 2
    logo_w, logo_h = 410, 410 # Increased back slightly for better presence
    
    # Layer 1: Multi-layered Glow
    glow_img = Image.new('RGBA', (WIDTH, HEIGHT), (0,0,0,0))
    glow_draw = ImageDraw.Draw(glow_img)
    draw_infinity(glow_draw, center_x, center_y, logo_w * 1.1, logo_h * 1.1, rainbow_colors, 42, alpha=30)
    draw_infinity(glow_draw, center_x, center_y, logo_w * 1.02, logo_h * 1.02, rainbow_colors, 22, alpha=60)
    glow_img = glow_img.filter(ImageFilter.GaussianBlur(12))
    img.paste(glow_img, (0,0), glow_img)

    # Layer 2: Main Infinity Path
    main_draw = ImageDraw.Draw(img)
    draw_infinity(main_draw, center_x, center_y, logo_w, logo_h, rainbow_colors, 18)

    # 4. Text Content (Centered relative to the new logo position)
    try:
        font_path = "C:/Windows/Fonts/segoeuib.ttf"
        if not os.path.exists(font_path):
            font_path = "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf"
        
        title_font = ImageFont.truetype(font_path, 84)
        tagline_font = ImageFont.truetype(font_path, 34)
        feature_font = ImageFont.truetype(font_path, 28)
    except:
        title_font = ImageFont.load_default()
        tagline_font = ImageFont.load_default()
        feature_font = ImageFont.load_default()

    TEXT_X = 525 # Adjusted to maintain good gap with the logo
    
    # Draw Title
    main_draw.text((TEXT_X, 165), "NeuroComet", fill=(255, 255, 255), font=title_font)
    
    # Draw Tagline
    main_draw.text((TEXT_X, 275), "A safe space for every mind", fill=(210, 210, 255), font=tagline_font)
    
    # 5. Features List
    features = ["Sensory-Friendly", "ND-Affirming", "LGBTQ+ Inclusive"]
    for i, feat in enumerate(features):
        main_draw.text((TEXT_X, 335 + (i * 40)), f"• {feat}", fill=(180, 180, 230), font=feature_font)

    # Save final image
    img.save(output_path)
    print(f"Success! Balanced 1:1 Feature graphic saved to: {os.path.abspath(output_path)}")

if __name__ == "__main__":
    generate_feature_graphic()
