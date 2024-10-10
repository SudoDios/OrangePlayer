use std::ffi::OsStr;
use std::path::{Path};
use jwalk::WalkDir;
use crate::utils;

const MEDIA_FORMATS: [&str; 11] = ["aac","avi","flac","m4a","mkv","mov","mp3","mp4","ogg","wav","wmv"];

pub struct Scanner<'a> {
    paths: &'a Vec<&'a str>,
}

#[derive(Debug)]
pub struct ScanResult {
    pub file_name: String,
    pub path: String,
    pub folder: String,
    pub hash: String,
    pub extension: String,
    pub size: u64,
}

impl Scanner<'_> {

    pub fn new<'a>(paths: &'a Vec<&'a str>) -> Scanner<'a> {
        Scanner { paths }
    }

    pub fn scan(&self) -> Vec<ScanResult> {
        let mut results = Vec::new();

        fn parse_path(path: &Path) -> Option<ScanResult> {
            if let Some(extension) = path.extension().and_then(OsStr::to_str) {
                if MEDIA_FORMATS.contains(&extension) {
                    let file_name = path.file_name()?.to_str()?.to_string();
                    let file_path = path.to_str()?.to_string();
                    let folder = path.parent()?.to_str()?.to_string();
                    let size = path.metadata().unwrap().len();
                    let hash = utils::hash_file(&file_path);
                    Some(ScanResult {
                        file_name,
                        path: file_path,
                        folder,
                        hash,
                        extension: extension.to_string(),
                        size
                    })
                } else {
                    None
                }
            } else {
                None
            }
        }

        for path in self.paths {
            let path = Path::new(path);
            if !path.is_dir() {
                if let Some(file) = parse_path(path) {
                    results.push(file)
                }
            } else {
                for entry in WalkDir::new(path).skip_hidden(true) {
                    let entry = entry.unwrap();
                    if !entry.file_type().is_dir() {
                        let path = entry.path();
                        if let Some(file) = parse_path(&path) {
                            results.push(file)
                        }
                    }
                }
            }
        };
        results
    }

}