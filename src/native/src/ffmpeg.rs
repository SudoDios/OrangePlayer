use std::process::Command;

pub fn is_ffmpeg_installed() -> bool {
    let output = Command::new("ffmpeg")
        .arg("-version")
        .output();
    match output {
        Ok(output) => output.status.success(),
        Err(_) => false,
    }
}

pub fn make_video_thumbnail(path : &str,thumbnail : &str) -> bool {
    let output = Command::new("ffmpeg")
        .arg("-i")
        .arg(path)
        .arg("-r")
        .arg("1")
        .arg("-an")
        .arg("-t")
        .arg("12")
        .arg("-s")
        .arg("512x288")
        .arg("-vsync")
        .arg("1")
        .arg("-threads")
        .arg("4")
        .arg(thumbnail)
        .output();
    match output {
        Ok(output) => output.status.success(),
        Err(_) => false,
    }
}