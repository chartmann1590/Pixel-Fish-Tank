# Pixel Fish Tank Promotional Video

This folder contains the promotional video for Pixel Fish Tank.

## Generated Files

- `pixel_fish_tank_promo.mp4` - The final promotional video (60 seconds)
- `voiceover.mp3` - Generated voiceover audio (if created)

## Creating the Video

To generate the promotional video, run from the project root:

```bash
pip install -r promo/requirements.txt
python create_promo_video.py
```

## Requirements

- Python 3.7+
- FFmpeg (must be installed separately)
  - Windows: Download from https://ffmpeg.org/download.html
  - Or use: `choco install ffmpeg` or `scoop install ffmpeg`
- Internet connection (for voiceover generation with gTTS)

## Video Details

- **Duration**: ~60 seconds
- **Resolution**: 1920x1080 (Full HD)
- **Frame Rate**: 30 fps
- **Format**: MP4 (H.264)

## Sections

1. Intro with feature image
2. Main tank view showcase
3. Mini-games feature
4. Decorations system
5. Store and upgrades
6. Offline-first feature
7. Website and call-to-action
8. Final logo/ending

## Customization

Edit `create_promo_video.py` to:
- Adjust video duration
- Change text overlays
- Modify transitions
- Update voiceover script
- Change color scheme

