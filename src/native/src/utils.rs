use std::fs::File;
use std::io::{Read, Seek, SeekFrom};

const BUFFER_SIZE: usize = 4096;

pub fn hash_file(path: &str) -> String {
    let mut f = File::open(path).unwrap();
    let mut context = md5::Context::new();
    let size = f.metadata().unwrap().len();

    if size < (BUFFER_SIZE * 3) as u64 {
        let mut buff = vec![0u8; size as usize];
        f.read_to_end(&mut buff).unwrap();
        context.consume(&buff);
    } else {
        let mut buff = vec![0u8; BUFFER_SIZE];
        f.seek(SeekFrom::Start(0)).unwrap();
        f.read_exact(&mut buff).unwrap();
        context.consume(&buff);
        f.seek(SeekFrom::Start(size - BUFFER_SIZE as u64)).unwrap();
        f.read_exact(&mut buff).unwrap();
        context.consume(&buff);
    }

    let digest = context.compute();
    let result = format!("{:x}", digest);
    result
}