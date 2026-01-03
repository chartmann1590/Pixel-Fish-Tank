"""
Script to create a promotional video for Pixel Fish Tank app.
Requires: pip install moviepy pillow gtts
"""

import os
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import tempfile
from moviepy.editor import (
    ImageClip, CompositeVideoClip, concatenate_videoclips,
    AudioFileClip
)
from moviepy.video.fx.all import fadein, fadeout

# Paths
ROOT_DIR = Path(__file__).parent
SCREENSHOTS_DIR = ROOT_DIR / "screenshots"
ASSETS_DIR = ROOT_DIR / "assets"
PROMO_DIR = ROOT_DIR / "promo"
PROMO_DIR.mkdir(exist_ok=True)

# Video settings
VIDEO_DURATION = 60  # 60 seconds total
FPS = 30
RESOLUTION = (1920, 1080)  # Full HD

# Color scheme
BG_COLOR = "#1a1a2e"  # Dark blue
TEXT_COLOR = "#ffffff"  # White
ACCENT_COLOR = "#4ecdc4"  # Teal
WEBSITE_COLOR = "#ffd700"  # Gold

def hex_to_rgb(hex_color):
    """Convert hex color to RGB tuple"""
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))

def create_text_image(text, width, fontsize, color, bg_color=None, add_background=True):
    """Create a text image using PIL with optional black background"""
    # Try to use a nice font, fallback to default
    try:
        # Try Windows fonts
        font_paths = [
            "C:/Windows/Fonts/arial.ttf",
            "C:/Windows/Fonts/arialbd.ttf",
            "C:/Windows/Fonts/calibri.ttf",
        ]
        font = None
        for path in font_paths:
            if os.path.exists(path):
                try:
                    font = ImageFont.truetype(path, fontsize)
                    break
                except:
                    continue
        if font is None:
            font = ImageFont.load_default()
    except:
        font = ImageFont.load_default()
    
    # Create a temporary image to measure text
    temp_img = Image.new('RGB', (width, 100))
    temp_draw = ImageDraw.Draw(temp_img)
    
    # Word wrap text
    words = text.split(' ')
    lines = []
    current_line = []
    current_width = 0
    
    for word in words:
        bbox = temp_draw.textbbox((0, 0), ' '.join(current_line + [word]), font=font)
        word_width = bbox[2] - bbox[0]
        
        if current_width + word_width < width * 0.9:
            current_line.append(word)
            current_width += word_width + temp_draw.textbbox((0, 0), ' ', font=font)[2]
        else:
            if current_line:
                lines.append(' '.join(current_line))
            current_line = [word]
            current_width = word_width
    
    if current_line:
        lines.append(' '.join(current_line))
    
    # Calculate text dimensions with padding for background
    line_height = fontsize + 10
    padding = 20 if add_background else 10
    text_height = len(lines) * line_height + padding * 2
    
    # Create image with black semi-transparent background if requested
    if add_background:
        img = Image.new('RGBA', (width, text_height), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
        # Draw semi-transparent black background
        bg_padding = 10
        for i, line in enumerate(lines):
            bbox = draw.textbbox((0, 0), line, font=font)
            line_width = bbox[2] - bbox[0]
            line_x = (width - line_width) // 2
            line_y = padding + i * line_height
            # Draw black rectangle behind text
            draw.rectangle(
                [line_x - bg_padding, line_y - 5, line_x + line_width + bg_padding, line_y + fontsize + 5],
                fill=(0, 0, 0, 200)  # Semi-transparent black
            )
    else:
        if bg_color:
            img = Image.new('RGB', (width, text_height), hex_to_rgb(bg_color))
        else:
            img = Image.new('RGBA', (width, text_height), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
    
    rgb_color = hex_to_rgb(color)
    
    # Draw text
    y = padding
    for line in lines:
        bbox = draw.textbbox((0, 0), line, font=font)
        text_width = bbox[2] - bbox[0]
        x = (width - text_width) // 2
        draw.text((x, y), line, fill=rgb_color, font=font)
        y += line_height
    
    # Save to temporary file
    temp_file = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
    img.save(temp_file.name)
    return temp_file.name

def create_text_clip(text, duration, position=('center', 'center'), 
                     fontsize=60, color=TEXT_COLOR, font='Arial-Bold', add_background=True):
    """Create a text clip using PIL-generated images with black background"""
    width = int(RESOLUTION[0] * 0.8)
    text_img_path = create_text_image(text, width, fontsize, color, add_background=add_background)
    txt_clip = ImageClip(text_img_path).set_duration(duration).set_position(position)
    return txt_clip

def create_image_clip(image_path, duration, start_time=0):
    """Create an image clip with fade transitions - screenshots appear on top"""
    if not os.path.exists(image_path):
        print(f"Warning: {image_path} not found")
        return None
    
    try:
        # Try multiple methods to load the image
        pil_img = None
        
        # Method 1: Try PIL directly
        try:
            pil_img = Image.open(image_path)
        except Exception as e1:
            print(f"PIL failed for {image_path}: {e1}")
            # Method 2: Try with imageio
            try:
                import imageio
                img_array = imageio.imread(image_path)
                pil_img = Image.fromarray(img_array)
            except Exception as e2:
                print(f"imageio failed for {image_path}: {e2}")
                # Method 3: Try opening as raw bytes
                try:
                    with open(image_path, 'rb') as f:
                        data = f.read()
                    from io import BytesIO
                    pil_img = Image.open(BytesIO(data))
                except Exception as e3:
                    print(f"All methods failed for {image_path}: {e3}")
                    return None
        
        if pil_img is None:
            return None
            
        # Convert to RGB if necessary
        if pil_img.mode != 'RGB':
            pil_img = pil_img.convert('RGB')
        
        # Save to temporary file
        temp_file = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
        pil_img.save(temp_file.name, 'PNG')
        temp_path = temp_file.name
        
        clip = ImageClip(temp_path).set_duration(duration).set_start(start_time)
        # Make screenshot larger and position below title text
        clip = clip.resize(height=RESOLUTION[1] * 0.65)  # Slightly smaller to fit better
        # Position below title text (title is at 5%, move screenshot to 18% to give space)
        clip = clip.set_position(('center', RESOLUTION[1] * 0.18))
        # Add fade in/out using fx method
        clip = clip.fx(fadein, 0.5).fx(fadeout, 0.5)
        return clip
    except Exception as e:
        print(f"Error loading image {image_path}: {e}")
        import traceback
        traceback.print_exc()
        return None

def create_section(title, subtitle, image_path, duration, start_time):
    """Create a video section with title, subtitle, and image - screenshot on top"""
    clips = []
    
    # Background - load with PIL first
    bg_path = ASSETS_DIR / "tank" / "Main_Tank_Background.png"
    try:
        pil_bg = Image.open(bg_path)
        if pil_bg.mode != 'RGB':
            pil_bg = pil_bg.convert('RGB')
        temp_bg = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
        pil_bg.save(temp_bg.name, 'PNG')
        bg = ImageClip(temp_bg.name)
        bg = bg.resize(RESOLUTION).set_duration(duration).set_start(start_time)
        clips.append(bg)
    except Exception as e:
        print(f"Warning: Could not load background: {e}")
        # Create a solid color background as fallback
        bg_img = Image.new('RGB', RESOLUTION, hex_to_rgb(BG_COLOR))
        temp_bg = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
        bg_img.save(temp_bg.name, 'PNG')
        bg = ImageClip(temp_bg.name).set_duration(duration).set_start(start_time)
        clips.append(bg)
    
    # Title text (with black background) - appears first in layer order
    title_clip = create_text_clip(
        title, duration - 1, 
        position=('center', RESOLUTION[1] * 0.05),
        fontsize=70,
        color=ACCENT_COLOR,
        add_background=True
    ).set_start(start_time + 0.5)
    clips.append(title_clip)
    
    # Main image (screenshot) - appears on top after text
    if image_path and os.path.exists(image_path):
        img_clip = create_image_clip(image_path, duration - 1, start_time + 0.5)
        if img_clip:
            clips.append(img_clip)  # Added after text, so appears on top
    
    # Subtitle text (with black background) - appears last, so on top of screenshot
    if subtitle:
        subtitle_clip = create_text_clip(
            subtitle, duration - 1,
            position=('center', RESOLUTION[1] * 0.85),
            fontsize=40,
            color=TEXT_COLOR,
            add_background=True
        ).set_start(start_time + 0.5)
        clips.append(subtitle_clip)  # Added last, appears on top
    
    return clips

def generate_voiceover():
    """Generate voiceover audio using text-to-speech"""
    script = """
    Welcome to Pixel Fish Tank! A cozy virtual pet game where you care for your adorable pixel-art fish.
    
    Feed your fish to keep it happy and healthy. Watch as your fish grows and evolves through your care.
    
    Play fun mini-games to earn coins and experience points. Challenge yourself to beat your high scores!
    
    Decorate your tank with plants, rocks, and toys. Make your fish's home unique and beautiful.
    
    Complete daily tasks to maintain streaks and earn rewards. Build a routine of care!
    
    Pixel Fish Tank works completely offline. All your progress is saved locally on your device.
    
    Available soon on Google Play Store. Visit pixel-fish-tank dot web dot app for more information, screenshots, and updates.
    
    Download Pixel Fish Tank today and start your virtual pet journey!
    """
    
    try:
        from gtts import gTTS
        import tempfile
        
        tts = gTTS(text=script, lang='en', slow=False)
        audio_path = PROMO_DIR / "voiceover.mp3"
        tts.save(str(audio_path))
        print(f"Voiceover saved to {audio_path}")
        return str(audio_path)
    except Exception as e:
        print(f"Could not generate voiceover with gTTS: {e}")
        print("You can manually add a voiceover later or install: pip install gtts")
        return None

def main():
    print("Creating promotional video for Pixel Fish Tank...")
    
    # Generate voiceover
    print("Generating voiceover...")
    audio_path = generate_voiceover()
    
    # Create video sections
    all_clips = []
    current_time = 0
    
    # Section 1: Intro with feature image
    print("Creating intro section...")
    feature_img = ASSETS_DIR / "feature" / "feature.png"
    intro_clips = create_section(
        "Pixel Fish Tank",
        "A cozy virtual pet game",
        str(feature_img),
        5,
        current_time
    )
    all_clips.extend(intro_clips)
    current_time += 5
    
    # Section 2: Main tank view
    print("Creating tank view section...")
    tank_img = SCREENSHOTS_DIR / "tankview.png"
    tank_clips = create_section(
        "Care for Your Fish",
        "Feed, clean, and watch your fish grow",
        str(tank_img),
        6,
        current_time
    )
    all_clips.extend(tank_clips)
    current_time += 6
    
    # Section 3: Mini-games
    print("Creating mini-games section...")
    minigame_img = SCREENSHOTS_DIR / "minigames.png"
    minigame_clips = create_section(
        "Play Mini-Games",
        "Earn coins and XP through fun challenges",
        str(minigame_img),
        6,
        current_time
    )
    all_clips.extend(minigame_clips)
    current_time += 6
    
    # Section 4: Decorations
    print("Creating decorations section...")
    decor_img = SCREENSHOTS_DIR / "decorationsview.png"
    decor_clips = create_section(
        "Customize Your Tank",
        "Decorate with plants, rocks, and toys",
        str(decor_img),
        6,
        current_time
    )
    all_clips.extend(decor_clips)
    current_time += 6
    
    # Section 5: Store
    print("Creating store section...")
    store_img = SCREENSHOTS_DIR / "storview.png"
    store_clips = create_section(
        "Shop & Upgrade",
        "Unlock new items and decorations",
        str(store_img),
        6,
        current_time
    )
    all_clips.extend(store_clips)
    current_time += 6
    
    # Section 6: Features showcase
    print("Creating features section...")
    widgets_img = SCREENSHOTS_DIR / "widgets.png"
    features_clips = create_section(
        "Offline-First",
        "Play anywhere, anytime - no internet needed",
        str(widgets_img),
        6,
        current_time
    )
    all_clips.extend(features_clips)
    current_time += 6
    
    # Section 7: Website and call to action
    print("Creating call-to-action section...")
    website_clips = []
    bg_path = ASSETS_DIR / "tank" / "Main_Tank_Background.png"
    try:
        pil_bg = Image.open(bg_path)
        if pil_bg.mode != 'RGB':
            pil_bg = pil_bg.convert('RGB')
        temp_bg = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
        pil_bg.save(temp_bg.name, 'PNG')
        bg = ImageClip(temp_bg.name)
        bg = bg.resize(RESOLUTION).set_duration(8).set_start(current_time)
        website_clips.append(bg)
    except Exception as e:
        print(f"Warning: Could not load background: {e}")
        bg_img = Image.new('RGB', RESOLUTION, hex_to_rgb(BG_COLOR))
        temp_bg = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
        bg_img.save(temp_bg.name, 'PNG')
        bg = ImageClip(temp_bg.name).set_duration(8).set_start(current_time)
        website_clips.append(bg)
    
    # Website label (with black background)
    website_label = create_text_clip(
        "Visit us at:",
        8,
        position=('center', RESOLUTION[1] * 0.35),
        fontsize=40,
        color=TEXT_COLOR,
        add_background=True
    ).set_start(current_time + 1)
    website_clips.append(website_label)
    
    # Website URL (more prominent, with black background)
    website_text = create_text_clip(
        "https://pixel-fish-tank.web.app",
        8,
        position=('center', RESOLUTION[1] * 0.45),
        fontsize=55,
        color=WEBSITE_COLOR,
        add_background=True
    ).set_start(current_time + 1)
    website_clips.append(website_text)
    
    # Call to action (with black background)
    cta_text = create_text_clip(
        "Available Soon on Google Play Store",
        8,
        position=('center', RESOLUTION[1] * 0.65),
        fontsize=60,
        color=ACCENT_COLOR,
        add_background=True
    ).set_start(current_time + 1)
    website_clips.append(cta_text)
    
    # App icon
    icon_path = ASSETS_DIR / "icon" / "icon.png"
    if os.path.exists(icon_path):
        try:
            pil_icon = Image.open(icon_path)
            if pil_icon.mode != 'RGB':
                pil_icon = pil_icon.convert('RGB')
            temp_icon = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
            pil_icon.save(temp_icon.name, 'PNG')
            icon_clip = ImageClip(temp_icon.name)
            icon_clip = icon_clip.resize(height=200).set_duration(8).set_start(current_time + 1)
            icon_clip = icon_clip.set_position(('center', RESOLUTION[1] * 0.2))
            icon_clip = icon_clip.fx(fadein, 0.5).fx(fadeout, 0.5)
            website_clips.append(icon_clip)
        except Exception as e:
            print(f"Warning: Could not load icon: {e}")
    
    all_clips.extend(website_clips)
    current_time += 8
    
    # Section 8: Final logo/ending
    print("Creating ending section...")
    ending_clips = []
    bg_path = ASSETS_DIR / "tank" / "Main_Tank_Background.png"
    try:
        pil_bg = Image.open(bg_path)
        if pil_bg.mode != 'RGB':
            pil_bg = pil_bg.convert('RGB')
        temp_bg = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
        pil_bg.save(temp_bg.name, 'PNG')
        bg = ImageClip(temp_bg.name)
        bg = bg.resize(RESOLUTION).set_duration(5).set_start(current_time)
        ending_clips.append(bg)
    except Exception as e:
        print(f"Warning: Could not load background: {e}")
        bg_img = Image.new('RGB', RESOLUTION, hex_to_rgb(BG_COLOR))
        temp_bg = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
        bg_img.save(temp_bg.name, 'PNG')
        bg = ImageClip(temp_bg.name).set_duration(5).set_start(current_time)
        ending_clips.append(bg)
    
    final_text = create_text_clip(
        "Pixel Fish Tank",
        5,
        position=('center', 'center'),
        fontsize=80,
        color=ACCENT_COLOR,
        add_background=True
    ).set_start(current_time + 0.5)
    ending_clips.append(final_text)
    
    tagline = create_text_clip(
        "Start your virtual pet journey today!",
        5,
        position=('center', RESOLUTION[1] * 0.6),
        fontsize=40,
        color=TEXT_COLOR,
        add_background=True
    ).set_start(current_time + 0.5)
    ending_clips.append(tagline)
    
    all_clips.extend(ending_clips)
    
    # Composite all clips
    print("Compositing video...")
    final_video = CompositeVideoClip(all_clips, size=RESOLUTION)
    
    # Add audio if available
    if audio_path and os.path.exists(audio_path):
        print("Adding voiceover audio...")
        try:
            audio = AudioFileClip(audio_path)
            # Trim or loop audio to match video duration
            if audio.duration < final_video.duration:
                # Loop audio if needed
                from moviepy.audio.AudioClip import concatenate_audioclips
                loops_needed = int(final_video.duration / audio.duration) + 1
                audio = concatenate_audioclips([audio] * loops_needed)
            audio = audio.subclip(0, final_video.duration)
            final_video = final_video.set_audio(audio)
        except Exception as e:
            print(f"Could not add audio: {e}")
    
    # Export video
    output_path = PROMO_DIR / "pixel_fish_tank_promo.mp4"
    print(f"Exporting video to {output_path}...")
    print("This may take a few minutes...")
    
    final_video.write_videofile(
        str(output_path),
        fps=FPS,
        codec='libx264',
        audio_codec='aac',
        bitrate='8000k',
        preset='medium'
    )
    
    print(f"\n[SUCCESS] Promotional video created successfully!")
    print(f"[VIDEO] Video saved to: {output_path}")
    print(f"[DURATION] Duration: {final_video.duration:.1f} seconds")
    print(f"[RESOLUTION] Resolution: {RESOLUTION[0]}x{RESOLUTION[1]}")

if __name__ == "__main__":
    main()

