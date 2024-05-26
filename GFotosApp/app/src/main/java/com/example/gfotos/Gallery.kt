package com.example.gfotos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberImagePainter

@Composable
fun Gallery() {
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }

    if (selectedAlbum == null) {
        AlbumList(onAlbumClick = { album -> selectedAlbum = album })
    } else {
        PhotoList(album = selectedAlbum!!, onBackClick = { selectedAlbum = null })
    }
}

data class Album(val id: String, val name: String, val coverUri: Uri)

@Composable
fun AlbumList(onAlbumClick: (Album) -> Unit) {
    val context = LocalContext.current
    val albums = remember { loadAlbums(context) }

    LazyColumn {
        items(albums) { album ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { onAlbumClick(album) }
            ) {
                Image(
                    painter = rememberImagePainter(data = album.coverUri),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(album.name, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun PhotoList(album: Album, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val photos = remember { loadPhotosFromAlbum(context, album.id) }

    Column {
        Button(onClick = onBackClick) {
            Text("Back to Albums")
        }
        LazyColumn {
            items(photos) { photoUri ->
                Image(
                    painter = rememberImagePainter(data = photoUri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

fun loadAlbums(context: Context): List<Album> {
    val albums = mutableListOf<Album>()
    val projection = arrayOf(
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media._ID
    )
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    val cursor: Cursor? = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
    )

    cursor?.use {
        val bucketIdColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
        val bucketNameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val albumMap = mutableMapOf<String, Album>()

        while (it.moveToNext()) {
            val bucketId = it.getString(bucketIdColumn)
            val bucketName = it.getString(bucketNameColumn)
            val imageId = it.getLong(idColumn)
            val imageUri = Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageId.toString()
            )

            if (!albumMap.containsKey(bucketId)) {
                albumMap[bucketId] = Album(bucketId, bucketName, imageUri)
            }
        }

        albums.addAll(albumMap.values)
    }

    return albums
}

fun loadPhotosFromAlbum(context: Context, albumId: String): List<Uri> {
    val photos = mutableListOf<Uri>()
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val selection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
    val selectionArgs = arrayOf(albumId)
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    val cursor: Cursor? = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )

    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

        while (it.moveToNext()) {
            val imageId = it.getLong(idColumn)
            val imageUri = Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageId.toString()
            )
            photos.add(imageUri)
        }
    }

    return photos
}
