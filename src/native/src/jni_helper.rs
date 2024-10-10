use jni::JNIEnv;
use jni::objects::{JObject, JObjectArray, JString, JValue};
use jni::sys::{jboolean, jlong, jsize};
use crate::database::{MediaItem, Playlists};
use crate::scanner::ScanResult;

// -> input
pub(crate) fn convert_scan_folders(env: &mut JNIEnv, input : &JObjectArray) -> Vec<String> {
    let length = env.get_array_length(input).unwrap() as usize;
    let mut result: Vec<String> = Vec::with_capacity(length);
    for i in 0..length {
        let j_object = env.get_object_array_element(input,i as jsize).unwrap();
        let j_str = JString::from(j_object);
        let input: String = env.get_string(&j_str).unwrap().into();
        result.push(input);
    }
    result
}
pub(crate) fn convert_insert_media_items(mut env: JNIEnv,input : &JObjectArray) -> Vec<MediaItem> {
    let length = env.get_array_length(input).unwrap() as usize;
    let mut result: Vec<MediaItem> = Vec::with_capacity(length);
    for i in 0..length {
        let j_object = env.get_object_array_element(input,i as jsize).unwrap();

        let _name = JString::from(env.get_field(&j_object, "name", "Ljava/lang/String;").unwrap().l().unwrap());
        let _album = JString::from(env.get_field(&j_object, "album", "Ljava/lang/String;").unwrap().l().unwrap());
        let _artist = JString::from(env.get_field(&j_object, "artist", "Ljava/lang/String;").unwrap().l().unwrap());
        let _path = JString::from(env.get_field(&j_object, "path", "Ljava/lang/String;").unwrap().l().unwrap());
        let _folder = JString::from(env.get_field(&j_object, "folder", "Ljava/lang/String;").unwrap().l().unwrap());
        let _hash = JString::from(env.get_field(&j_object, "hash", "Ljava/lang/String;").unwrap().l().unwrap());
        let _cover_path = JString::from(env.get_field(&j_object, "coverPath", "Ljava/lang/String;").unwrap().l().unwrap());
        let width = env.get_field(&j_object,"width","I").unwrap().i().unwrap();
        let height = env.get_field(&j_object,"height","I").unwrap().i().unwrap();
        let is_fav = env.get_field(&j_object, "isFav", "Z").unwrap().z().unwrap();
        let is_video = env.get_field(&j_object, "isVideo", "Z").unwrap().z().unwrap();
        let duration = env.get_field(&j_object, "duration", "J").unwrap().j().unwrap() as u64;
        let size = env.get_field(&j_object, "size", "J").unwrap().j().unwrap() as usize;
        let _extension = JString::from(env.get_field(&j_object, "extension", "Ljava/lang/String;").unwrap().l().unwrap());

        let name : String = env.get_string(&_name).unwrap().into();
        let album : String = env.get_string(&_album).unwrap().into();
        let artist : String = env.get_string(&_artist).unwrap().into();
        let path : String = env.get_string(&_path).unwrap().into();
        let folder : String = env.get_string(&_folder).unwrap().into();
        let hash : String = env.get_string(&_hash).unwrap().into();
        let cover_path : String = env.get_string(&_cover_path).unwrap().into();
        let extension : String = env.get_string(&_extension).unwrap().into();

        result.push(MediaItem {
            id : -1,
            name,
            album,
            artist,
            path,
            folder,
            hash,
            cover_path,
            width,
            height,
            is_fav,
            is_video,
            duration,
            size,
            extension
        })
    }
    result
}

