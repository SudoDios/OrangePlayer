mod scanner;
mod jni_helper;
mod database;
mod ffmpeg;
pub mod utils;

use std::fs::File;
use std::sync::OnceLock;
use jni::JNIEnv;
use jni::objects::{JByteBuffer, JClass, JIntArray, JObjectArray, JString};
use jni::sys::{jboolean, jint, jlong, jobject, jobjectArray, jsize, jstring};
use tar::Archive;
use xz2::read::XzDecoder;
use crate::jni_helper::{convert_insert_media_items, convert_scan_folders, convert_scan_result_to_obj_array, return_folders_list, return_media_list_analyze, return_playlists_list};
use crate::scanner::Scanner;

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_version<'local>(env: JNIEnv<'local>, _class: JClass<'local>) -> jstring {
    env.new_string("1.0.0").unwrap().into_raw()
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_searchMedia<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                    folders : JObjectArray<'local>) -> jobjectArray {
    let scan_folders = convert_scan_folders(&mut env,&folders);
    let scan_folders = scan_folders.iter().map(|item| &**item).collect();
    let scanner = Scanner::new(&scan_folders);
    let result = scanner.scan();
    convert_scan_result_to_obj_array(env, result).into_raw()
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_extractArchive<'local>(mut env: JNIEnv<'local>,
                                                                                  _class: JClass<'local>,
                                                                                  sourceDir : JString<'local>,
                                                                                  destDir : JString<'local>) {
    let sourceDir : String = env.get_string(&sourceDir).unwrap().into();
    let destDir : String = env.get_string(&destDir).unwrap().into();
    let zip_file = File::open(sourceDir).unwrap();
    let tar = XzDecoder::new(zip_file);
    let mut archive = Archive::new(tar);
    archive.unpack(destDir).unwrap();
}


// database
pub static DATABASE_PATH: OnceLock<String> = OnceLock::new();
#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_initDatabase<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                     dbPath : JString<'local>) {
    let dbPath : String = env.get_string(&dbPath).unwrap().into();
    DATABASE_PATH.get_or_init(|| dbPath);
    database::init_database()
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbInsertItems<'local>(env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                    mediaItems : JObjectArray<'local>) {
    let media_items = convert_insert_media_items(env,&mediaItems);
    database::insert_items(&media_items)
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbAddMediaToFav<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                            path : JString<'local>) {
    let path : String = env.get_string(&path).unwrap().into();
    database::add_media_to_favorites(&path)
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbRemoveMediaFromFav<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                            path : JString<'local>) {
    let path : String = env.get_string(&path).unwrap().into();
    database::remove_media_from_favorites(&path)
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbReset<'local>(_: JNIEnv<'local>, _class: JClass<'local>) {
    database::reset_database()
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbDeleteItemByPath<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                     path : JString<'local>) {
    let path : String = env.get_string(&path).unwrap().into();
    database::delete_item_by_path(&path)
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbReadMediaItems<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                           rawQuery : JString<'local>) -> jobject {
    let raw_query : String = env.get_string(&rawQuery).unwrap().into();
    let result = database::read_items(&raw_query);
    return_media_list_analyze(env, result).into_raw()
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbCountMediaItems<'local>(_env: JNIEnv<'local>, _class: JClass<'local>) -> jint {
    database::count_media_items()
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbReadFolders<'local>(env: JNIEnv<'local>, _class: JClass<'local>) -> jobjectArray {
    let result = database::read_folders();
    return_folders_list(env,result).into_raw()
}

//playlists
#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbPlaylistsCreate<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                           title : JString<'local>) -> jint {
    let title : String = env.get_string(&title).unwrap().into();
    database::create_playlist(&title)
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbPlaylistsDelete<'local>(_env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                           p_id : jint) {
    database::delete_playlist(p_id);
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbPlaylistsUpdate<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                           p_id : jint,
                                                                                           title : JString<'local>) {
    let title : String = env.get_string(&title).unwrap().into();
    database::update_playlist_title(p_id,&title);
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbPlaylistsRead<'local>(env: JNIEnv<'local>, _class: JClass<'local>) -> jobjectArray {
    let result = database::read_playlists();
    return_playlists_list(env,result).into_raw()
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbPlaylistsReadItems<'local>(env: JNIEnv<'local>, _class: JClass<'local>,p_id : jint) -> jobject {
    let result = database::read_playlist_items(p_id);
    return_media_list_analyze(env, result).into_raw()
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbPlaylistsAddMediaItem<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                              p_id : jint,
                                                                                              m_path : JString<'local>) {
    let m_path : String = env.get_string(&m_path).unwrap().into();
    database::add_media_item_to_playlist(p_id,&m_path);
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbPlaylistsDelMediaItem<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                                 p_id : jint,
                                                                                                 m_path : JString<'local>) {
    let m_path : String = env.get_string(&m_path).unwrap().into();
    database::del_media_item_from_playlist(p_id,&m_path);
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbMediaPlaylists<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                          m_path : JString<'local>) -> JIntArray<'local> {
    let m_path : String = env.get_string(&m_path).unwrap().into();
    let raw_res = database::media_item_playlists(&m_path);
    let raw_result_len = raw_res.len() as jsize;
    let jint_array = env.new_int_array(raw_result_len).unwrap();
    env.set_int_array_region(&jint_array, 0, &raw_res).unwrap();
    jint_array
}

//waveform
#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbSaveMediaWaveform<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                                 m_path : JString<'local>,
                                                                                             wave_data : JString<'local>) {
    let m_path : String = env.get_string(&m_path).unwrap().into();
    let m_wave_data : String = env.get_string(&wave_data).unwrap().into();
    database::save_media_waveform(&m_path,&m_wave_data);
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_dbGetMediaWaveform<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                            m_path : JString<'local>) -> jstring  {
    let m_path : String = env.get_string(&m_path).unwrap().into();
    let result = database::get_media_waveform(&m_path);
    env.new_string(result).unwrap().into_raw()
}


#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_getBufferAddr<'local>(env: JNIEnv<'local>, _class: JClass<'local>,buffer : JByteBuffer<'local>) -> jlong {
    let buffer_addr = env.get_direct_buffer_address(&buffer).unwrap();
    buffer_addr as jlong
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_fastFileMD5<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,path : JString<'local>) -> jstring {
    let file_path : String = env.get_string(&path).unwrap().into();
    let result = utils::hash_file(&file_path);
    env.new_string(result).unwrap().into_raw()
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_orangeplayer_core_Native_ffmpegMakeVidThumbnail<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                    vidPath : JString<'local>,thumbPath : JString<'local>) -> jboolean {
    let vid_path : String = env.get_string(&vidPath).unwrap().into();
    let thumb_path : String = env.get_string(&thumbPath).unwrap().into();
    if !ffmpeg::is_ffmpeg_installed() {
        return jboolean::from(false)
    };
    let gen_thumb = ffmpeg::make_video_thumbnail(&vid_path,&thumb_path);
    jboolean::from(gen_thumb)
}