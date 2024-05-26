package com.example.gfotos



import androidx.activity.ComponentActivity

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
fun Navigation (){

    val navController = rememberNavController()
    var currentPage: String by remember {
        mutableStateOf("init")
    }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            currentPage = destination.route ?: ""

        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    Scaffold (
        topBar = {
            if(currentPage != "init") {

                TopAppBar(title = { Text(text = "Prova") })
            }
        },
        content = {  paddingValues ->
            NavHost(navController = navController, startDestination = "init"){
                composable(
                    "init"
                ){
                    InitialPage(navController = navController)
                }

                composable(
                    "client"
                ){

                    Column(modifier = Modifier.padding(paddingValues)) {

                        Gallery()
                    }
                }

                composable(
                    "client/albums"
                ){

                    Column(modifier = Modifier.padding(paddingValues)) {

                        AlbumList(onAlbumClick = { album -> navController.navigate("client/albums/${album.id}") })
                    }
                }

            }
        }
    )


}