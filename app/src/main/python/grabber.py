import yt_dlp


def grab_video_info(video_url):
    try:
        ydl = yt_dlp.YoutubeDL()
        video_info = ydl.extract_info(
            video_url,
            download = False,
            extra_info={
                "extra_info": {
                    "fields": ["title", "uploader", "view_count", "like_count"],
                    "thumbnails": True,
                    "thumbnail_quality": "high",
                    "download": False,
                    "format": "bestaudio/best",
                }
            },
        )

        return video_info

    except yt_dlp.DownloadError as e:
        print(f"Failed to grab video info: {str(e)}")
        return str(e).split(".")[0]


def get_video_info(video_url):
    video_info = grab_video_info(video_url)

    if isinstance(video_info, dict):
        new_dict = {
            "Title": video_info.get("title"),
            "Views": video_info.get("view_count"),
            "Likes": video_info.get("like_count"),
            "Error": "No Error Found",
            "Uploader": video_info.get("uploader"),
            "Channel URL": video_info.get("channel_url"),
            "High Quality Thumbnail": video_info.get("thumbnails", [])[40]["url"]
            if "thumbnails" in video_info
            else None,
        }
        # Return the extracted information as JSON response
        return new_dict
    elif isinstance(video_info, str):
        new_dict = {
            "Error": video_info,
        }
        return new_dict

def get_download_url(video_url):
    ydl_opts = {
        'format': 'best',
        'quiet': True,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info_dict = ydl.extract_info(video_url, download=False)
        download_url = info_dict['url']

    return download_url