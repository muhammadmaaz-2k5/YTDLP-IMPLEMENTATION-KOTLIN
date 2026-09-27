"""Thin wrapper around yt-dlp's Python API, called from Kotlin via Chaquopy.

Two entry points, both called from ChaquopyYtDlpEngine:
  - fetch_metadata(url) -> JSON string (curated title/thumbnail/duration + quality options)
  - download(url, format_selector, out_template, ffmpeg_location, progress_sink) -> None

Format curation happens here (not in Kotlin) so the app doesn't need to replicate
yt-dlp's raw `formats` schema or ranking logic on the Kotlin side.
"""

import json
import os
import re
import urllib.request

import yt_dlp


def _label_for(fmt):
    if fmt.get("vcodec") in (None, "none") and fmt.get("acodec") not in (None, "none"):
        return f"Audio only ({fmt.get('acodec', '?')})"
    height = fmt.get("height")
    fps = fmt.get("fps")
    if height:
        label = f"{height}p"
        if fps and fps > 30:
            label += str(int(fps))
        return label
    return fmt.get("format_note") or fmt.get("format_id", "unknown")


def _size_of(fmt):
    return fmt.get("filesize") or fmt.get("filesize_approx")


def _curate_formats(info):
    formats = info.get("formats") or []
    usable = [f for f in formats if f.get("format_id") and f.get("ext") not in (None, "mhtml")]

    combined = [f for f in usable if f.get("vcodec") not in (None, "none") and f.get("acodec") not in (None, "none")]
    video_only = [f for f in usable if f.get("vcodec") not in (None, "none") and f.get("acodec") in (None, "none")]
    audio_only = [f for f in usable if f.get("vcodec") in (None, "none") and f.get("acodec") not in (None, "none")]

    best_audio = max(audio_only, key=lambda f: f.get("abr") or 0, default=None)

    curated = {}

    for f in combined:
        label = _label_for(f)
        curated[label] = {
            "label": label,
            "format_selector": f["format_id"],
            "ext": f.get("ext", "mp4"),
            "resolution": f"{f.get('width')}x{f.get('height')}" if f.get("height") else None,
            "fps": f.get("fps"),
            "filesize": _size_of(f),
            "requires_merge": False,
        }

    # Video-only + best audio is usually higher quality than an equivalent combined
    # format at the same resolution, so it's allowed to overwrite a `combined` entry
    # with the same label.
    if best_audio is not None:
        for f in video_only:
            label = _label_for(f)
            v_size = _size_of(f) or 0
            a_size = _size_of(best_audio) or 0
            curated[label] = {
                "label": label,
                "format_selector": f"{f['format_id']}+{best_audio['format_id']}",
                "ext": "mp4",
                "resolution": f"{f.get('width')}x{f.get('height')}" if f.get("height") else None,
                "fps": f.get("fps"),
                "filesize": (v_size + a_size) or None,
                "requires_merge": True,
            }

    # ALWAYS provide an Audio (HQ Audio / M4A) option if an audio stream is available
    if best_audio is not None:
        audio_abr = best_audio.get("abr")
        audio_label = f"Audio ({int(audio_abr)}kbps)" if audio_abr else "Audio Only (HQ)"
        curated["Audio Only"] = {
            "label": audio_label,
            "format_selector": best_audio["format_id"],
            "ext": best_audio.get("ext", "m4a"),
            "resolution": None,
            "fps": None,
            "filesize": _size_of(best_audio),
            "requires_merge": False,
        }

    def sort_key(item):
        res = item.get("resolution")
        if res and "x" in res:
            try:
                return -int(res.split("x")[1])
            except ValueError:
                pass
        return 999999

    return sorted(curated.values(), key=sort_key)[:8]


