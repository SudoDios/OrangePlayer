use std::path::Path;
use crate::DATABASE_PATH;
use rusqlite::{Connection, Statement};

#[derive(Debug)]
pub struct MediaItem {
    pub id: i32,
    pub name: String,
    pub album: String,
    pub artist: String,
    pub path: String,
    pub folder: String,
    pub hash: String,
    pub cover_path: String,
    pub width: i32,
    pub height: i32,
    pub is_fav: bool,
    pub is_video: bool,
    pub duration: u64,
    pub size: usize,
    pub extension: String,
}

#[derive(Debug)]
pub struct Playlists {
    pub id: i32,
    pub title: String,
    pub item_count: i32, // join
    pub cover_art: String, // join
}

pub fn init_database() {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    connection.execute(r#"
        CREATE TABLE IF NOT EXISTS media_items
            (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                name       TEXT,
                album      TEXT,
                artist     TEXT,
                path       TEXT UNIQUE,
                folder     TEXT,
                hash       TEXT,
                cover_path TEXT,
                width      INTEGER,
                height     INTEGER,
                is_fav     INTEGER,
                is_video   INTEGER,
                duration   INTEGER,
                size       INTEGER,
                extension  TEXT
            );
    "#, []).unwrap();
    connection.execute(r#"
            CREATE TABLE IF NOT EXISTS playlists
            (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                title      TEXT
            );
    "#, []).unwrap();
    connection.execute(r#"
            CREATE TABLE IF NOT EXISTS playlists_items
            (
                playlist_id     INTEGER NOT NULL,
                media_item_path TEXT    NOT NULL,
                PRIMARY KEY (playlist_id, media_item_path),
                FOREIGN KEY (media_item_path) REFERENCES media_items (path) ON DELETE CASCADE,
                FOREIGN KEY (playlist_id) REFERENCES playlists (id) ON DELETE CASCADE
            );
    "#, ()).unwrap();
    connection.execute(r#"
            CREATE TABLE IF NOT EXISTS waveforms
            (
                media_item_path TEXT NOT NULL,
                waveform_data TEXT NOT NULL,
                PRIMARY KEY (media_item_path),
                FOREIGN KEY (media_item_path) REFERENCES media_items (path) ON DELETE CASCADE
            );
    "#, ()).unwrap();
    connection.close().unwrap();
}
pub fn insert_items(media_items: &[MediaItem]) {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    clear_not_exists_files(&connection);
    let insert = |media_item: &MediaItem| {
        connection.execute(r#"
            INSERT INTO media_items (name, album, artist, path, folder ,hash , cover_path,width,height,
            is_fav,is_video, duration, size, extension) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14) ON CONFLICT(path)
            DO UPDATE SET name=?1, album=?2, artist=?3, folder=?5,hash=?6, cover_path=?7, width=?8, height=?9,
            is_video=?11, duration=?12, size=?13, extension=?14
            "#, (
            &media_item.name,
            &media_item.album,
            &media_item.artist,
            &media_item.path,
            &media_item.folder,
            &media_item.hash,
            &media_item.cover_path,
            &media_item.width,
            &media_item.height,
            &media_item.is_fav,
            &media_item.is_video,
            &media_item.duration,
            &media_item.size,
            &media_item.extension
        )).expect("Failed to insert media items");
    };
    for item in media_items.iter() {
        insert(item);
    };
    connection.close().unwrap();
}
pub fn add_media_to_favorites(path : &str) {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    connection.execute("UPDATE media_items SET is_fav=1 WHERE path=?1", [path]).unwrap();
    connection.close().unwrap()
}
pub fn remove_media_from_favorites(path : &str) {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    connection.execute("UPDATE media_items SET is_fav=0 WHERE path=?1", [path]).unwrap();
    connection.close().unwrap()
}
pub fn reset_database() {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    connection.execute("DELETE FROM media_items", ()).unwrap();
    connection.close().unwrap()
}
pub fn delete_item_by_path(path: &str) {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    connection.execute("DELETE FROM media_items WHERE path = ?1", [path]).unwrap();
    connection.close().unwrap()
}
pub fn read_items(raw_query: &str) -> (Vec<MediaItem>, i32, u64) {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    let query = format!("SELECT * FROM media_items{}", raw_query);
    let mut stmt = connection.prepare(&query).unwrap();
    read_media_items_stmt(&mut stmt)
}
pub fn count_media_items() -> i32 {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    let mut stmt = connection.prepare("SELECT COUNT(*) FROM media_items").unwrap();
    let mut row = stmt.raw_query();
    let first_row = row.next().unwrap();
    let count: i32 = first_row.unwrap().get(0).unwrap();
    count
}
//folders
pub fn read_folders() -> Vec<(String, i32)> {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    let mut stmt = connection.prepare("SELECT folder, COUNT(*) as row_count from media_items group by folder").unwrap();
    let result = stmt.query_map([], |row| {
        Ok((row.get(0)?, row.get(1)?))
    });
    match result {
        Ok(result) => {
            let result: Vec<(String, i32)> = result.map(|row| row.unwrap()).collect();
            result
        }
        Err(_) => {
            Vec::new()
        }
    }
}
//playlists
pub fn create_playlist(title : &str) -> i32 {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    connection.execute(r#"
            INSERT INTO playlists (title) VALUES (?1)
    "#,[title]).unwrap();
    let playlist_id = connection.last_insert_rowid() as i32;
    connection.close().unwrap();
    playlist_id
}
pub fn delete_playlist(id : i32) {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    connection.execute("DELETE FROM playlists WHERE id = ?1", [id]).unwrap();
    connection.close().unwrap()
}
pub fn update_playlist_title(id : i32,title : &str) {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    connection.execute("UPDATE playlists SET title=?1 WHERE id=?2", (title,id)).unwrap();
    connection.close().unwrap()
}
pub fn read_playlists() -> Vec<Playlists> {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    let mut stmt = connection.prepare(r#"
            SELECT
                p.id AS id,
                p.title AS title,
                COUNT(pi.media_item_path) AS item_count,
                COALESCE((
                    SELECT mi.cover_path
                    FROM playlists_items pi2
                             JOIN media_items mi ON pi2.media_item_path = mi.path
                    WHERE pi2.playlist_id = p.id AND mi.cover_path != ''
                    ORDER BY random()
                    LIMIT 1
                ),'') AS cover_art
            FROM
                playlists p LEFT JOIN playlists_items pi ON p.id = pi.playlist_id
            GROUP BY
                p.id, p.title
    "#).unwrap();
    let result = stmt.query_map([], |row| {
       Ok(Playlists {
           id : row.get(0)?,
           title : row.get(1)?,
           item_count : row.get(2)?,
           cover_art : row.get(3)?
       })
    });
    match result {
        Ok(result) => {
            let result: Vec<Playlists> = result.map(|row| row.unwrap()).collect();
            result
        }
        Err(_) => {
            Vec::new()
        }
    }
}
pub fn read_playlist_items(p_id : i32) -> (Vec<MediaItem>, i32, u64) {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    let query = format!(r#"
        SELECT mi.* FROM playlists_items pi JOIN media_items mi ON pi.media_item_path = mi.path
        WHERE pi.playlist_id = {}"#,p_id);
    let mut stmt = connection.prepare(&query).unwrap();
    read_media_items_stmt(&mut stmt)
}
pub fn add_media_item_to_playlist(p_id : i32,m_path : &str) {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    connection.execute(r#"
            INSERT INTO playlists_items (playlist_id,media_item_path) VALUES (?1,?2)
    "#,(p_id,m_path)).unwrap();
    connection.close().unwrap();
}
pub fn del_media_item_from_playlist(p_id : i32,m_path : &str) {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    connection.execute("DELETE FROM playlists_items WHERE playlist_id = ?1 AND media_item_path = ?2",(p_id,m_path)).unwrap();
    connection.close().unwrap();
}
pub fn media_item_playlists(m_path : &str) -> Vec<i32> {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    let query = format!("SELECT playlist_id FROM playlists_items WHERE media_item_path='{}'",m_path);
    let mut stmt = connection.prepare(&query).unwrap();
    let result = stmt.query_map([], |row| {
        row.get(0)
    });
    match result {
        Ok(result) => {
            let result: Vec<i32> = result.map(|row| row.unwrap()).collect();
            result
        }
        Err(_) => {
            Vec::new()
        }
    }
}

//waveform
pub fn save_media_waveform(m_path : &str,wave_data : &str) {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    connection.execute(r#"INSERT INTO waveforms (media_item_path,waveform_data) VALUES (?1,?2)
        ON CONFLICT(media_item_path) DO UPDATE SET waveform_data = ?2"#,(m_path,wave_data)).unwrap();
    connection.close().unwrap();
}

pub fn get_media_waveform(m_path : &str) -> String {
    let connection = Connection::open(DATABASE_PATH.get().unwrap()).unwrap();
    let query = format!("SELECT waveform_data from waveforms WHERE media_item_path = '{}'",m_path);
    let mut stmt = connection.prepare(&query).unwrap();
    let mut row = stmt.raw_query();
    let result = if let Some(row) = row.next().unwrap() {
        row.get(0).unwrap()
    } else {
        String::new()
    };
    result
}


//private methods
fn read_media_items_stmt(statement: &mut Statement) -> (Vec<MediaItem>, i32, u64) {
    let mut sum_duration : u64 = 0;
    let result = statement.query_map([], |row| {
        let duration : u64 = row.get(12).unwrap();
        sum_duration += duration;
        Ok(MediaItem {
            id: row.get(0)?,
            name: row.get(1)?,
            album: row.get(2)?,
            artist: row.get(3)?,
            path: row.get(4)?,
            folder: row.get(5)?,
            hash: row.get(6)?,
            cover_path: row.get(7)?,
            width: row.get(8)?,
            height: row.get(9)?,
            is_fav: row.get(10)?,
            is_video: row.get(11)?,
            duration,
            size: row.get(13)?,
            extension: row.get(14)?,
        })
    });
    match result {
        Ok(result) => {
            let result: Vec<MediaItem> = result.map(|row| row.unwrap()).collect();
            let item_count = result.len();
            (result, item_count as i32, sum_duration)
        }
        Err(_) => {
            (Vec::new(), 0, 0)
        }
    }
}
fn clear_not_exists_files(connection: &Connection) {
    let mut stmt = connection.prepare("SELECT path FROM media_items").unwrap();
    let media_paths = stmt.query_map([], |row| {
        row.get(0)
    });
    if let Ok(media_paths) = media_paths {
        let paths: Vec<String> = media_paths.map(|row| row.unwrap()).collect();
        for path in paths.iter() {
            let file_exists = Path::new(path).exists();
            if !file_exists {
                connection.execute("DELETE FROM media_items WHERE path=?1", [path]).unwrap();
            }
        }
    }
}