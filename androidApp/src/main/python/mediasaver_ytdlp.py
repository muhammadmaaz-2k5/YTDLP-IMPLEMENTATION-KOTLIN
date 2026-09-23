"""Thin wrapper around yt-dlp's Python API, called from Kotlin via Chaquopy.

Two entry points, both called from ChaquopyYtDlpEngine:
  - fetch_metadata(url) -> JSON string (curated title/thumbnail/duration + quality options)
  - download(url, format_selector, out_template, ffmpeg_location, progress_sink) -> None

Format curation happens here (not in Kotlin) so the app doesn't need to replicate
yt-dlp's raw `formats` schema or ranking logic on the Kotlin side.
"""

import json

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

    if best_audio is not None and not combined and not video_only:
        curated["Audio only"] = {
            "label": "Audio only",
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
        return 0

    return sorted(curated.values(), key=sort_key)[:8]


def fetch_metadata(url):
    opts = {
        "quiet": True,
        "no_warnings": True,
        "skip_download": True,
        "noplaylist": True,
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

    result = {
        "id": info.get("id") or url,
        "title": info.get("title") or "Untitled",
        "thumbnail": info.get("thumbnail") or "",
        "duration": info.get("duration"),
        "uploader": info.get("uploader"),
        "extractor_key": info.get("extractor_key") or "",
        "formats": formats,
    }
    return json.dumps(result, default=str)


def download(url, format_selector, out_template, ffmpeg_location, progress_sink):
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

    opts = {
        "quiet": True,
        "no_warnings": True,
        "noplaylist": True,
        "format": format_selector,
        "outtmpl": out_template,
        "ffmpeg_location": ffmpeg_location,
        "merge_output_format": "mp4",
        "progress_hooks": [hook],
        "postprocessor_hooks": [postprocessor_hook],
    }
    with yt_dlp.YoutubeDL(opts) as ydl:
        ydl.download([url])