def fetch_metadata(url):
    opts = {
        "quiet": True,
        "no_warnings": True,
        "skip_download": True,
        "noplaylist": True,
        "socket_timeout": 20,
        "geo_bypass": True,
        "http_headers": {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
        },
    }
    with yt_dlp.YoutubeDL(opts) as ydl:
        info = ydl.extract_info(url, download=False)

    formats = _curate_formats(info)
    if not formats:
        # Single-file media (e.g. a direct image, or a site yt-dlp resolves to one file).
        formats = [{
            "label": "Best available",
            "format_selector": "best",
            "ext": info.get("ext", "mp4"),
            "resolution": None,
            "fps": None,
            "filesize": _size_of(info),
            "requires_merge": False,
        }]

    # Provide thumbnail as an image format option if available
    thumb_url = info.get("thumbnail") or ""
    if thumb_url:
        formats.append({
            "label": "Thumbnail (HD Image)",
            "format_selector": "thumbnail",
            "ext": "jpg",
            "resolution": "HD Image",
            "fps": None,
            "filesize": None,
            "requires_merge": False,
        })

    # Extract tags, keywords, and hashtags from tags/title/description
    desc = (info.get("description") or "").strip()
    raw_tags = info.get("tags") or []
    if not isinstance(raw_tags, list):
        raw_tags = []

    tags_list = [str(t).strip() for t in raw_tags if t and isinstance(t, str)]
    seen = set(t.lower() for t in tags_list)
    for word in re.findall(r'#([A-Za-z0-9_]+)', (info.get("title") or "") + " " + desc):
        if word.lower() not in seen:
            seen.add(word.lower())
            tags_list.append(word)

    result = {
        "id": info.get("id") or url,
        "title": info.get("title") or "Untitled",
        "thumbnail": thumb_url,
        "duration": info.get("duration"),
        "uploader": info.get("uploader"),
        "description": desc,
        "tags": tags_list[:30],
        "extractor_key": info.get("extractor_key") or "",
        "formats": formats,
    }
    return json.dumps(result, default=str)


def download(url, format_selector, out_template, ffmpeg_location, progress_sink):
    # Specialized high-speed download for thumbnail image
    if format_selector == "thumbnail":
        opts = {
            "quiet": True,
            "skip_download": True,
            "http_headers": {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
            },
        }
        with yt_dlp.YoutubeDL(opts) as ydl:
            info = ydl.extract_info(url, download=False)
        thumb = info.get("thumbnail")
        if not thumb:
            raise Exception("No thumbnail image found for this URL")

        title = (info.get("title") or "thumbnail")[:60]
        clean_title = re.sub(r'[\\/*?:"<>|]', "", title) or "thumbnail"
        parent_dir = os.path.dirname(out_template)
        ext = "jpg"
        if ".webp" in thumb.lower():
            ext = "webp"
        elif ".png" in thumb.lower():
            ext = "png"
        dest_file = os.path.join(parent_dir, f"{clean_title}.{ext}")

        progress_sink.onProgress(json.dumps({"status": "downloading", "downloaded_bytes": 0, "total_bytes": 100, "speed": 0}))
        req = urllib.request.Request(thumb, headers={"User-Agent": "Mozilla/5.0 Chrome/122.0.0.0 Safari/537.36"})
        with urllib.request.urlopen(req, timeout=20) as resp, open(dest_file, "wb") as f:
            data = resp.read()
            f.write(data)
        progress_sink.onProgress(json.dumps({"status": "finished", "downloaded_bytes": len(data), "total_bytes": len(data), "speed": 0}))
        return

    def hook(d):
        if progress_sink.isCancelled():
            raise yt_dlp.utils.DownloadCancelled("Cancelled by user")
        status = {
            "status": d.get("status"),
            "downloaded_bytes": d.get("downloaded_bytes"),
            "total_bytes": d.get("total_bytes") or d.get("total_bytes_estimate"),
            "speed": d.get("speed"),
            "eta": d.get("eta"),
        }
        progress_sink.onProgress(json.dumps(status, default=str))

    def postprocessor_hook(d):
        if d.get("status") == "started" and d.get("postprocessor") == "Merger":
            progress_sink.onMerging()

    # Format fallback: selector -> bestvideo+bestaudio -> best
    safe_format = f"{format_selector}/bestvideo+bestaudio/best" if "+" in format_selector else f"{format_selector}/best"

    opts = {
        "quiet": True,
        "no_warnings": True,
        "noplaylist": True,
        "socket_timeout": 25,
        "geo_bypass": True,
        "format": safe_format,
        "outtmpl": out_template,
        "ffmpeg_location": ffmpeg_location,
        "merge_output_format": "mp4",
        "progress_hooks": [hook],
        "postprocessor_hooks": [postprocessor_hook],
        "http_headers": {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
        },
    }
    with yt_dlp.YoutubeDL(opts) as ydl:
        ydl.download([url])