// -> output
pub(crate) fn convert_scan_result_to_obj_array(mut env: JNIEnv,input : Vec<ScanResult>) -> JObjectArray {
    let j_model = env.find_class("me/sudodios/orangeplayer/models/ModelScanResult").unwrap();
    let result = env.new_object_array(input.len() as i32,&j_model,JObject::null()).unwrap();
    for (i, item) in input.iter().enumerate() {
        let result_ref = &result;
        let item_obj = env.new_object(&j_model,
                                      "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;J)V",
                                      &[
                                          JValue::Object(&*env.new_string(item.file_name.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(item.path.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(item.folder.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(item.hash.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(item.extension.clone()).unwrap()),
                                          JValue::Long(item.size as jlong)
                                      ]).unwrap();
        env.set_object_array_element(result_ref,i as i32,item_obj).unwrap();
    }
    result
}
pub(crate) fn return_media_list_analyze(mut env: JNIEnv,input : (Vec<MediaItem>,i32,u64)) -> JObject {
    let j_model = env.find_class("me/sudodios/orangeplayer/models/MediaItem").unwrap();
    let list_media_items = env.new_object_array(input.0.len() as i32,&j_model,JObject::null()).unwrap();
    for (i, media_item) in input.0.iter().enumerate() {
        let list_media_items_ref = &list_media_items;
        let item_obj = env.new_object(&j_model,
                                      "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIZZJJLjava/lang/String;)V",
                                      &[
                                          JValue::Int(media_item.id),
                                          JValue::Object(&*env.new_string(media_item.name.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(media_item.album.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(media_item.artist.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(media_item.path.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(media_item.folder.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(media_item.hash.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(media_item.cover_path.clone()).unwrap()),
                                          JValue::Int(media_item.width),
                                          JValue::Int(media_item.height),
                                          JValue::Bool(jboolean::from(media_item.is_fav)),
                                          JValue::Bool(jboolean::from(media_item.is_video)),
                                          JValue::Long(media_item.duration as jlong),
                                          JValue::Long(media_item.size as jlong),
                                          JValue::Object(&*env.new_string(media_item.extension.clone()).unwrap()),
                                      ]).unwrap();
        env.set_object_array_element(list_media_items_ref,i as i32,item_obj).unwrap();
    }
    let result_model = env.find_class("me/sudodios/orangeplayer/models/ModelMediaRead").unwrap();
    let obj_result = env.new_object(&result_model,"([Lme/sudodios/orangeplayer/models/MediaItem;IJ)V",
                                    &[
                                        JValue::Object(&*list_media_items),
                                        JValue::Int(input.1),
                                        JValue::Long(input.2 as jlong)
                                    ]).unwrap();
    obj_result
}
pub(crate) fn return_folders_list(mut env: JNIEnv,input : Vec<(String,i32)>) -> JObjectArray {
    let j_model = env.find_class("me/sudodios/orangeplayer/models/ModelFolderRead").unwrap();
    let result = env.new_object_array(input.len() as i32,&j_model,JObject::null()).unwrap();
    for (i, item) in input.iter().enumerate() {
        let result_ref = &result;
        let item_obj = env.new_object(&j_model,
                                      "(Ljava/lang/String;I)V",
                                      &[
                                          JValue::Object(&*env.new_string(item.0.clone()).unwrap()),
                                          JValue::Int(item.1),
                                      ]).unwrap();
        env.set_object_array_element(result_ref,i as i32,item_obj).unwrap();
    }
    result
}
pub(crate) fn return_playlists_list(mut env: JNIEnv,input : Vec<Playlists>) -> JObjectArray {
    let j_model = env.find_class("me/sudodios/orangeplayer/models/ModelPlaylistsRead").unwrap();
    let result = env.new_object_array(input.len() as i32,&j_model,JObject::null()).unwrap();
    for (i, item) in input.iter().enumerate() {
        let result_ref = &result;
        let item_obj = env.new_object(&j_model,
                                      "(ILjava/lang/String;ILjava/lang/String;)V",
                                      &[
                                          JValue::Int(item.id),
                                          JValue::Object(&*env.new_string(item.title.clone()).unwrap()),
                                          JValue::Int(item.item_count),
                                          JValue::Object(&*env.new_string(item.cover_art.clone()).unwrap()),
                                      ]).unwrap();
        env.set_object_array_element(result_ref,i as i32,item_obj).unwrap();
    }
    result
}